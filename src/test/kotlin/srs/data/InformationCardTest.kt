package srs.data

import TestClass
import json
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import java.io.FileNotFoundException

internal class InformationCardTest : TestClass() {
    companion object {
        private lateinit var sample: String

        @BeforeAll
        @JvmStatic
        fun setUp() = runBlocking {
            sample = readSample("/srs/infoCard.html")
        }
    }

    @Test
    fun parseInformationCard() {
        val informationCard = parseInformationCard(Jsoup.parse(sample), null)
        println(json(informationCard))
    }
}
