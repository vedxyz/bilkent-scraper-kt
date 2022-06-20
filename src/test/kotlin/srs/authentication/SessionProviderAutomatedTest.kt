package srs.authentication

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import srs.srsId
import srs.srsPassword
import srs.webmailAddress
import srs.webmailPassword
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SessionProviderAutomatedTest {

    @Test
    fun getSessionCookie() = runBlocking {
        val (cookie, reference) = initializeLogin(srsId, srsPassword)
        println("Initial cookie: $cookie, verification reference: $reference")
        assertTrue(cookie.startsWith("PHPSESSID"), "Initial cookie key is correct")
        assertTrue(reference.length == 4, "Initial reference code is correct")

        val (code, actualRef) = getVerificationCode(webmailAddress, webmailPassword)
        println("Verification code: $code, actual reference: $actualRef")
        assertTrue(code.length == 5)
        assertEquals(reference, actualRef, "Reference codes match")

        val sessionCookie = verifyEmail(cookie, code)
        println("Session cookie: $sessionCookie")
        assertTrue(sessionCookie.startsWith("PHPSESSID"), "Final cookie key is correct")
    }
}
