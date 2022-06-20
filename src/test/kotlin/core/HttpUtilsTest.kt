package core

import core.HttpUtils.guardedFetch
import core.HttpUtils.guardedFetchImage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class HttpUtilsTest {
    private val guardedEndpoint = "https://stars.bilkent.edu.tr/srs/ajax/exam/index.php"
    private val sampleSessionCookie = "PHPSESSID=5tbcb6ltnlel5chaqukcst3fg7"

    @Test
    fun guardedFetchTestFail(): Unit = runBlocking {
        assertFails {
            guardedFetch(guardedEndpoint, sampleSessionCookie)
        }
    }

    @Test
    fun guardedFetchImageTestFail(): Unit = runBlocking {
        assertFails {
            guardedFetchImage(guardedEndpoint, sampleSessionCookie)
        }
    }
}
