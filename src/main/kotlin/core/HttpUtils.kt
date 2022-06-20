package core

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.coroutines.resumeWithException

internal enum class HttpMethods { GET, POST }

internal object HttpUtils {

    // Core

    private var client = OkHttpClient().newBuilder().followRedirects(false).build()

    private suspend fun fetch(request: Request): Response = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                continuation.resumeWith(Result.success(response))
            }
        })

        continuation.invokeOnCancellation {
            try {
                call.cancel()
            } catch (_: Throwable) {
            }
        }
    }

    suspend fun fetch(
        url: String, method: HttpMethods = HttpMethods.GET, headers: Headers? = null, body: RequestBody? = null
    ): Response {
        val requestBuilder = Request.Builder().url(url).method(method.toString(), body)
        if (headers != null) requestBuilder.headers(headers)
        val request = requestBuilder.build()

        return fetch(request)
    }

    // SRS

    private const val LOGGED_OUT_RESPONSE = "You are logged out from SRS"

    suspend fun guardedFetch(url: String, cookie: String, body: RequestBody? = null): String {
        fetch(
            url, if (body == null) HttpMethods.GET else HttpMethods.POST, Headers.headersOf("Cookie", cookie), body
        ).use {
            val content = it.body!!.string()
            if (content.startsWith(LOGGED_OUT_RESPONSE)) throw Exception("Unauthenticated request, cookie may be invalid")
            return content
        }
    }

    suspend fun guardedFetchImage(url: String, cookie: String): String {
        fetch(url, HttpMethods.GET, Headers.headersOf("Cookie", cookie)).use {
            val textContent = it.peekBody(LOGGED_OUT_RESPONSE.toByteArray().size.toLong()).string()
            if (textContent.startsWith("You are logged out from SRS")) throw Exception("Unauthenticated request, cookie may be invalid")

            val byteContent = it.body!!.bytes()
            return "data:image/${it.headers["Content-Type"]};base64,${Base64.getEncoder().encodeToString(byteContent)}"
        }
    }

    // Utility

    fun getCookie(response: Response, key: String): String =
        response.headers["Set-Cookie"]?.split("; ")?.find { it.startsWith(key) }
            ?: throw Exception("No cookie by name '$key'")

    fun getRedirect(response: Response): String =
        response.headers["Location"] ?: throw Exception("No redirect location sent in response")
}
