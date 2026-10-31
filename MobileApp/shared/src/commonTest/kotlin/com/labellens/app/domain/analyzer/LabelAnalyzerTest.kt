package com.labellens.app.domain.analyzer

import com.labellens.app.domain.model.Allergen
import com.labellens.app.domain.model.AppLanguage
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
            language = AppLanguage.ENGLISH,
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
            language = AppLanguage.ENGLISH,
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
            language = AppLanguage.ENGLISH,
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
            language = AppLanguage.ENGLISH,
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
            language = AppLanguage.ENGLISH,
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
            language = AppLanguage.ENGLISH,
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
            language = AppLanguage.ENGLISH,
        )
        assertTrue(result1.watchOutForYou.none { it.title.contains("Sesame") })

        val profileWithSesame = UserProfile(allergies = setOf(Allergen.SESAME))
        val result2 = analyzer.analyze(
            productName = sample.name,
            rawLabelText = sample.fullLabelText,
            userProfile = profileWithSesame,
            language = AppLanguage.ENGLISH,
        )
        assertTrue(result2.watchOutForYou.any { it.title.contains("Sesame") })
    }

    @Test
    fun testVietnameseModeFlagsAndSummaries() {
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
            language = AppLanguage.VIETNAMESE,
        )

        // (a) Flag titles in Vietnamese
        assertTrue(result.watchOutForYou.any { it.title.contains("Chứa") && it.title.contains("Giáp xác") })
        assertTrue(result.watchOutForYou.any { it.title.contains("Bột ngọt") && it.title.contains("MSG") })
        assertTrue(result.watchOutForYou.any { it.title.contains("Hàm lượng natri cao") })

        // Check quick glance
        assertNotNull(result.quickGlance)
        assertTrue(result.quickGlance!!.contains("lưu ý cá nhân"))

        // Check disclaimer
        val expectedVnDisclaimer = "Chỉ mang tính chất giáo dục - đây không phải lời khuyên y tế."
        val sample4Result = analyzer.analyze(
            productName = BuiltInSamples.sample4.name,
            rawLabelText = BuiltInSamples.sample4.fullLabelText,
            userProfile = UserProfile(),
            category = BuiltInSamples.sample4.category,
            scanSource = ScanSource.SAMPLE,
            language = AppLanguage.VIETNAMESE,
        )
        assertTrue(sample4Result.verdict.endsWith(expectedVnDisclaimer))

        // Ensure NO English template leftovers in result flags, summary, or glance
        val allTitles = (result.watchOutForYou + result.worthNoting + result.goodToKnow).map { it.title }
        assertTrue(allTitles.none { it.startsWith("Contains ") })
        assertTrue(allTitles.none { it.startsWith("Avoid: ") })
        assertTrue(allTitles.none { it.contains("High Sodium Level") })
        assertFalse(result.quickGlance!!.contains("ingredients parsed"))
        assertFalse(result.quickGlance!!.contains("personal note"))
    }

    @Test
    fun testEnglishModeFlagsAndSemantics() {
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
            language = AppLanguage.ENGLISH,
        )

        // (b) English mode semantics
        assertTrue(result.watchOutForYou.any { it.title.contains("Contains") && it.title.contains("Shellfish") })
        assertTrue(result.watchOutForYou.any { it.title.contains("MSG") })
        assertTrue(result.watchOutForYou.any { it.title.contains("High Sodium Level") })

        assertNotNull(result.quickGlance)
        assertTrue(result.quickGlance!!.contains("ingredients parsed"))
        assertTrue(result.quickGlance!!.contains("personal notes"))
    }

    @Test
    fun testFirstLaunchDefaultLanguageIsVietnamese() {
        // (c) App default language is Vietnamese
        assertEquals(AppLanguage.VIETNAMESE, AppLanguage.DEFAULT)
        assertEquals("vi", AppLanguage.DEFAULT.code)
        assertEquals("Tiếng Việt", AppLanguage.DEFAULT.displayName)

        // Also test UserProfileRepository returns Vietnamese by default on fresh install
        val fakePrefs = object : com.labellens.app.data.source.preferences.UserPreferences {
            private val store = mutableMapOf<String, Any>()
            override suspend fun getString(key: String, defaultValue: String?): String? = store[key] as? String ?: defaultValue
            override suspend fun getInt(key: String, defaultValue: Int?): Int? = store[key] as? Int ?: defaultValue
            override suspend fun getLong(key: String, defaultValue: Long?): Long? = store[key] as? Long ?: defaultValue
            override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = store[key] as? Boolean ?: defaultValue
            override suspend fun putString(key: String, value: String) {
                store[key] = value
            }
            override suspend fun putInt(key: String, value: Int) {
                store[key] = value
            }
            override suspend fun putLong(key: String, value: Long) {
                store[key] = value
            }
            override suspend fun putBoolean(key: String, value: Boolean) {
                store[key] = value
            }
            override suspend fun remove(key: String) {
                store.remove(key)
            }
            override suspend fun clear() {
                store.clear()
            }
            override suspend fun hasSeenExitDownsell(): Boolean = false
            override suspend fun setExitDownsellSeen(seen: Boolean) {}
            override suspend fun getDownsellStartTimeMillis(): Long? = null
            override suspend fun setDownsellStartTimeMillis(timeMillis: Long) {}
        }

        val repo = com.labellens.app.data.repository.UserProfileRepositoryImpl(fakePrefs)
        kotlinx.coroutines.test.runTest {
            val initialLang = repo.getLanguage()
            assertEquals(AppLanguage.VIETNAMESE, initialLang)
            assertEquals(AppLanguage.VIETNAMESE, repo.languageFlow.value)
        }
    }

    @Test
    fun testCaneSugarHoneySeaSaltLocalizationVietnameseAndEnglish() {
        val labelText = "INGREDIENTS: Cane Sugar, Honey, Sea Salt."

        // 1. Language = VIETNAMESE
        val resultVi = analyzer.analyze(
            productName = "Test Product",
            rawLabelText = labelText,
            userProfile = UserProfile(),
            language = AppLanguage.VIETNAMESE,
        )

        val sugarVi = resultVi.fullBreakdown.firstOrNull { it.canonicalName == "Đường mía" }
        assertNotNull(sugarVi, "Must find Vietnamese card for Cane Sugar ('Đường mía')")
        assertEquals("Chất tạo ngọt", sugarVi.category)
        assertFalse(sugarVi.plainExplanation.contains("Standard carbohydrate extracted from sugar cane"), "Must not contain English explanation")
        assertFalse(sugarVi.whyUsed.contains("Sweetens, tenderises dough crumb"), "Must not contain English whyUsed")
        assertFalse(sugarVi.doseContextNote.contains("Standard serving adds fast-digesting carbohydrates"), "Must not contain English doseContextNote")
        assertTrue(sugarVi.plainExplanation.contains("Carbohydrate tiêu chuẩn chiết xuất từ cây mía"), "Must contain Vietnamese explanation")

        val honeyVi = resultVi.fullBreakdown.firstOrNull { it.canonicalName == "Mật ong" }
        assertNotNull(honeyVi, "Must find Vietnamese card for Honey ('Mật ong')")
        assertEquals("Chất tạo ngọt tự nhiên", honeyVi.category)
        assertFalse(honeyVi.plainExplanation.contains("A sweet fluid produced by honeybees"), "Must not contain English explanation")
        assertFalse(honeyVi.whyUsed.contains("Imparts warm floral sweetness"), "Must not contain English whyUsed")
        assertFalse(honeyVi.doseContextNote.contains("Natural sweetener containing trace minerals"), "Must not contain English doseContextNote")
        assertTrue(honeyVi.plainExplanation.contains("Dịch ngọt tự nhiên do ong mật"), "Must contain Vietnamese explanation")

        val saltVi = resultVi.fullBreakdown.firstOrNull { it.canonicalName == "Muối biển" }
        assertNotNull(saltVi, "Must find Vietnamese card for Sea Salt ('Muối biển')")
        assertEquals("Khoáng chất / Gia vị", saltVi.category)
        assertFalse(saltVi.plainExplanation.contains("Essential dietary mineral composed primarily"), "Must not contain English explanation")
        assertFalse(saltVi.whyUsed.contains("Essential flavor catalyst that enhances sweetness"), "Must not contain English whyUsed")
        assertFalse(saltVi.doseContextNote.contains("Major dietary contributor of sodium"), "Must not contain English doseContextNote")
        assertTrue(saltVi.plainExplanation.contains("Khoáng chất thiết yếu"), "Must contain Vietnamese explanation")

        // Assert no English explanation text anywhere in breakdown
        for (item in resultVi.fullBreakdown) {
            assertFalse(item.plainExplanation.contains("Standard carbohydrate"), "Breakdown plainExplanation must not have English")
            assertFalse(item.plainExplanation.contains("A sweet fluid"), "Breakdown plainExplanation must not have English")
            assertFalse(item.plainExplanation.contains("Essential dietary mineral"), "Breakdown plainExplanation must not have English")
        }

        // Check worth noting or good to know titles and details in Vietnamese
        for (item in resultVi.worthNoting + resultVi.goodToKnow) {
            assertFalse(item.title.contains("Cane Sugar"), "Title must not be English")
            assertFalse(item.title.contains("Sea Salt"), "Title must not be English")
            assertFalse(item.detail.contains("Standard carbohydrate"), "Detail must not contain English")
            assertFalse(item.detail.contains("Used as"), "Detail must not contain English 'Used as'")
        }

        // 2. Language = ENGLISH
        val resultEn = analyzer.analyze(
            productName = "Test Product",
            rawLabelText = labelText,
            userProfile = UserProfile(),
            language = AppLanguage.ENGLISH,
        )

        val sugarEn = resultEn.fullBreakdown.firstOrNull { it.canonicalName == "Cane Sugar / Sucrose" }
        assertNotNull(sugarEn, "Must find English card for Cane Sugar ('Cane Sugar / Sucrose')")
        assertEquals("Sweetener", sugarEn.category)
        assertEquals("Standard carbohydrate extracted from sugar cane or sugar beet.", sugarEn.plainExplanation)
        assertEquals("Sweetens, tenderises dough crumb, and aids golden browning.", sugarEn.whyUsed)
        assertEquals("Standard serving adds fast-digesting carbohydrates.", sugarEn.doseContextNote)

        val honeyEn = resultEn.fullBreakdown.firstOrNull { it.canonicalName == "Honey" }
        assertNotNull(honeyEn, "Must find English card for Honey")
        assertEquals("Natural Sweetener", honeyEn.category)
        assertEquals("A sweet fluid produced by honeybees from plant nectars.", honeyEn.plainExplanation)

        val saltEn = resultEn.fullBreakdown.firstOrNull { it.canonicalName == "Sea Salt" }
        assertNotNull(saltEn, "Must find English card for Sea Salt")
        assertEquals("Mineral / Seasoning", saltEn.category)
        assertEquals("Essential dietary mineral composed primarily of sodium chloride.", saltEn.plainExplanation)
    }

    @Test
    fun testAllKnowledgeBaseEntriesHaveVietnameseTranslations() {
        val total = KnowledgeBase.ingredients.size
        assertEquals(127, total)
        for (ing in KnowledgeBase.ingredients) {
            val viData = KnowledgeBaseVi.get(ing.canonicalName)
            assertNotNull(viData, "Missing Vietnamese translation for: ${ing.canonicalName}")
            assertTrue(viData.displayName.isNotBlank(), "Blank displayName for: ${ing.canonicalName}")
            assertTrue(viData.category.isNotBlank(), "Blank category for: ${ing.canonicalName}")
            assertTrue(viData.plainExplanation.isNotBlank(), "Blank plainExplanation for: ${ing.canonicalName}")
            assertTrue(viData.whyUsed.isNotBlank(), "Blank whyUsed for: ${ing.canonicalName}")
            assertTrue(viData.doseContextNote.isNotBlank(), "Blank doseContextNote for: ${ing.canonicalName}")
        }
    }

    @Test
    fun testNutritionNameVietnameseCoverage() {
        val nutrients = listOf(
            "Calories", "Sugars", "Sodium", "Saturated Fat", "Protein", "Dietary Fiber",
            "Total Sugar", "Total Sugars", "Added Sugar", "Added Sugars",
            "Trans Fat", "Total Fat", "Fat", "Cholesterol", "Total Carbohydrate",
            "Carbohydrate", "Carbohydrates", "Carbs", "Calcium", "Iron", "Potassium",
            "Vitamin A", "Vitamin C", "Vitamin D", "Vitamin E",
        )
        for (nutrient in nutrients) {
            val vi = AnalysisStrings.nutritionName(AppLanguage.VIETNAMESE, nutrient)
            assertFalse(vi.isBlank(), "Blank nutrition name for: $nutrient")
            val en = AnalysisStrings.nutritionName(AppLanguage.ENGLISH, nutrient)
            assertEquals(nutrient, en, "English nutrition name should match standardName")
        }
    }
}
