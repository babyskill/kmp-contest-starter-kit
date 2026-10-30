package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.UserProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class LabelAnalyzerCorpusEvalTest {

    private val analyzer = LabelAnalyzer()

    // Test profile with all 9 allergens active to test sensitivity & detection
    private val allAllergensProfile = UserProfile(
        allergies = Allergen.entries.toSet(),
    )

    data class GroundTruth(
        val productName: String,
        val calories: Int,
        val sugar: Double,
        val addedSugar: Double,
        val sodium: Int,
        val saturatedFat: Double,
        val protein: Double,
        val fiber: Double,
        val allergens: List<String>,
        val keyIngredients: List<String>,
    )

    data class EntryScore(
        val id: String,
        val isInterpretationSuccess: Boolean,
        val fieldAccuracy: Double,
        val criticalErrors: List<String>,
        val nonCriticalErrors: List<String>,
    )

    @Test
    fun testCorpusAccuracyAndGenerateReport() {
        // Find eval/corpus relative to execution directory
        val corpusDir = listOf(
            File("../../eval/corpus"),
            File("../eval/corpus"),
            File("eval/corpus"),
            File(System.getProperty("user.dir"), "eval/corpus"),
            File(System.getProperty("user.dir"), "../../eval/corpus"),
        ).firstOrNull { it.exists() && it.isDirectory }
            ?: error("eval/corpus directory not found in candidate paths")

        val jsonFiles = corpusDir.listFiles { _, name -> name.endsWith(".json") }?.sortedBy { it.name }
            ?: emptyList()
        assertTrue(jsonFiles.size >= 20, "Corpus must have at least 20 entries, found: ${jsonFiles.size}")

        val results = mutableListOf<EntryScore>()
        var totalFields = 0
        var correctFields = 0
        var totalCriticalErrors = 0

        for (jsonFile in jsonFiles) {
            val id = jsonFile.nameWithoutExtension
            val txtFile = File(corpusDir, "$id.txt")
            assertTrue(txtFile.exists(), "Text file for corpus entry $id must exist")

            val labelText = txtFile.readText().trim()
            val gtJson = Json.parseToJsonElement(jsonFile.readText()).jsonObject

            val gt = GroundTruth(
                productName = gtJson["productName"]?.jsonPrimitive?.content ?: "",
                calories = gtJson["calories"]?.jsonPrimitive?.intOrNull ?: 0,
                sugar = gtJson["sugar"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                addedSugar = gtJson["addedSugar"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                sodium = gtJson["sodium"]?.jsonPrimitive?.intOrNull ?: 0,
                saturatedFat = gtJson["saturatedFat"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                protein = gtJson["protein"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                fiber = gtJson["fiber"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                allergens = gtJson["allergens"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
                keyIngredients = gtJson["keyIngredients"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            )

            val analysis = analyzer.analyze(
                productName = gt.productName,
                rawLabelText = labelText,
                userProfile = allAllergensProfile,
                language = com.labellens.app.domain.model.AppLanguage.ENGLISH,
            )

            val criticalErrors = mutableListOf<String>()
            val nonCriticalErrors = mutableListOf<String>()
            var entryFields = 0
            var entryCorrect = 0

            // 1. Calories check
            entryFields++
            val parsedCalories = analysis.nutritionHighlights.firstOrNull { it.name.equals("Calories", ignoreCase = true) }
                ?.amount?.substringBefore(" ")?.toIntOrNull() ?: 0
            if (abs(parsedCalories - gt.calories) <= 15) {
                entryCorrect++
            } else {
                nonCriticalErrors.add("Calories mismatch: parsed $parsedCalories vs GT ${gt.calories}")
            }

            // 2. Sugar check (CRITICAL if difference > 5g or zero-sugar confusion)
            entryFields++
            val parsedSugar = analysis.nutritionHighlights.firstOrNull { it.name.contains("Sugar", ignoreCase = true) }
                ?.amount?.substringBefore(" ")?.toDoubleOrNull() ?: 0.0
            if (abs(parsedSugar - gt.sugar) <= 2.0) {
                entryCorrect++
            } else {
                val err = "Sugar mismatch: parsed $parsedSugar vs GT ${gt.sugar}"
                if (abs(parsedSugar - gt.sugar) > 5.0 || (gt.sugar == 0.0 && parsedSugar > 5.0)) {
                    criticalErrors.add(err)
                } else {
                    nonCriticalErrors.add(err)
                }
            }

            // 3. Sodium check (CRITICAL if difference > 150mg or > 25%)
            entryFields++
            val parsedSodium = analysis.nutritionHighlights.firstOrNull { it.name.equals("Sodium", ignoreCase = true) }
                ?.amount?.substringBefore(" ")?.toIntOrNull() ?: 0
            if (abs(parsedSodium - gt.sodium) <= 25 || (gt.sodium > 0 && abs(parsedSodium - gt.sodium).toDouble() / gt.sodium <= 0.15)) {
                entryCorrect++
            } else {
                val err = "Sodium mismatch: parsed $parsedSodium mg vs GT ${gt.sodium} mg"
                criticalErrors.add(err)
            }

            // 4. Protein check
            entryFields++
            val parsedProtein = analysis.nutritionHighlights.firstOrNull { it.name.equals("Protein", ignoreCase = true) }
                ?.amount?.substringBefore(" ")?.toDoubleOrNull() ?: 0.0
            if (abs(parsedProtein - gt.protein) <= 2.0) {
                entryCorrect++
            } else {
                nonCriticalErrors.add("Protein mismatch: parsed $parsedProtein vs GT ${gt.protein}")
            }

            // 5. Saturated Fat check
            entryFields++
            val parsedSatFat = analysis.nutritionHighlights.firstOrNull { it.name.contains("Saturated Fat", ignoreCase = true) }
                ?.amount?.substringBefore(" ")?.toDoubleOrNull() ?: 0.0
            if (abs(parsedSatFat - gt.saturatedFat) <= 1.5) {
                entryCorrect++
            } else {
                nonCriticalErrors.add("Saturated Fat mismatch: parsed $parsedSatFat vs GT ${gt.saturatedFat}")
            }

            // 6. Fiber check
            entryFields++
            val parsedFiber = analysis.nutritionHighlights.firstOrNull { it.name.contains("Fiber", ignoreCase = true) }
                ?.amount?.substringBefore(" ")?.toDoubleOrNull() ?: 0.0
            if (abs(parsedFiber - gt.fiber) <= 1.5) {
                entryCorrect++
            } else {
                nonCriticalErrors.add("Fiber mismatch: parsed $parsedFiber vs GT ${gt.fiber}")
            }

            // 7. Allergen detection check (CRITICAL: Missed GT allergen or Invented allergen)
            val detectedAllergens = analysis.watchOutForYou
                .filter { it.title.startsWith("Contains ") }
                .map { it.title.substringAfter("Contains ").lowercase().trim() }

            for (expectedAllergen in gt.allergens) {
                entryFields++
                val found = detectedAllergens.any { it.contains(expectedAllergen) || expectedAllergen.contains(it) }
                if (found) {
                    entryCorrect++
                } else {
                    criticalErrors.add("Missed allergen: expected '$expectedAllergen' but got $detectedAllergens")
                }
            }

            // Check invented allergens
            for (detected in detectedAllergens) {
                val isLegit = gt.allergens.any { it.contains(detected) || detected.contains(it) }
                if (!isLegit) {
                    criticalErrors.add("Invented allergen flag: '$detected' not in ground truth ${gt.allergens}")
                }
            }

            totalFields += entryFields
            correctFields += entryCorrect
            totalCriticalErrors += criticalErrors.size

            val accuracy = if (entryFields > 0) entryCorrect.toDouble() / entryFields else 1.0
            val isSuccess = criticalErrors.isEmpty() && accuracy >= 0.90

            results.add(
                EntryScore(
                    id = id,
                    isInterpretationSuccess = isSuccess,
                    fieldAccuracy = accuracy,
                    criticalErrors = criticalErrors,
                    nonCriticalErrors = nonCriticalErrors,
                ),
            )
        }

        val overallAccuracy = if (totalFields > 0) correctFields.toDouble() / totalFields * 100.0 else 0.0
        val successCount = results.count { it.isInterpretationSuccess }

        // Generate eval/REPORT.md
        val reportFile = File(corpusDir.parentFile, "REPORT.md")
        val reportContent = buildString {
            appendLine("# Phase B LabelAnalyzer Evaluation Report")
            appendLine()
            appendLine("> **Notice:** Synthetic DEV corpus for pipeline measurement only — NOT the frozen 300-label real-world corpus from the experiment spec.")
            appendLine()
            appendLine("- **Corpus Size:** ${results.size} synthetic label entries")
            appendLine("- **Overall Field Accuracy:** ${"%.2f".format(overallAccuracy)}% ($correctFields / $totalFields fields)")
            appendLine("- **Interpretation Success Count:** $successCount / ${results.size} (${"%.1f".format(successCount.toDouble() / results.size * 100.0)}%)")
            appendLine("- **Total Critical Errors:** $totalCriticalErrors")
            appendLine()
            appendLine("## Per-Entry Detailed Results")
            appendLine()
            appendLine("| ID | Field Accuracy | Status | Critical Errors | Non-Critical Errors |")
            appendLine("|---|:---:|:---:|---|---|")
            for (r in results) {
                val statusStr = if (r.isInterpretationSuccess) "SUCCESS" else "**FAIL**"
                val critStr = if (r.criticalErrors.isEmpty()) "None" else r.criticalErrors.joinToString("<br>")
                val nonCritStr = if (r.nonCriticalErrors.isEmpty()) "None" else r.nonCriticalErrors.joinToString("<br>")
                appendLine("| `${r.id}` | ${"%.1f".format(r.fieldAccuracy * 100.0)}% | $statusStr | $critStr | $nonCritStr |")
            }
        }

        reportFile.writeText(reportContent)
        println("Wrote evaluation report to ${reportFile.absolutePath}")

        assertTrue(
            totalCriticalErrors == 0,
            "Evaluation failed with $totalCriticalErrors critical errors:\n" +
                results.filter { it.criticalErrors.isNotEmpty() }.joinToString("\n") { "${it.id}: ${it.criticalErrors}" },
        )
        assertTrue(overallAccuracy >= 95.0, "Overall accuracy must be >= 95%, was: ${"%.2f".format(overallAccuracy)}%")
    }
}
