package com.labellens.app.presentation.screens.result

import com.labellens.app.domain.model.FlagSeverity
import com.labellens.app.domain.model.FlaggedItem
import com.labellens.app.domain.model.IngredientBreakdown
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ScanSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IngredientGroupingTest {

    private fun sampleIngredient(
        canonicalName: String,
        rawName: String = canonicalName,
        category: String = "General",
        aliases: List<String> = emptyList(),
    ) = IngredientBreakdown(
        rawName = rawName,
        canonicalName = canonicalName,
        category = category,
        plainExplanation = "Test explanation",
        whyUsed = "Test why used",
        doseContextNote = "Test dose note",
        matchedAliases = aliases,
    )

    @Test
    fun `groupAndOrderIngredients orders watch out first, worth noting second, good to know third, remaining last`() {
        val ingOats = sampleIngredient("Oats")
        val ingSugar = sampleIngredient("Sugar")
        val ingCaffeine = sampleIngredient("Caffeine")
        val ingPreservative = sampleIngredient("Sodium Benzoate")
        val ingSalt = sampleIngredient("Salt")

        val watchOut = listOf(
            FlaggedItem(
                title = "Contains Caffeine",
                detail = "Alert for caffeine",
                severity = FlagSeverity.PERSONAL_AVOID,
                relatedIngredient = "Caffeine",
            ),
        )
        val worthNoting = listOf(
            FlaggedItem(
                title = "Sodium Benzoate",
                detail = "Common preservative",
                severity = FlagSeverity.WORTH_NOTING,
                relatedIngredient = "Sodium Benzoate",
            ),
        )
        val goodToKnow = listOf(
            FlaggedItem(
                title = "Oats",
                detail = "Wholesome whole grain",
                severity = FlagSeverity.GOOD_TO_KNOW,
                relatedIngredient = "Oats",
            ),
        )

        val input = listOf(ingOats, ingSugar, ingCaffeine, ingPreservative, ingSalt)
        val result = groupAndOrderIngredients(
            ingredients = input,
            watchOut = watchOut,
            worthNoting = worthNoting,
            goodToKnow = goodToKnow,
        )

        assertEquals(5, result.size)
        // 1. Watch Out
        assertEquals("Caffeine", result[0].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.WATCH_OUT, result[0].group)

        // 2. Worth Noting
        assertEquals("Sodium Benzoate", result[1].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.WORTH_NOTING, result[1].group)

        // 3. Good to Know
        assertEquals("Oats", result[2].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.GOOD_TO_KNOW, result[2].group)

        // 4. Remaining items in relative order
        assertEquals("Sugar", result[3].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.REMAINING, result[3].group)

        assertEquals("Salt", result[4].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.REMAINING, result[4].group)
    }

    @Test
    fun `groupAndOrderIngredients deduplicates ingredients so each appears exactly once`() {
        val ing1 = sampleIngredient("Wheat Flour")
        val ing2 = sampleIngredient("Sugar")
        val ing3 = sampleIngredient("Wheat Flour") // Duplicate
        val ing4 = sampleIngredient("wheat flour") // Duplicate case variation
        val ing5 = sampleIngredient("Salt")

        val result = groupAndOrderIngredients(
            ingredients = listOf(ing1, ing2, ing3, ing4, ing5),
            watchOut = emptyList(),
            worthNoting = emptyList(),
            goodToKnow = emptyList(),
        )

        assertEquals(3, result.size, "Duplicate ingredients must be deduplicated")
        val names = result.map { it.ingredient.canonicalName.lowercase() }
        assertEquals(listOf("wheat flour", "sugar", "salt"), names)
        assertEquals(result.size, result.distinctBy { it.ingredient.canonicalName.lowercase() }.size)
    }

    @Test
    fun `groupAndOrderIngredients matches flags by relatedIngredient, title, verbatimSourceLine, or aliases`() {
        // Alias match
        val matcha = sampleIngredient("Green Tea", aliases = listOf("matcha powder", "camellia sinensis"))
        // Title match
        val peanuts = sampleIngredient("Peanuts")
        // Verbatim match
        val dye = sampleIngredient("Red 40", rawName = "FD&C Red No. 40")

        val watchOut = listOf(
            FlaggedItem(title = "Peanuts", detail = "Allergen", severity = FlagSeverity.PERSONAL_ALLERGEN),
        )
        val worthNoting = listOf(
            FlaggedItem(title = "Artificial Color", detail = "Coloring", severity = FlagSeverity.WORTH_NOTING, verbatimSourceLine = "FD&C Red No. 40"),
        )
        val goodToKnow = listOf(
            FlaggedItem(title = "Antioxidants", detail = "Beneficial", severity = FlagSeverity.GOOD_TO_KNOW, relatedIngredient = "matcha powder"),
        )

        val result = groupAndOrderIngredients(
            ingredients = listOf(dye, matcha, peanuts),
            watchOut = watchOut,
            worthNoting = worthNoting,
            goodToKnow = goodToKnow,
        )

        assertEquals(3, result.size)
        assertEquals("Peanuts", result[0].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.WATCH_OUT, result[0].group)

        assertEquals("Red 40", result[1].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.WORTH_NOTING, result[1].group)

        assertEquals("Green Tea", result[2].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.GOOD_TO_KNOW, result[2].group)
    }

    @Test
    fun `groupAndOrderIngredients handles empty input gracefully`() {
        val result = groupAndOrderIngredients(
            ingredients = emptyList(),
            watchOut = emptyList(),
            worthNoting = emptyList(),
            goodToKnow = emptyList(),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `groupAndOrderIngredients preserves order within same group`() {
        val itemA = sampleIngredient("Alpha")
        val itemB = sampleIngredient("Beta")
        val itemC = sampleIngredient("Gamma")

        val result = groupAndOrderIngredients(
            ingredients = listOf(itemA, itemB, itemC),
            watchOut = emptyList(),
            worthNoting = emptyList(),
            goodToKnow = emptyList(),
        )

        assertEquals(listOf("Alpha", "Beta", "Gamma"), result.map { it.ingredient.canonicalName })
    }

    @Test
    fun `groupAndOrderIngredients with LabelAnalysisResult overload orders and deduplicates properly`() {
        val oats = sampleIngredient("Rolled Oats")
        val honey = sampleIngredient("Raw Honey")
        val salt = sampleIngredient("Salt")

        val analysis = LabelAnalysisResult(
            productName = "Granola",
            category = ProductCategory.FOOD,
            scanSource = ScanSource.PHOTO,
            verdict = "Wholesome snack",
            watchOutForYou = emptyList(),
            worthNoting = emptyList(),
            goodToKnow = listOf(
                FlaggedItem(
                    title = "Rolled Oats",
                    detail = "Wholesome fiber source",
                    severity = FlagSeverity.GOOD_TO_KNOW,
                    relatedIngredient = "Rolled Oats",
                ),
            ),
            fullBreakdown = listOf(salt, oats, honey, salt), // Note: salt is duplicate
            rawInputText = "Ingredients: Salt, Rolled Oats, Raw Honey, Salt",
        )

        val grouped = groupAndOrderIngredients(analysis)

        // Expected: Rolled Oats (Good to know) first, then Salt, Raw Honey (Remaining)
        assertEquals(3, grouped.size, "Duplicate salt must be deduplicated")
        assertEquals("Rolled Oats", grouped[0].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.GOOD_TO_KNOW, grouped[0].group)

        assertEquals("Salt", grouped[1].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.REMAINING, grouped[1].group)

        assertEquals("Raw Honey", grouped[2].ingredient.canonicalName)
        assertEquals(IngredientDetailGroup.REMAINING, grouped[2].group)
    }
}
