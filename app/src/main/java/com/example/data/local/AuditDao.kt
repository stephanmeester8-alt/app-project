package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_records ORDER BY id ASC")
    fun getAllRecordsFlow(): Flow<List<AuditRecordEntity>>

    @Query("SELECT * FROM audit_records ORDER BY id ASC")
    suspend fun getAllRecords(): List<AuditRecordEntity>

    @Query("SELECT * FROM audit_records WHERE tenantId = :tenantId ORDER BY id DESC")
    fun getRecordsByTenantFlow(tenantId: String): Flow<List<AuditRecordEntity>>

    @Query("SELECT * FROM audit_records ORDER BY id DESC LIMIT 1")
    suspend fun getLastRecord(): AuditRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AuditRecordEntity): Long

    @Update
    suspend fun updateRecord(record: AuditRecordEntity)

    @Query("DELETE FROM audit_records")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM audit_records")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM audit_records WHERE policyVerdict = 'BLOCKED_BY_POLICY'")
    fun getBlockedAttemptsCountFlow(): Flow<Int>
}
