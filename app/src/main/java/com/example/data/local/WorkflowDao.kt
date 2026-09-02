package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY updatedAt DESC")
    fun getAllWorkflowsFlow(): Flow<List<WorkflowEntity>>

    @Query("SELECT * FROM workflows WHERE workflowId = :workflowId")
    suspend fun getWorkflowById(workflowId: String): WorkflowEntity?

    @Query("SELECT * FROM workflows WHERE workflowId = :workflowId")
    fun getWorkflowByIdFlow(workflowId: String): Flow<WorkflowEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Query("DELETE FROM workflows WHERE workflowId = :workflowId")
    suspend fun deleteWorkflow(workflowId: String)

    @Query("SELECT COUNT(*) FROM workflows WHERE state NOT IN ('COMPLETED', 'FAILED', 'BLOCKED_BY_POLICY')")
    fun getActiveWorkflowsCountFlow(): Flow<Int>
}
