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
        .let { text -> objectMapper.readValue<AirQJsonResponse>(text) }
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

private data class AirQJsonResponse(
    val content: String
)
