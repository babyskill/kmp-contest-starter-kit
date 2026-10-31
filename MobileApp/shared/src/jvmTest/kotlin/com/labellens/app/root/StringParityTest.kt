package com.labellens.app.root

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringParityTest {

    @Test
    fun testComposeResourcesKeyParityBetweenEnglishAndVietnamese() {
        val rootDir = File(".").canonicalFile
        // Handle when run from MobileApp/ or project root
        val baseResDir = if (File(rootDir, "shared/src/commonMain/composeResources").exists()) {
            File(rootDir, "shared/src/commonMain/composeResources")
        } else if (File(rootDir, "MobileApp/shared/src/commonMain/composeResources").exists()) {
            File(rootDir, "MobileApp/shared/src/commonMain/composeResources")
        } else {
            File(rootDir, "../shared/src/commonMain/composeResources")
        }

        val enFile = File(baseResDir, "values/strings.xml")
        val viFile = File(baseResDir, "values-vi/strings.xml")

        assertTrue(enFile.exists(), "EN strings.xml must exist at ${enFile.absolutePath}")
        assertTrue(viFile.exists(), "VI strings.xml must exist at ${viFile.absolutePath}")

        fun extractKeys(file: File): Set<String> {
            val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = db.parse(file)
            val stringNodes = doc.getElementsByTagName("string")
            val keys = mutableSetOf<String>()
            for (i in 0 until stringNodes.length) {
                val node = stringNodes.item(i)
                val name = node.attributes.getNamedItem("name")?.nodeValue
                if (!name.isNullOrBlank()) {
                    keys.add(name)
                }
            }
            return keys
        }

        val enKeys = extractKeys(enFile)
        val viKeys = extractKeys(viFile)

        val missingInVi = enKeys - viKeys
        val missingInEn = viKeys - enKeys

        assertTrue(missingInVi.isEmpty(), "Keys missing in Vietnamese (values-vi/strings.xml): $missingInVi")
        assertTrue(missingInEn.isEmpty(), "Keys missing in English (values/strings.xml): $missingInEn")
        assertEquals(enKeys.size, viKeys.size, "Total number of string keys must match exactly")
    }
}
