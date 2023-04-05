package biz.thehacker.airq

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.hc.client5.http.fluent.Request
import org.apache.hc.core5.http.ContentType
import java.security.SecureRandom
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
import javax.crypto.Cipher.ENCRYPT_MODE
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.Charsets.UTF_8

class AirQ(
    private val host: String,
    private val password: String
) {
    private val cryptoBlockSize = 16

    /** password as AES key */
    private val secretKey: SecretKey
        get() = password
            .padEnd(32, '0')
            .substring(0, 32)
            .toByteArray(UTF_8)
            .let { SecretKeySpec(it, "AES") }

    private val objectMapper = jacksonObjectMapper().apply {
        disable(FAIL_ON_UNKNOWN_PROPERTIES)
    }

    val data: Any
        get() = getRequest("/data")

    val log: List<String>
        get() = getRequest("/log")
            .let { objectMapper.readValue<List<String>>(it) }

    var deviceName: String
        get() {
            val config = getRequest("/config")

            return objectMapper.readValue<AirQDevicenameConfig>(config)
                .devicename
        }
        set(value) {
            if (value.length > 16) {
                throw IllegalArgumentException("air-Q only allows up to 16 characters for a device's name.")
            }

            val configData = AirQDevicenameConfig(devicename = value)
                .let { objectMapper.writeValueAsString(it) }

            postRequest("/config", configData)
        }

    /**
     * Identifies an air-Q by blinking all its LEDs and returns the device's ID.
     *
     * Note:
     * The `/blink` endpoint does not contain encrypted data.
     * Anybody in the network can let the air-Q blink.
     */
    fun blink(): String = Request.get("http://$host/blink")
        .execute()
        .returnContent()
        .asStream()
        .use { inputStream -> inputStream.readAllBytes() }
        .let { bytes -> String(bytes, UTF_8) }
        .let { text -> objectMapper.readValue<AirQJsonResponseIdOnly>(text) }
        .id

    fun ping(): Boolean = try {
        getRequest("/ping")
        true // no error during request -> ping ok
    } catch (e: Exception) {
        false
    }

    private fun getRequest(path: String): String {
        val request = Request.get("http://$host$path")

        return executeRequestAndDecryptResponse(request)
    }

    private fun postRequest(path: String, requestData: String): String {
        /**
         * !! IMPORTANT !!
         * Don't use `Form.form().add("request", ...).build()` and `bodyForm()` to build the request's body.
         *
         * air-Q does not support "application/x-www-form-urlencoded" correctly. Instead of
         *
         * ```
         * request=encryptedBase64EncodedData
         * ```
         *
         * air-Q needs additional quotes around the value
         *
         * ```
         * request="encryptedBase64EncodedData"
         * ```
         *
         * to process the request correctly.
         * Otherwise, it will return an HTTP 500, and complain
         *
         * ```
         * Function ConfigPostHandler failed on command  - Type: ValueError; Reason: syntax error in JSON
         * Function ConfigPostHandler failed on command  - Type: NameError; Reason: local variable referenced before assignment
         * ```
         */

        // The following does not work:

        // val form = Form.form()
        //     .add("request", encrypt(requestData))
        //     .build()
        //
        // val request = Request
        //     .post("http://$host$path")
        //     .bodyForm(form)

        val request = Request
            .post("http://$host$path")
            .bodyString("request=\"${encrypt(requestData)}\"", ContentType.APPLICATION_FORM_URLENCODED)

        return executeRequestAndDecryptResponse(request)
    }

    private fun executeRequestAndDecryptResponse(request: Request): String = request
        .execute()
        .returnContent()
        .asStream()
        .use { inputStream -> inputStream.readAllBytes() }
        .let { bytes -> String(bytes, Charsets.US_ASCII) }
        .let { text -> objectMapper.readValue<AirQJsonResponseWithContent>(text) }
        .content
        .let { encryptedData ->
            try {
                decrypt(encryptedData)
            } catch (e: BadPaddingException) {
                throw AirQPasswordWrongException(e)
            }
        }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decrypt(encryptedData: String): String {
        val decoded = Base64.decode(encryptedData)

        val iv = IvParameterSpec(decoded, 0, cryptoBlockSize)

        return Cipher
            .getInstance("AES/CBC/PKCS5Padding")
            .run {
                init(DECRYPT_MODE, secretKey, iv)
                doFinal(decoded, blockSize, decoded.size - blockSize)
            }
            .toString(UTF_8)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encrypt(plainData: String): String {
        val iv = ByteArray(cryptoBlockSize)
            .apply { SecureRandom().nextBytes(this) }
            .let { IvParameterSpec(it) }

        val encoded = plainData.toByteArray(UTF_8)

        return Cipher
            .getInstance("AES/CBC/PKCS5Padding")
            .run {
                init(ENCRYPT_MODE, secretKey, iv)
                doFinal(encoded)
            }
            .let { Base64.encode(iv.iv + it) }
    }
}

class AirQPasswordWrongException(cause: BadPaddingException) : RuntimeException(
    "Decryption of air-Q data failed. " +
        "This means the provided password for air-Q does not match.",
    cause
)

private data class AirQJsonResponseIdOnly(
    val id: String
)

private data class AirQJsonResponseWithContent(
    val content: String
)

private data class AirQDevicenameConfig(
    val devicename: String
)
