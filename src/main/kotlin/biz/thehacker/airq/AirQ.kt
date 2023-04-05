package biz.thehacker.airq

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.hc.client5.http.fluent.Request
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.Cipher.DECRYPT_MODE
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

    val deviceName: String
        get() {
            val config = getRequest("/config")

            return objectMapper.readTree(config)
                .get("devicename")
                .asText()
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

    private fun getRequest(path: String): String = Request
        .get("http://$host$path")
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
