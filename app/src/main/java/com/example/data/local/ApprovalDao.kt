package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApprovalStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ApprovalDao {
    @Query("SELECT * FROM hitl_approvals ORDER BY createdAt DESC")
    fun getAllApprovalsFlow(): Flow<List<HITLApprovalEntity>>

    @Query("SELECT * FROM hitl_approvals WHERE tenantId = :tenantId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingApprovalsByTenantFlow(tenantId: String): Flow<List<HITLApprovalEntity>>

    @Query("SELECT * FROM hitl_approvals WHERE approvalId = :approvalId")
    suspend fun getApprovalById(approvalId: String): HITLApprovalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: HITLApprovalEntity)

    @Update
    suspend fun updateApproval(approval: HITLApprovalEntity)

    @Query("SELECT COUNT(*) FROM hitl_approvals WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>
}
