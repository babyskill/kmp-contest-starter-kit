@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.labellens.app.data.repository

import com.labellens.app.data.source.local.dao.ScanHistoryDao
import com.labellens.app.data.source.local.entity.ScanHistoryEntity
import com.labellens.app.data.source.local.entity.toAnalysisResult
import com.labellens.app.domain.analyzer.LabelAnalyzer
import com.labellens.app.domain.model.LabelAnalysisResult
import com.labellens.app.domain.model.UserProfile
import com.labellens.app.util.extensions.nowEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ScanRepository {
    fun getAllScans(): Flow<List<LabelAnalysisResult>>
    fun getRecentScans(limit: Int = 5): Flow<List<LabelAnalysisResult>>
    fun getFavoriteScans(): Flow<List<LabelAnalysisResult>>
    suspend fun getScanById(id: String): LabelAnalysisResult?
    suspend fun getScanByIdReanalyzed(id: String, profile: UserProfile): LabelAnalysisResult?
    suspend fun saveScan(result: LabelAnalysisResult): String
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun setFeedback(id: String, feedback: String)
    suspend fun deleteScan(id: String)
    suspend fun getPhotoAnalysisCount(): Int
    suspend fun incrementPhotoAnalysisCount()
}

@OptIn(ExperimentalUuidApi::class)
class ScanRepositoryImpl(
    private val scanHistoryDao: ScanHistoryDao,
    private val analyzer: LabelAnalyzer,
    private val userPreferences: com.labellens.app.data.source.preferences.UserPreferences,
) : ScanRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    override fun getAllScans(): Flow<List<LabelAnalysisResult>> = scanHistoryDao.getAllFlow().map { entities -> entities.map { it.toAnalysisResult() } }

    override fun getRecentScans(limit: Int): Flow<List<LabelAnalysisResult>> = scanHistoryDao.getRecentFlow(limit).map { entities -> entities.map { it.toAnalysisResult() } }

    override fun getFavoriteScans(): Flow<List<LabelAnalysisResult>> = scanHistoryDao.getFavoritesFlow().map { entities -> entities.map { it.toAnalysisResult() } }

    override suspend fun getScanById(id: String): LabelAnalysisResult? = (scanHistoryDao.getById(id) ?: scanHistoryDao.getByBarcode(id))?.toAnalysisResult()

    override suspend fun getScanByIdReanalyzed(id: String, profile: UserProfile): LabelAnalysisResult? {
        val entity = scanHistoryDao.getById(id) ?: scanHistoryDao.getByBarcode(id) ?: return null
        val current = entity.toAnalysisResult()
        val langCode = userPreferences.getString(com.labellens.app.data.source.preferences.UserPreferences.KEY_APP_LANGUAGE, null)
        val lang = com.labellens.app.domain.model.AppLanguage.fromCode(langCode)
        // Re-analyze with current profile to update personal flags
        val reanalyzed = analyzer.analyze(
            productName = current.productName,
            rawLabelText = current.rawInputText,
            userProfile = profile,
            category = current.category,
            scanSource = current.scanSource,
            barcode = current.barcode,
            sourceAttribution = current.sourceAttribution,
            language = lang,
        )
        return reanalyzed.copy(id = entity.id)
    }

    override suspend fun saveScan(result: LabelAnalysisResult): String {
        val existing = if (!result.id.isNullOrBlank()) {
            scanHistoryDao.getById(result.id)
        } else if (!result.barcode.isNullOrBlank()) {
            scanHistoryDao.getByBarcode(result.barcode)
        } else {
            scanHistoryDao.getByRawText(result.rawInputText)
        }

        val idToUse = if (!result.id.isNullOrBlank()) {
            result.id
        } else {
            existing?.id ?: result.barcode?.takeIf { it.isNotBlank() } ?: Uuid.random().toString()
        }

        val finalExisting = existing ?: scanHistoryDao.getById(idToUse)

        val entity = ScanHistoryEntity(
            id = idToUse,
            productName = result.productName,
            category = result.category.name,
            source = result.scanSource.name,
            verdict = result.verdict,
            rawLabelText = result.rawInputText,
            analysisJson = json.encodeToString(result.copy(id = idToUse)),
            barcode = result.barcode,
            timestamp = finalExisting?.timestamp ?: nowEpochMillis(),
            isFavorite = finalExisting?.isFavorite ?: false,
            feedbackState = finalExisting?.feedbackState,
        )
        scanHistoryDao.upsert(entity)
        return idToUse
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        scanHistoryDao.setFavorite(id, isFavorite)
        scanHistoryDao.getByBarcode(id)?.let { scanHistoryDao.setFavorite(it.id, isFavorite) }
    }

    override suspend fun setFeedback(id: String, feedback: String) {
        scanHistoryDao.setFeedback(id, feedback)
        scanHistoryDao.getByBarcode(id)?.let { scanHistoryDao.setFeedback(it.id, feedback) }
    }

    override suspend fun deleteScan(id: String) {
        scanHistoryDao.deleteById(id)
        scanHistoryDao.getByBarcode(id)?.let { scanHistoryDao.delete(it) }
    }

    override suspend fun getPhotoAnalysisCount(): Int = userPreferences.getInt("KEY_PHOTO_ANALYSIS_COUNT", 0) ?: 0

    override suspend fun incrementPhotoAnalysisCount() {
        val current = getPhotoAnalysisCount()
        userPreferences.putInt("KEY_PHOTO_ANALYSIS_COUNT", current + 1)
    }
}
