package com.labellens.app.data.source.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.labellens.app.data.source.local.dao.CreditTransactionDao
import com.labellens.app.data.source.local.dao.ExampleDao
import com.labellens.app.data.source.local.dao.GenerationOutputDao
import com.labellens.app.data.source.local.entity.CreditTransactionEntity
import com.labellens.app.data.source.local.entity.ExampleEntity
import com.labellens.app.data.source.local.entity.GenerationOutputEntity

// The app's Room 3 database (lives in commonMain). Bump `version` and add a Migration when you
// change the schema after shipping; otherwise DatabaseModule drops all tables on schema change.
@Database(
    entities = [GenerationOutputEntity::class, CreditTransactionEntity::class, ExampleEntity::class, com.labellens.app.data.source.local.entity.ScanHistoryEntity::class],
    version = 2,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun generationOutputDao(): GenerationOutputDao
    abstract fun creditTransactionDao(): CreditTransactionDao
    abstract fun exampleDao(): ExampleDao
    abstract fun scanHistoryDao(): com.labellens.app.data.source.local.dao.ScanHistoryDao

    // Add new DAOs above — make_local.sh inserts here.
}

// Each platform supplies a DatabaseProvider actual that picks the right SQLite driver
// (bundled native on Android/iOS/JVM, web-worker on js/wasmJs).
interface DatabaseProvider {
    fun provideAppDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
