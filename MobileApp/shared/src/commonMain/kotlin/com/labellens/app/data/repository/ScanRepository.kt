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

    override suspend fun getScanById(id: String): LabelAnalysisResult? = scanHistoryDao.getById(id)?.toAnalysisResult()

    override suspend fun getScanByIdReanalyzed(id: String, profile: UserProfile): LabelAnalysisResult? {
        val entity = scanHistoryDao.getById(id) ?: return null
        val current = entity.toAnalysisResult()
        // Re-analyze with current profile to update personal flags
        return analyzer.analyze(
            productName = current.productName,
            rawLabelText = current.rawInputText,
            userProfile = profile,
            category = current.category,
            scanSource = current.scanSource,
            barcode = current.barcode,
        )
    }

    override suspend fun saveScan(result: LabelAnalysisResult): String {
        val id = result.barcode?.takeIf { it.isNotBlank() } ?: Uuid.random().toString()
        val entity = ScanHistoryEntity(
            id = id,
            productName = result.productName,
            category = result.category.name,
            source = result.scanSource.name,
            verdict = result.verdict,
            rawLabelText = result.rawInputText,
            analysisJson = json.encodeToString(result),
            barcode = result.barcode,
            timestamp = nowEpochMillis(),
            isFavorite = false,
        )
        scanHistoryDao.upsert(entity)
        return id
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        scanHistoryDao.setFavorite(id, isFavorite)
    }

    override suspend fun setFeedback(id: String, feedback: String) {
        scanHistoryDao.setFeedback(id, feedback)
    }

    override suspend fun deleteScan(id: String) {
        scanHistoryDao.deleteById(id)
    }

    override suspend fun getPhotoAnalysisCount(): Int = userPreferences.getInt("KEY_PHOTO_ANALYSIS_COUNT", 0) ?: 0

    override suspend fun incrementPhotoAnalysisCount() {
        val current = getPhotoAnalysisCount()
        userPreferences.putInt("KEY_PHOTO_ANALYSIS_COUNT", current + 1)
    }
}
