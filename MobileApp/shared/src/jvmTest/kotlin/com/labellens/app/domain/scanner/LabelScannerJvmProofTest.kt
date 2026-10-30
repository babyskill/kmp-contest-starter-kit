package com.labellens.app.domain.scanner

import com.labellens.app.domain.analyzer.LabelAnalyzer
import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.UserProfile
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LabelScannerJvmProofTest {

    private val analyzer = LabelAnalyzer()

    @Test
    fun testHeldOutLabelOcrAnalysisEndToEnd() {
        val labelFile = listOf(
            File("../../eval/corpus/heldout01_almond_butter.txt"),
            File("../eval/corpus/heldout01_almond_butter.txt"),
            File("eval/corpus/heldout01_almond_butter.txt"),
            File(System.getProperty("user.dir"), "eval/corpus/heldout01_almond_butter.txt"),
        ).firstOrNull { it.exists() } ?: error("heldout01_almond_butter.txt not found")

        val rawOcrText = labelFile.readText()

        // User profile with Tree Nuts allergen active
        val profile = UserProfile(
            allergies = setOf(Allergen.TREE_NUTS),
        )

        val result = analyzer.analyze(
            productName = "Almond Butter — Creamy",
            rawLabelText = rawOcrText,
            userProfile = profile,
            scanSource = com.labellens.app.domain.model.ScanSource.PHOTO,
            language = com.labellens.app.domain.model.AppLanguage.ENGLISH,
        )

        // Verifications
        assertEquals("Almond Butter — Creamy", result.productName)
        assertTrue(result.watchOutForYou.any { it.title.contains("Tree Nuts", ignoreCase = true) }, "Must flag tree nuts allergen")
        assertTrue(result.nutritionHighlights.any { it.name == "Calories" && it.amount.contains("190") })
        assertTrue(result.nutritionHighlights.any { it.name == "Sodium" && it.amount.contains("65") })
        assertTrue(result.fullBreakdown.any { it.canonicalName.contains("Almond", ignoreCase = true) })
    }
}
