package com.labellens.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SampleProduct(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val frontDescription: String,
    val fullLabelText: String,
    val barcode: String,
)

object BuiltInSamples {
    val sample1 = SampleProduct(
        id = "sample_honey_oats",
        name = "Honey Oat Cereal",
        category = ProductCategory.FOOD,
        frontDescription = "Crispy toasted whole grain oat clusters drizzled with pure honey and golden sugar.",
        fullLabelText = """
            INGREDIENTS: Whole Grain Oats, Sugar, Honey, Canola Oil, Sea Salt, Molasses, Tocopherols (Vitamin E) added to retain freshness.
            CONTAINS: May contain Wheat.
            Nutrition Facts: Serving size 1 cup (40g). Calories: 160. Total Fat 3g. Saturated Fat 0.5g. Sodium: 140mg. Total Carbohydrate 32g. Dietary Fiber: 3g. Total Sugar: 9g. Protein: 4g.
        """.trimIndent(),
        barcode = "016000275263",
    )

    val sample2 = SampleProduct(
        id = "sample_seaweed",
        name = "Roasted Seaweed Snack",
        category = ProductCategory.FOOD,
        frontDescription = "Korean imported crispy seasoned laver roasted with sesame oil and sea salt.",
        fullLabelText = """
            원재료명 및 함량: 마른김 (Seaweed) 65%, 카놀라유 (Canola Oil) 20%, 참기름 (Sesame Oil) 10%, 천일염 (Sea Salt) 5%.
            INGREDIENTS: Seaweed (Laver), Canola Oil, Roasted Sesame Oil, Sea Salt, Rosemary Extract.
            알레르기 정보: 참깨 (Sesame) 함유.
            영양정보: 총 내용량 5g (25 kcal). 나트륨 (Sodium): 45mg. 탄수화물 1g. 당류: 0g. 지방 2g. 포화지방: 0.2g. 단백질: 1g.
        """.trimIndent(),
        barcode = "8801043014853",
    )

    val sample3 = SampleProduct(
        id = "sample_matcha_biscuits",
        name = "Matcha Sandwich Biscuits",
        category = ProductCategory.FOOD,
        frontDescription = "Japanese imported crisp biscuits with rich Uji green tea matcha cream filling.",
        fullLabelText = """
            原材料名: 小麦粉 (Wheat Flour), 砂糖 (Sugar), パーム油 (Palm Oil), 全粉乳 (Whole Milk Powder), 抹茶 (Matcha Green Tea Powder), 膨脹剤 (Sodium Bicarbonate), 大豆レシチン (Soy Lecithin), 香料.
            INGREDIENTS: Wheat Flour, Sugar, Palm Oil, Whole Milk Powder, Matcha Powder, Sodium Bicarbonate, Soy Lecithin, Vanillin.
            アレルギー物質: 小麦, 乳成分, 大豆 (Contains Wheat, Milk, Soy).
            栄養成分表示: 1パック当たり 熱量 (Calories): 145kcal. たんぱく質 (Protein): 2.1g. 脂質 6.8g (飽和脂肪酸 (Saturated Fat): 3.5g). 炭水化物 18.5g (糖類 (Sugars): 8.2g). 食塩相当量 (Sodium): 75mg.
        """.trimIndent(),
        barcode = "4901360341758",
    )

    val sample4 = SampleProduct(
        id = "sample_vitamin_d3",
        name = "Vitamin D3 1000 IU",
        category = ProductCategory.SUPPLEMENT,
        frontDescription = "High-potency essential vitamin supplement supporting bone density and immune function.",
        fullLabelText = """
            SUPPLEMENT FACTS: Serving Size 1 Softgel.
            Amount Per Softgel: Vitamin D3 (as Cholecalciferol) 25 mcg (1000 IU).
            OTHER INGREDIENTS: Extra Virgin Olive Oil, Gelatin (Bovine), Glycerin, Purified Water.
            DIETARY DISCLAIMER: Education only - this is not medical advice.
            Contains no artificial colors, artificial flavors, yeast or gluten.
        """.trimIndent(),
        barcode = "020525117382",
    )

    val sample5 = SampleProduct(
        id = "sample_cola_zero",
        name = "Cola Zero Sugar",
        category = ProductCategory.BEVERAGE,
        frontDescription = "Zero-calorie effervescent cola flavored with aromatic extracts and sweeteners.",
        fullLabelText = """
            INGREDIENTS: Carbonated Purified Water, Caramel Color, Phosphoric Acid, Aspartame, Potassium Benzoate (preserves freshness), Natural Flavors, Acesulfame Potassium, Caffeine, Citric Acid.
            PHENYLKETONURICS: CONTAINS PHENYLALANINE.
            Nutrition Facts: Serving size 1 can (355ml). Calories: 0. Total Fat 0g. Sodium: 40mg. Total Carbohydrate 0g. Sugars: 0g. Protein: 0g.
        """.trimIndent(),
        barcode = "049000042566",
    )

    val sample6 = SampleProduct(
        id = "sample_shrimp_noodles",
        name = "Instant Noodles, Shrimp Flavor",
        category = ProductCategory.FOOD,
        frontDescription = "Thai imported savory noodle soup with spicy Tom Yum shrimp broth notes.",
        fullLabelText = """
            INGREDIENTS:
            Noodles: Wheat Flour, Palm Oil, Tapioca Starch, Salt, Potassium Carbonate, Sodium Tripolyphosphate.
            Seasoning Paste & Powder: Salt, Sugar, Monosodium Glutamate (MSG, E621), Shrimp Powder (Crustacean), Disodium Inosinate & Guanylate (E635), Chili Powder, Citric Acid, Garlic, Lemongrass, Paprika Oleoresin.
            CONTAINS: Wheat, Crustacean (Shrimp), Soy.
            Nutrition Facts: Serving size 1 package (70g). Calories: 310. Total Fat 13g. Saturated Fat: 6g. Sodium: 1480mg. Total Carbohydrate 42g. Dietary Fiber 2g. Total Sugar: 3g. Protein: 6g.
        """.trimIndent(),
        barcode = "8850987101051",
    )

    val sample7 = SampleProduct(
        id = "sample_chewy_granola_bars",
        name = "Chewy Chocolate Chip Granola Bars",
        category = ProductCategory.FOOD,
        frontDescription = "Whole grain rolled oat granola bars drizzled with chocolate chips, pure honey, and golden molasses.",
        fullLabelText = """
            INGREDIENTS: Whole Grain Rolled Oats, Cane Sugar, Corn Syrup, Semi-Sweet Chocolate Chips (Sugar, Chocolate Liquor, Cocoa Butter, Soy Lecithin), Inulin (Chicory Root Fiber), Brown Rice Flour, Canola Oil. Contains 2% or less of: Honey, Molasses, Salt, Baking Soda, Natural Flavors.
            CONTAINS: Soy. May contain Peanuts, Tree Nuts, and Milk.
            Nutrition Facts: 2.5 servings per container. Serving size 1 bar (24g). Calories: 100. Total Fat 3g. Saturated Fat 1g. Sodium: 75mg. Total Carbohydrate 19g. Dietary Fiber 3g. Total Sugars 7g. Includes 6g Added Sugars. Protein 1g.
        """.trimIndent(),
        barcode = "016000264625",
    )

    val all: List<SampleProduct> = listOf(sample1, sample2, sample3, sample4, sample5, sample6, sample7)
}
