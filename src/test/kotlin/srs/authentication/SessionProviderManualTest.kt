package srs.authentication

import srs.SRSSession
import kotlinx.coroutines.runBlocking
import srs.srsId
import srs.srsPassword

internal fun main() = runBlocking {
    factoryMethod()
    // fullyManual()
}

internal suspend fun fullyManual() {
    val (cookie, reference) = initializeLogin(srsId, srsPassword)
    println("Initial cookie: $cookie, verification reference: $reference")

    print("Verification code: ")
    val code = readln()
    if (code.length != 5) throw IllegalStateException("Illegal verification code given by user")

    val sessionCookie = verifyEmail(cookie, code)
    println("Session cookie: $sessionCookie")
}

internal suspend fun factoryMethod() {
    val (reference, verify) = SRSSession.withManualVerification(srsId, srsPassword)
    println("Verification reference: $reference")

    print("Verification code: ")
    val code = readln()
    if (code.length != 5) throw IllegalStateException("Illegal verification code given by user")

    val session = verify(code)
}
