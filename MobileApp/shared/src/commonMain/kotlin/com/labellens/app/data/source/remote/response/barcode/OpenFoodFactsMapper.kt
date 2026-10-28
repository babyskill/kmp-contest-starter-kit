package com.labellens.app.data.source.remote.response.barcode

fun OpenFoodFactsResponse.toLabelText(): String {
    val prod = product ?: return ""
    val sb = StringBuilder()

    prod.ingredientsText?.takeIf { it.isNotBlank() }?.let {
        sb.append("INGREDIENTS: ").append(it).append("\n\n")
    }

    prod.allergens?.takeIf { it.isNotBlank() }?.let {
        sb.append("CONTAINS: ").append(it).append("\n\n")
    }

    val nutriments = prod.nutriments
    if (nutriments != null) {
        sb.append("Nutrition Facts:\n")
        nutriments.energyKcal?.let { sb.append("Calories: ${it.toInt()} kcal\n") }
        nutriments.sugars?.let { sb.append("Sugars: $it g\n") }
        nutriments.addedSugars?.let { sb.append("Added Sugars: $it g\n") }
        nutriments.sodium?.let {
            // Open Food Facts sodium is often in grams; convert to mg if < 10
            val mg = if (it < 10.0) (it * 1000).toInt() else it.toInt()
            sb.append("Sodium: $mg mg\n")
        }
        nutriments.saturatedFat?.let { sb.append("Saturated Fat: $it g\n") }
        nutriments.proteins?.let { sb.append("Protein: $it g\n") }
        nutriments.fiber?.let { sb.append("Dietary Fiber: $it g\n") }
    }

    return sb.toString().trim()
}
