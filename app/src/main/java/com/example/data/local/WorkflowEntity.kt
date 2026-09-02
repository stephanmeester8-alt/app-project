package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.WorkflowState

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey
    val workflowId: String,
    val tenantId: String,
    val targetDomain: String,
    val state: WorkflowState,
    val qualificationScore: Int = 0,
    val aiDetected: Boolean = false,
    val aiProviders: List<String> = emptyList(),
    val headings: List<String> = emptyList(),
    val payloadSizeKb: Double = 0.0,
    val contactEmails: List<String> = emptyList(),
    val contactPhones: List<String> = emptyList(),
    val hasContactForm: Boolean = false,
    val draftSubject: String? = null,
    val draftBody: String? = null,
    val pendingApprovalId: String? = null,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
