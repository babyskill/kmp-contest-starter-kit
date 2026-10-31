package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AllergenCategory
import com.labellens.app.domain.model.AppLanguage
import com.labellens.app.domain.model.AttentionFlag
import com.labellens.app.domain.model.Market
import com.labellens.app.domain.model.MarketPacks
import com.labellens.app.domain.model.NutritionFact
import com.labellens.app.domain.model.PortionInfo
import com.labellens.app.domain.model.ProfileFitState
import com.labellens.app.domain.model.UserProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Wave2ProfileFitTest {

    private val analyzer = LabelAnalyzer()

    // ==========================================
    // 1. Pack Contents & Differences
    // ==========================================

    @Test
    fun testUSPackHasExactly9Categories() {
        val usPack = MarketPacks.US
        assertEquals(9, usPack.categories.size, "US pack must have exactly 9 FALCPA/FASTER categories")
        val expected = listOf(
            AllergenCategory.MILK,
            AllergenCategory.EGG,
            AllergenCategory.FISH,
            AllergenCategory.CRUSTACEANS,
            AllergenCategory.TREE_NUTS,
            AllergenCategory.PEANUT,
            AllergenCategory.WHEAT_GLUTEN,
            AllergenCategory.SOY,
            AllergenCategory.SESAME,
        )
        assertEquals(expected.toSet(), usPack.categories.toSet())
    }

    @Test
    fun testEUUKPackHasExactly14Categories() {
        val euPack = MarketPacks.EU_UK
        assertEquals(14, euPack.categories.size, "EU/UK pack must have exactly 14 categories")
        val expected = listOf(
            AllergenCategory.WHEAT_GLUTEN,
            AllergenCategory.CRUSTACEANS,
            AllergenCategory.EGG,
            AllergenCategory.FISH,
            AllergenCategory.PEANUT,
            AllergenCategory.SOY,
            AllergenCategory.MILK,
            AllergenCategory.TREE_NUTS,
            AllergenCategory.CELERY,
            AllergenCategory.MUSTARD,
            AllergenCategory.SESAME,
            AllergenCategory.SULPHITES,
            AllergenCategory.LUPIN,
            AllergenCategory.MOLLUSCS,
        )
        assertEquals(expected.toSet(), euPack.categories.toSet())
    }

    @Test
    fun testAUNZPackDiffersFromEUAsSpecified() {
        val auPack = MarketPacks.AU_NZ
        val euPack = MarketPacks.EU_UK

        // FSANZ list has exactly 12 categories
        assertEquals(12, auPack.categories.size, "AU/NZ pack must have 12 categories")

        // Must assert AU != EU: Celery and Mustard absent in AU, present in EU
        assertFalse(auPack.categories.contains(AllergenCategory.CELERY), "Celery must be absent in AU/NZ pack")
        assertFalse(auPack.categories.contains(AllergenCategory.MUSTARD), "Mustard must be absent in AU/NZ pack")
        assertTrue(euPack.categories.contains(AllergenCategory.CELERY), "Celery must be present in EU/UK pack")
        assertTrue(euPack.categories.contains(AllergenCategory.MUSTARD), "Mustard must be present in EU/UK pack")
    }

    @Test
    fun testJPAndKRHaveNoPackData() {
        assertNull(MarketPacks.getPack(Market.JP), "JP enum exists as placeholder with NO pack data")
        assertNull(MarketPacks.getPack(Market.KR), "KR enum exists as placeholder with NO pack data")
    }

    @Test
    fun testImportedFoodModeUnionOfAllThreePacks() {
        val union = MarketPacks.activeCategories(market = Market.US, importedFoodMode = true)
        assertEquals(14, union.size, "Imported food mode union must contain all 14 categories across US, EU/UK, AU/NZ")
        assertTrue(union.contains(AllergenCategory.CELERY))
        assertTrue(union.contains(AllergenCategory.MUSTARD))
        assertTrue(union.contains(AllergenCategory.LUPIN))
        assertTrue(union.contains(AllergenCategory.MOLLUSCS))
        assertTrue(union.contains(AllergenCategory.SESAME))
    }

    // ==========================================
    // 2. Six-State Priority Matrix
    // Priority: CONTAINS_YOURS > CANNOT_RULE_OUT > NOT_ENOUGH_INFO > DEPENDS_ON_PORTION > WORTH_NOTING > NO_MATCHES
    // ==========================================

    @Test
    fun testPriorityContainsYoursTrumpsAllOthers() {
        // Label has: direct peanut match (CONTAINS_YOURS), precautionary warning, cut-off comma, and high sodium
        val label = "INGREDIENTS: Peanuts, Sugar, Salt, May contain tree nuts, and"
        val profile = UserProfile(
            allergies = setOf(Allergen.PEANUT, Allergen.TREE_NUTS),
            enabledAttentionFlags = setOf(AttentionFlag.SODIUM),
        )
        val result = analyzer.analyze("Peanut Snack", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.CONTAINS_YOURS, result.fitHeader?.state)
    }

    @Test
    fun testPriorityCannotRuleOutTrumpsNotEnoughInfoAndFlags() {
        // Label has: precautionary warning for Tree Nuts, cut-off comma, and high sodium
        // User is allergic to Tree Nuts, NOT Peanuts
        val label = "INGREDIENTS: Oats, Sugar, Salt, May contain tree nuts,"
        val profile = UserProfile(
            allergies = setOf(Allergen.TREE_NUTS),
            enabledAttentionFlags = setOf(AttentionFlag.SODIUM),
        )
        val result = analyzer.analyze("Oat Bar", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.CANNOT_RULE_OUT, result.fitHeader?.state)
    }

    @Test
    fun testPriorityNotEnoughInfoTrumpsPortionAndWorthNoting() {
        // Label has: cut-off trailing comma, high whole pack sodium, but no allergens
        val label = "INGREDIENTS: Oats, Corn, Salt,\nNutrition Facts: Serving size 1 bar. Servings per container: 2. Sodium: 300mg."
        val profile = UserProfile(
            allergies = emptySet(),
            enabledAttentionFlags = setOf(AttentionFlag.SODIUM),
        )
        val result = analyzer.analyze("Grain Bar", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.NOT_ENOUGH_INFO, result.fitHeader?.state)
    }

    @Test
    fun testPriorityDependsOnPortionWhenTriggeredOnlyAtWholePack() {
        // Sodium: 300mg per serving (< 460mg per serving), but 2 servings per container = 600mg (>= 460mg whole pack)
        val label = "INGREDIENTS: Enriched Flour, Water, Yeast, Salt.\nNutrition Facts: Serving size 1 slice. Servings per container: 2. Sodium: 300mg."
        val profile = UserProfile(
            allergies = emptySet(),
            enabledAttentionFlags = setOf(AttentionFlag.SODIUM),
        )
        val result = analyzer.analyze("Bread", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.DEPENDS_ON_PORTION, result.fitHeader?.state)
    }

    @Test
    fun testPriorityWorthNotingWhenTriggeredPerServing() {
        // Sodium: 500mg per serving (>= 460mg per serving)
        val label = "INGREDIENTS: Enriched Flour, Water, Yeast, Salt.\nNutrition Facts: Serving size 1 slice. Servings per container: 1. Sodium: 500mg."
        val profile = UserProfile(
            allergies = emptySet(),
            enabledAttentionFlags = setOf(AttentionFlag.SODIUM),
        )
        val result = analyzer.analyze("Savory Bread", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.WORTH_NOTING, result.fitHeader?.state)
    }

    @Test
    fun testPriorityNoMatchesWhenCompleteAndNoFlagsApply() {
        val label = "INGREDIENTS: Rolled Oats, Filtered Water, Sea Salt.\nNutrition Facts: Serving size 1 cup. Servings per container: 1. Sodium: 80mg."
        val profile = UserProfile(
            allergies = setOf(Allergen.PEANUT),
            enabledAttentionFlags = setOf(AttentionFlag.SODIUM, AttentionFlag.ADDED_SUGARS),
        )
        val result = analyzer.analyze("Plain Oatmeal", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.NO_MATCHES, result.fitHeader?.state)
    }

    // ==========================================
    // 3. CANNOT_RULE_OUT Scenarios
    // ==========================================

    @Test
    fun testCannotRuleOutViaPrecautionaryStatement() {
        val label = "INGREDIENTS: Organic Whole Wheat Flour, Canola Oil, Sea Salt. Manufactured in a facility that also processes peanuts."
        val profile = UserProfile(
            allergies = setOf(Allergen.PEANUT),
        )
        val result = analyzer.analyze("Crackers", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.CANNOT_RULE_OUT, result.fitHeader?.state)
        assertTrue(result.fitHeader?.matchedTerms?.any { it.contains("peanut", ignoreCase = true) } == true)
    }

    @Test
    fun testCannotRuleOutViaCatchAllInUSMarket() {
        // In US market, "natural flavor" can legally hide milk/egg/soy/peanut/tree-nut derivatives
        val label = "INGREDIENTS: Whole Grain Oats, Sugar, Natural Flavor, Salt."
        val profile = UserProfile(
            allergies = setOf(Allergen.MILK),
            selectedMarket = Market.US,
            enabledAttentionFlags = emptySet(),
        )
        val result = analyzer.analyze("Flavored Oats", label, profile)
        assertNotNull(result.fitHeader)
        assertEquals(ProfileFitState.CANNOT_RULE_OUT, result.fitHeader?.state)
        assertTrue(result.fitHeader?.matchedTerms?.any { it.contains("natural flavor", ignoreCase = true) } == true)
    }

    // ==========================================
    // 4. Never Silent-Pass (Cut-off Detection)
    // ==========================================

    @Test
    fun testNeverSilentPassTrailingComma() {
        val label = "INGREDIENTS: Rolled Oats, Cane Sugar, Canola Oil,"
        assertTrue(ProfileFitEvaluator.isCutOffDetected(label, listOf("Rolled Oats", "Cane Sugar", "Canola Oil")))
        val result = analyzer.analyze("Cut-off Cereal", label, UserProfile())
        assertEquals(ProfileFitState.NOT_ENOUGH_INFO, result.fitHeader?.state)
    }

    @Test
    fun testNeverSilentPassTrailingAnd() {
        val label = "INGREDIENTS: Rolled Oats, Cane Sugar, and"
        assertTrue(ProfileFitEvaluator.isCutOffDetected(label, listOf("Rolled Oats", "Cane Sugar")))
        val result = analyzer.analyze("Cut-off Cereal", label, UserProfile())
        assertEquals(ProfileFitState.NOT_ENOUGH_INFO, result.fitHeader?.state)
    }

    @Test
    fun testNeverSilentPassTrailingOr() {
        val label = "INGREDIENTS: Sunflower Oil, Canola Oil or"
        assertTrue(ProfileFitEvaluator.isCutOffDetected(label, listOf("Sunflower Oil", "Canola Oil")))
        val result = analyzer.analyze("Cut-off Oil", label, UserProfile())
        assertEquals(ProfileFitState.NOT_ENOUGH_INFO, result.fitHeader?.state)
    }

    @Test
    fun testNeverSilentPassTrailingOpenParen() {
        val label = "INGREDIENTS: Enriched Flour, Preservative ("
        assertTrue(ProfileFitEvaluator.isCutOffDetected(label, listOf("Enriched Flour", "Preservative")))
        val result = analyzer.analyze("Cut-off Bread", label, UserProfile())
        assertEquals(ProfileFitState.NOT_ENOUGH_INFO, result.fitHeader?.state)
    }

    @Test
    fun testNeverSilentPassNoIngredientsSectionCaptured() {
        val label = "Brand Super Crunchy Chips - 100% Delicious!"
        assertTrue(ProfileFitEvaluator.isCutOffDetected(label, emptyList()))
        val result = analyzer.analyze("Chips", label, UserProfile())
        assertEquals(ProfileFitState.NOT_ENOUGH_INFO, result.fitHeader?.state)
    }

    // ==========================================
    // 5. Personal Exceptions vs Allergen Rule
    // ==========================================

    @Test
    fun testExceptionSuppressesAttentionFlag() {
        val label = "INGREDIENTS: Carbonated Water, Citric Acid, Sucralose, Natural Flavors."
        val profileWithoutException = UserProfile(
            allergies = emptySet(),
            enabledAttentionFlags = setOf(AttentionFlag.NON_NUTRITIVE_SWEETENERS),
            ingredientExceptions = emptySet(),
        )
        val result1 = analyzer.analyze("Diet Soda", label, profileWithoutException)
        assertEquals(ProfileFitState.WORTH_NOTING, result1.fitHeader?.state)

        // Adding "sucralose" to personal exceptions
        val profileWithException = UserProfile(
            allergies = emptySet(),
            enabledAttentionFlags = setOf(AttentionFlag.NON_NUTRITIVE_SWEETENERS),
            ingredientExceptions = setOf("sucralose"),
        )
        val result2 = analyzer.analyze("Diet Soda", label, profileWithException)
        assertEquals(ProfileFitState.NO_MATCHES, result2.fitHeader?.state, "Exception must suppress attention flag")
    }

    @Test
    fun testExceptionNeverSuppressesDirectAllergenMatch() {
        // HARD RULE: User exception must NEVER suppress a CONTAINS_YOURS direct allergen match
        val label = "INGREDIENTS: Roasted Peanuts, Vegetable Oil, Sea Salt."
        val profile = UserProfile(
            allergies = setOf(Allergen.PEANUT),
            ingredientExceptions = setOf("peanut", "peanuts", "roasted peanuts"),
        )
        val result = analyzer.analyze("Peanut Jar", label, profile)
        assertEquals(ProfileFitState.CONTAINS_YOURS, result.fitHeader?.state, "Exceptions must NEVER suppress CONTAINS_YOURS")
    }

    // ==========================================
    // 6. False-Positive Whitelist (Individual Tests)
    // ==========================================

    @Test
    fun testFalsePositiveNutmegIsNotTreeNut() {
        val match = ProfileFitEvaluator.matchAllergenCategory("Nutmeg", AllergenCategory.TREE_NUTS)
        assertNull(match, "Nutmeg must NOT match tree nut allergen")

        val label = "INGREDIENTS: Wheat Flour, Sugar, Nutmeg, Cinnamon, Salt."
        val profile = UserProfile(allergies = setOf(Allergen.TREE_NUTS))
        val result = analyzer.analyze("Spiced Bread", label, profile)
        assertFalse(result.fitHeader?.state == ProfileFitState.CONTAINS_YOURS, "Nutmeg must not trigger CONTAINS_YOURS for Tree Nuts")
    }

    @Test
    fun testFalsePositiveCocoaButterIsNotMilk() {
        val match = ProfileFitEvaluator.matchAllergenCategory("Cocoa Butter", AllergenCategory.MILK)
        assertNull(match, "Cocoa Butter must NOT match milk allergen")

        val label = "INGREDIENTS: Dark Chocolate, Cocoa Butter, Cane Sugar, Vanilla."
        val profile = UserProfile(allergies = setOf(Allergen.MILK))
        val result = analyzer.analyze("Dark Chocolate", label, profile)
        assertFalse(result.fitHeader?.state == ProfileFitState.CONTAINS_YOURS, "Cocoa butter must not trigger CONTAINS_YOURS for Milk")
    }

    @Test
    fun testFalsePositiveLacticAcidIsNotMilk() {
        val match = ProfileFitEvaluator.matchAllergenCategory("Lactic Acid", AllergenCategory.MILK)
        assertNull(match, "Lactic Acid must NOT match milk allergen")

        val label = "INGREDIENTS: Pickled Cucumbers, Water, Salt, Lactic Acid."
        val profile = UserProfile(allergies = setOf(Allergen.MILK))
        val result = analyzer.analyze("Pickles", label, profile)
        assertFalse(result.fitHeader?.state == ProfileFitState.CONTAINS_YOURS, "Lactic acid must not trigger CONTAINS_YOURS for Milk")
    }

    @Test
    fun testFalsePositiveCreamOfTartarIsNotMilk() {
        val match = ProfileFitEvaluator.matchAllergenCategory("Cream of Tartar", AllergenCategory.MILK)
        assertNull(match, "Cream of Tartar must NOT match milk allergen")

        val label = "INGREDIENTS: Flour, Sugar, Baking Soda, Cream of Tartar."
        val profile = UserProfile(allergies = setOf(Allergen.MILK))
        val result = analyzer.analyze("Snickerdoodle", label, profile)
        assertFalse(result.fitHeader?.state == ProfileFitState.CONTAINS_YOURS, "Cream of tartar must not trigger CONTAINS_YOURS for Milk")
    }

    // ==========================================
    // 7. Attention Flags Named Constants & Lists
    // ==========================================

    @Test
    fun testAttentionFlagsNamedConstantsAndLists() {
        assertEquals(460.0, ProfileFitEvaluator.SODIUM_HIGH_THRESHOLD_MG)
        assertEquals(10.0, ProfileFitEvaluator.ADDED_SUGARS_HIGH_THRESHOLD_G)

        // Non-nutritive sweeteners: exactly non-nutritive, sugar alcohols excluded
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("sucralose"))
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("aspartame"))
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("acesulfame potassium"))
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("saccharin"))
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("stevia"))
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("monk fruit extract"))
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("neotame"))
        assertTrue(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("advantame"))

        // Sugar alcohols must NOT be in non-nutritive sweeteners list
        assertFalse(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("erythritol"))
        assertFalse(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("xylitol"))
        assertFalse(ProfileFitEvaluator.NON_NUTRITIVE_SWEETENERS.contains("sorbitol"))

        // Caffeine sources
        assertTrue(ProfileFitEvaluator.CAFFEINE_SOURCES.contains("caffeine"))
        assertTrue(ProfileFitEvaluator.CAFFEINE_SOURCES.contains("guarana"))
        assertTrue(ProfileFitEvaluator.CAFFEINE_SOURCES.contains("yerba mate"))
        assertTrue(ProfileFitEvaluator.CAFFEINE_SOURCES.contains("green tea extract"))

        // Artificial colours: caramel colour is NOT artificial
        assertTrue(ProfileFitEvaluator.ARTIFICIAL_COLOURS.contains("red 40"))
        assertTrue(ProfileFitEvaluator.ARTIFICIAL_COLOURS.contains("yellow 5"))
        assertTrue(ProfileFitEvaluator.ARTIFICIAL_COLOURS.contains("blue 1"))
        assertFalse(ProfileFitEvaluator.ARTIFICIAL_COLOURS.contains("caramel"))
        assertFalse(ProfileFitEvaluator.ARTIFICIAL_COLOURS.contains("caramel color"))
    }

    // ==========================================
    // 8. Custom Free-Text Allergen Token-Boundary
    // ==========================================

    @Test
    fun testCustomAllergenTokenBoundaryMatching() {
        val label = "INGREDIENTS: Corn Starch, Sugar, Gelatin, Water."
        val profile = UserProfile(
            allergies = emptySet(),
            customAllergens = listOf("Gelatin"),
        )
        val result = analyzer.analyze("Gummy", label, profile)
        assertEquals(ProfileFitState.CONTAINS_YOURS, result.fitHeader?.state)
        assertTrue(result.fitHeader?.matchedTerms?.any { it.contains("Gelatin", ignoreCase = true) } == true)
    }

    // ==========================================
    // 9. Vietnamese Localization Overlay
    // ==========================================

    @Test
    fun testVietnameseLocalizationOverlayHasZeroEnglishLeakageInHeader() {
        val label = "THÀNH PHẦN: Đậu phộng, Đường mía, Muối biển."
        val profile = UserProfile(
            allergies = setOf(Allergen.PEANUT),
        )
        val result = analyzer.analyze("Bơ Đậu Phộng", label, profile, language = AppLanguage.VIETNAMESE)
        val header = result.fitHeader
        assertNotNull(header)
        assertEquals(ProfileFitState.CONTAINS_YOURS, header.state)
        assertEquals("Chứa chất gây dị ứng của bạn", header.title)
        assertTrue(header.subtitle.contains("hồ sơ cá nhân"))
    }
}
