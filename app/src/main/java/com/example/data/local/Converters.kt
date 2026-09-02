package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ApprovalStatus
import com.example.data.model.PolicyVerdict
import com.example.data.model.RiskLevel
import com.example.data.model.TenantRole
import com.example.data.model.ToolActionType
import com.example.data.model.WorkflowState

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return list?.joinToString("||") ?: ""
    }

    @TypeConverter
    fun toStringList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split("||").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromTenantRole(role: TenantRole?): String = role?.name ?: TenantRole.OPERATOR.name

    @TypeConverter
    fun toTenantRole(value: String?): TenantRole =
        value?.let { runCatching { TenantRole.valueOf(it) }.getOrNull() } ?: TenantRole.OPERATOR

    @TypeConverter
    fun fromRiskLevel(risk: RiskLevel?): String = risk?.name ?: RiskLevel.LOW.name

    @TypeConverter
    fun toRiskLevel(value: String?): RiskLevel =
        value?.let { runCatching { RiskLevel.valueOf(it) }.getOrNull() } ?: RiskLevel.LOW

    @TypeConverter
    fun fromToolActionType(action: ToolActionType?): String = action?.name ?: ToolActionType.READ.name

    @TypeConverter
    fun toToolActionType(value: String?): ToolActionType =
        value?.let { runCatching { ToolActionType.valueOf(it) }.getOrNull() } ?: ToolActionType.READ

    @TypeConverter
    fun fromWorkflowState(state: WorkflowState?): String = state?.name ?: WorkflowState.INITIALIZED.name

    @TypeConverter
    fun toWorkflowState(value: String?): WorkflowState =
        value?.let { runCatching { WorkflowState.valueOf(it) }.getOrNull() } ?: WorkflowState.INITIALIZED

    @TypeConverter
    fun fromPolicyVerdict(verdict: PolicyVerdict?): String = verdict?.name ?: PolicyVerdict.PASS.name

    @TypeConverter
    fun toPolicyVerdict(value: String?): PolicyVerdict =
        value?.let { runCatching { PolicyVerdict.valueOf(it) }.getOrNull() } ?: PolicyVerdict.PASS

    @TypeConverter
    fun fromApprovalStatus(status: ApprovalStatus?): String = status?.name ?: ApprovalStatus.PENDING.name

    @TypeConverter
    fun toApprovalStatus(value: String?): ApprovalStatus =
        value?.let { runCatching { ApprovalStatus.valueOf(it) }.getOrNull() } ?: ApprovalStatus.PENDING
}
