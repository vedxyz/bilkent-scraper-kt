package srs.authentication

import core.HttpMethods
import core.HttpUtils
import core.HttpUtils.getCookie
import core.HttpUtils.getRedirect
import okhttp3.FormBody
import okhttp3.Headers
import org.jsoup.Jsoup

/**
 * @param cookie The initial session cookie, to be used to finalize authentication
 * @param reference Reference code for the verification code
 */
internal data class LoginRequestInfo(val cookie: String, val reference: String)

private const val VERIFY_EMAIL_URL = "https://stars.bilkent.edu.tr/accounts/auth/verifyEmail"

/**
 * Initializes a login request for Bilkent SRS.
 *
 * The reference code is to be used to identify the corresponding verification code.
 *
 * The returned initial cookie must be fed into the next stage of the authentication process [verifyEmail].
 *
 * @param id Bilkent student ID
 * @param password Bilkent SRS password
 * @return An object holding a reference code and the initial session cookie
 */
internal suspend fun initializeLogin(id: String, password: String): LoginRequestInfo {
    val oauthMainResponse = HttpUtils.fetch("https://stars.bilkent.edu.tr/srs/oauth-login.php")

    val cookie = getCookie(oauthMainResponse, "PHPSESSID")
    val headers = Headers.headersOf("Cookie", cookie)

    val oauthAuthResponse = HttpUtils.fetch(getRedirect(oauthMainResponse), HttpMethods.GET, headers)
    val loginIntermediatePageResponse = HttpUtils.fetch(getRedirect(oauthAuthResponse), HttpMethods.GET, headers)
    val loginPageResponse = HttpUtils.fetch(getRedirect(loginIntermediatePageResponse), HttpMethods.GET, headers)

    HttpUtils.fetch(
        loginPageResponse.request.url.toString(),
        HttpMethods.POST,
        headers,
        FormBody.Builder()
            .add("LoginForm[username]", id)
            .add("LoginForm[password]", password)
            .add("yt0", "")
            .build()
    )

    HttpUtils.fetch(VERIFY_EMAIL_URL, HttpMethods.GET, headers).use {
        val reference = Jsoup.parse(it.body!!.string())
            .selectFirst("#verifyEmail-form div.controls > p.help-block > span > strong")
            ?.text()
            ?: throw Exception("No reference code found on 2FA verification page, incorrect credentials likely")
        return LoginRequestInfo(cookie, reference)
    }
}

/**
 * Completes a login request for Bilkent SRS by verifying the two-factor authentication code.
 *
 * @see initializeLogin
 * @param initialCookie The session cookie returned by {@link initializeLogin}
 * @param code The verification code
 * @returns An authenticated SRS session cookie
 */
internal suspend fun verifyEmail(initialCookie: String, code: String): String {
    val codeResponse = HttpUtils.fetch(
        VERIFY_EMAIL_URL,
        HttpMethods.POST,
        Headers.headersOf("Cookie", initialCookie),
        FormBody.Builder().add("EmailVerifyForm[verifyCode]", code).add("yt0", "").build()
    )

    val newCookie = try {
        getCookie(codeResponse, "PHPSESSID")
    } catch (e: Exception) {
        throw Exception("No PHPSESSID cookie, incorrect verification code likely")
    }

    val headers = Headers.headersOf("Cookie", newCookie, "Referer", VERIFY_EMAIL_URL)

    val oauthAuthResponse = HttpUtils.fetch(getRedirect(codeResponse), HttpMethods.GET, headers)

    val authorizationHeaders = headers.newBuilder()
        .set("Cookie", headers["Cookie"]!! + "; ${getCookie(oauthAuthResponse, "authorize")}")
        .build()

    val oauthMainResponse = HttpUtils.fetch(getRedirect(oauthAuthResponse), HttpMethods.GET, authorizationHeaders)
    val srsResponse = HttpUtils.fetch(
        "https://stars.bilkent.edu.tr${getRedirect(oauthMainResponse)}", HttpMethods.GET, authorizationHeaders
    )
    HttpUtils.fetch(getRedirect(srsResponse), HttpMethods.GET, authorizationHeaders)

    return newCookie
}
