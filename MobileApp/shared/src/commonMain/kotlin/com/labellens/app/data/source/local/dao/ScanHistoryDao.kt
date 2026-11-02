package com.labellens.app.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.labellens.app.data.source.local.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentFlow(limit: Int): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE is_favorite = 1 ORDER BY timestamp DESC")
    fun getFavoritesFlow(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getById(id: String): ScanHistoryEntity?

    @Query("SELECT * FROM scan_history WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): ScanHistoryEntity?

    @Query("SELECT * FROM scan_history WHERE raw_label_text = :rawText LIMIT 1")
    suspend fun getByRawText(rawText: String): ScanHistoryEntity?

    @Upsert
    suspend fun upsert(entity: ScanHistoryEntity)

    @Query("UPDATE scan_history SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE scan_history SET feedback_state = :feedback WHERE id = :id")
    suspend fun setFeedback(id: String, feedback: String)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(entity: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()
}
