package srs.data

import TestClass
import json
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeAll

internal class TranscriptTest : TestClass() {
    companion object {
        private lateinit var sample: String

        @BeforeAll
        @JvmStatic
        fun setUp() = runBlocking {
            sample = readSample("/srs/transcript.html")
        }
    }

    @Test
    fun parseTranscript() {
        val transcript = parseTranscript(Jsoup.parse(sample))
        println(json(transcript))
    }
}
