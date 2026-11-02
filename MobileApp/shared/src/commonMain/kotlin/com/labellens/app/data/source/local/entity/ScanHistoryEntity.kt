package com.labellens.app.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.ProductCategory
import com.labellens.app.domain.model.ScanSource
import kotlinx.serialization.json.Json

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "verdict")
    val verdict: String,
    @ColumnInfo(name = "raw_label_text")
    val rawLabelText: String,
    @ColumnInfo(name = "analysis_json")
    val analysisJson: String,
    @ColumnInfo(name = "barcode")
    val barcode: String? = null,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = 0L,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "feedback_state")
    val feedbackState: String? = null, // e.g. "accurate", "inaccurate", or field feedback json
)

private val json = Json { ignoreUnknownKeys = true }

fun ScanHistoryEntity.toAnalysisResult(): LabelAnalysisResult = try {
    json.decodeFromString<LabelAnalysisResult>(analysisJson).copy(id = id)
} catch (e: Exception) {
    LabelAnalysisResult(
        id = id,
        productName = productName,
        category = try {
            ProductCategory.valueOf(category)
        } catch (e: Exception) {
            ProductCategory.FOOD
        },
        scanSource = try {
            ScanSource.valueOf(source)
        } catch (e: Exception) {
            ScanSource.SAMPLE
        },
        verdict = verdict,
        rawInputText = rawLabelText,
        barcode = barcode,
    )
}
