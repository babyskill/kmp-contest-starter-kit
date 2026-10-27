package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AvoidPreference
import com.labellens.app.domain.model.BuiltInSamples
import com.labellens.app.domain.model.DietaryGoal
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ScanSource
import com.labellens.app.domain.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LabelAnalyzerTest {

    private val analyzer = LabelAnalyzer()

    @Test
    fun testKnowledgeBaseContainsAtLeast120Ingredients() {
        val total = KnowledgeBase.ingredients.size
        assertTrue(total >= 120, "Knowledge base must contain at least 120 ingredients, found $total")
    }

    @Test
    fun testAsianAliasesCoverage() {
        // Must include Korean, Japanese, and Chinese alias names for at least 30 most common additives/ingredients
        var countWithAllThreeAsianLanguages = 0
        for (item in KnowledgeBase.ingredients) {
            val hasKorean = item.aliases.any { alias -> alias.any { c -> c in '\uAC00'..'\uD7AF' } }
            val hasJapanese = item.aliases.any { alias -> alias.any { c -> (c in '\u3040'..'\u309F') || (c in '\u30A0'..'\u30FF') } }
            val hasChinese = item.aliases.any { alias -> alias.any { c -> c in '\u4E00'..'\u9FFF' } }

            if (hasKorean && (hasJapanese || hasChinese)) {
                countWithAllThreeAsianLanguages++
            }
        }
        assertTrue(
            countWithAllThreeAsianLanguages >= 30,
            "Must have at least 30 ingredients with Asian alias support, found $countWithAllThreeAsianLanguages",
        )
    }

    @Test
    fun testSample1HoneyOatCerealAnalysis() {
        val sample = BuiltInSamples.sample1
        val result = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = UserProfile(),
            category = sample.category,
            scanSource = ScanSource.SAMPLE,
        )

        assertEquals("Honey Oat Cereal", result.productName)
        assertTrue(result.goodToKnow.any { it.title.contains("Oat") || it.title.contains("Honey") })
        assertTrue(result.nutritionHighlights.any { it.name == "Calories" && it.amount.contains("160") })
        assertFalse(result.verdict.contains("toxic"))
        assertFalse(result.verdict.contains("dirty"))
        assertFalse(result.verdict.contains("bad"))
    }

    @Test
    fun testSample2RoastedSeaweedSnackKoreanAnalysis() {
        val sample = BuiltInSamples.sample2
        val profile = UserProfile(allergies = setOf(Allergen.SESAME))
        val result = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = profile,
            category = sample.category,
            scanSource = ScanSource.SAMPLE,
        )

        assertEquals("Roasted Seaweed Snack", result.productName)
        // Sesame allergen match in Watch out for you
        assertTrue(result.watchOutForYou.any { it.title.contains("Sesame") })
        assertTrue(result.fullBreakdown.any { it.canonicalName.contains("Seaweed") || it.canonicalName.contains("Sesame") })
    }

    @Test
    fun testSample3MatchaBiscuitsJapaneseAnalysis() {
        val sample = BuiltInSamples.sample3
        val profile = UserProfile(
            allergies = setOf(Allergen.MILK, Allergen.WHEAT_GLUTEN),
            avoidList = setOf(AvoidPreference.PALM_OIL),
        )
        val result = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = profile,
            category = sample.category,
            scanSource = ScanSource.SAMPLE,
        )

        assertTrue(result.watchOutForYou.any { it.title.contains("Milk") })
        assertTrue(result.watchOutForYou.any { it.title.contains("Wheat") })
        assertTrue(result.watchOutForYou.any { it.title.contains("Palm Oil") })
    }

    @Test
    fun testSample4SupplementGuardrailDisclaimer() {
        val sample = BuiltInSamples.sample4
        val result = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = UserProfile(),
            category = sample.category,
            scanSource = ScanSource.SAMPLE,
        )

        val expectedDisclaimer = "Education only - this is not medical advice."
        assertTrue(
            result.verdict.endsWith(expectedDisclaimer),
            "Supplement verdict must append exactly '$expectedDisclaimer', but was '${result.verdict}'",
        )
    }

    @Test
    fun testSample5ColaZeroSugarAnalysis() {
        val sample = BuiltInSamples.sample5
        val profile = UserProfile(avoidList = setOf(AvoidPreference.ARTIFICIAL_SWEETENERS))
        val result = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = profile,
            category = sample.category,
            scanSource = ScanSource.SAMPLE,
        )

        assertTrue(result.watchOutForYou.any { it.title.contains("Artificial Sweetener") })
        assertTrue(result.worthNoting.any { it.title.contains("Caramel Color") || it.title.contains("Phosphoric Acid") })
    }

    @Test
    fun testSample6ShrimpNoodlesHighSodiumAndAllergen() {
        val sample = BuiltInSamples.sample6
        val profile = UserProfile(
            allergies = setOf(Allergen.SHELLFISH),
            goals = setOf(DietaryGoal.LOW_SODIUM),
            avoidList = setOf(AvoidPreference.MSG, AvoidPreference.HIGH_SODIUM),
        )
        val result = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = profile,
            category = sample.category,
            scanSource = ScanSource.SAMPLE,
        )

        assertTrue(result.watchOutForYou.any { it.title.contains("Shellfish") })
        assertTrue(result.watchOutForYou.any { it.title.contains("MSG") })
        assertTrue(result.watchOutForYou.any { it.title.contains("Sodium") })
    }

    @Test
    fun testReFlaggingOnProfileChange() {
        val sample = BuiltInSamples.sample2 // Contains Sesame
        val profileWithoutSesame = UserProfile()
        val result1 = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = profileWithoutSesame,
        )
        assertTrue(result1.watchOutForYou.none { it.title.contains("Sesame") })

        val profileWithSesame = UserProfile(allergies = setOf(Allergen.SESAME))
        val result2 = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = profileWithSesame,
        )
        assertTrue(result2.watchOutForYou.any { it.title.contains("Sesame") })
    }
}
