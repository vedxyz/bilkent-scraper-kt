package authentication

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import webmailAddress
import webmailPassword
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class VerificationProviderTest {

    @Test
    fun getVerificationCode() = runBlocking {
        val (code, ref) = getVerificationCode(webmailAddress, webmailPassword)

        println("Code: $code, Reference: $ref")
        assertFalse(code.isEmpty())
        assertTrue(code.length == 5)
        assertFalse(ref.isEmpty())
        assertTrue(ref.length == 4)
    }
}
