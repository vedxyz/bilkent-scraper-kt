package utility

import okhttp3.Response

internal fun getCookie(response: Response, key: String): String =
    response.headers["Set-Cookie"]?.split("; ")?.find { it.startsWith(key) }
        ?: throw Exception("No cookie by name '$key'")

internal fun getRedirect(response: Response): String =
    response.headers["Location"] ?: throw Exception("No redirect location sent in response")
