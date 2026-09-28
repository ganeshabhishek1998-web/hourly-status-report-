package com.ganeshabhishek.hourlystatusreport.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

@Dao
interface HourlyReportDao {
    @Query("SELECT * FROM hourly_slots WHERE date = :date ORDER BY sortOrder ASC")
    fun observeSlotsForDate(date: String): Flow<List<HourlySlotEntity>>

    @Query("SELECT * FROM hourly_slots WHERE date = :date ORDER BY sortOrder ASC")
    suspend fun getSlotsForDateOnce(date: String): List<HourlySlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlots(slots: List<HourlySlotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(slot: HourlySlotEntity)

    @Query("DELETE FROM hourly_slots WHERE date = :date")
    suspend fun deleteSlotsForDate(date: String)
}

@Database(
    entities = [HourlySlotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ReportDatabase : RoomDatabase() {
    abstract fun hourlyReportDao(): HourlyReportDao

    companion object {
        @Volatile
        private var INSTANCE: ReportDatabase? = null

        fun getInstance(context: Context): ReportDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReportDatabase::class.java,
                    "hourly_status_report.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class HourlyReportRepository(private val dao: HourlyReportDao) {

    fun observeReport(date: String): Flow<DailyHourlyReport> {
        return dao.observeSlotsForDate(date).map { entities ->
            val domainSlots = if (entities.isEmpty()) {
                DefaultHourlySlots.createEntitiesForDate(date).map { it.toDomain() }
            } else {
                entities.map { it.toDomain() }
            }
            val loggedHours = DefaultHourlySlots.calculateLoggedHours(domainSlots)
            DailyHourlyReport(
                id = "rep_$date",
                date = date,
                slots = domainSlots,
                totalLoggedHours = loggedHours,
                targetHours = 12.0,
                updatedAt = Instant.now().toString()
            )
        }
    }

    suspend fun ensureDateInitialized(date: String) {
        val existing = dao.getSlotsForDateOnce(date)
        if (existing.isEmpty()) {
            dao.upsertSlots(DefaultHourlySlots.createEntitiesForDate(date))
        }
    }

    suspend fun updateSlot(
        date: String,
        slotId: String,
        activity: String? = null,
        status: SlotStatus? = null
    ) {
        ensureDateInitialized(date)
        val currentSlots = dao.getSlotsForDateOnce(date)
        val target = currentSlots.find { it.slotId == slotId } ?: return

        val newActivity = activity ?: target.activity
        var newStatusKey = status?.key ?: target.statusKey
        if (activity != null && newActivity.trim().isNotEmpty() && newStatusKey == SlotStatus.PENDING.key) {
            newStatusKey = SlotStatus.COMPLETED.key
        }

        dao.upsertSlot(
            target.copy(
                activity = newActivity,
                statusKey = newStatusKey,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun applyTemplate(date: String) {
        ensureDateInitialized(date)
        val currentSlots = dao.getSlotsForDateOnce(date)
        val now = System.currentTimeMillis()
        val updated = currentSlots.map { entity ->
            val sampleActivity = DefaultHourlySlots.SAMPLE_TEMPLATE_TASKS[entity.slotId] ?: entity.activity
            entity.copy(
                activity = sampleActivity,
                statusKey = SlotStatus.COMPLETED.key,
                updatedAt = now
            )
        }
        dao.upsertSlots(updated)
    }

    suspend fun importReport(date: String, importedSlots: List<ImportedSlotItem>): Int {
        ensureDateInitialized(date)
        val currentSlots = dao.getSlotsForDateOnce(date)
        val now = System.currentTimeMillis()
        var updatedCount = 0

        val normalizeTime: (String) -> String = { raw ->
            raw.lowercase().replace(Regex("[\\s\\u2013\\u2014-]"), "")
        }

        val updatedEntities = currentSlots.mapIndexed { idx, s ->
            val match = importedSlots.find { item ->
                (!item.slotId.isNullOrBlank() && item.slotId == s.slotId) ||
                    (!item.timeRange.isNullOrBlank() &&
                        (item.timeRange.trim().equals(s.timeRange.trim(), ignoreCase = true) ||
                            normalizeTime(item.timeRange) == normalizeTime(s.timeRange)))
            } ?: importedSlots.getOrNull(idx)?.takeIf {
                it.timeRange.isNullOrBlank() && it.slotId.isNullOrBlank()
            }

            if (match != null) {
                val act = match.activity
                var status = SlotStatus.fromKey(match.status ?: s.statusKey)
                if (s.isBreak) {
                    status = SlotStatus.COMPLETED
                } else if (act.trim().isNotEmpty() && status == SlotStatus.PENDING) {
                    status = SlotStatus.COMPLETED
                }
                if (act.trim().isNotEmpty()) {
                    updatedCount++
                }
                s.copy(
                    activity = if (s.isBreak && act.isBlank()) "Lunch break" else act,
                    statusKey = status.key,
                    updatedAt = now
                )
            } else {
                s
            }
        }

        dao.upsertSlots(updatedEntities)
        return updatedCount
    }

    suspend fun resetReport(date: String) {
        dao.deleteSlotsForDate(date)
        dao.upsertSlots(DefaultHourlySlots.createEntitiesForDate(date))
    }
}
