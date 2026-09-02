package com.example.domain.audit

import com.example.data.local.AppDatabase
import com.example.data.local.AuditRecordEntity
import com.example.data.model.PolicyVerdict
import com.example.data.model.ToolActionType
import com.example.domain.crypto.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AuditLogger(private val database: AppDatabase) {
    private val mutex = Mutex()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    suspend fun log(
        tenantId: String,
        actorId: String,
        workflowId: String,
        actionType: ToolActionType,
        toolName: String,
        policyVerdict: PolicyVerdict,
        inputHash: String,
        outputHash: String? = null,
        details: String
    ): AuditRecordEntity = withContext(Dispatchers.IO) {
        mutex.withLock {
            val lastRecord = database.auditDao().getLastRecord()
            val previousLogHash = lastRecord?.currentHash ?: CryptoManager.GENESIS_HASH
            val timestamp = dateFormat.format(Date())
            val logId = "aud-${UUID.randomUUID()}"

            val currentHash = CryptoManager.calculateAuditHash(
                logId = logId,
                timestamp = timestamp,
                tenantId = tenantId,
                actorId = actorId,
                toolName = toolName,
                policyVerdict = policyVerdict.name,
                inputHash = inputHash,
                previousLogHash = previousLogHash
            )

            val record = AuditRecordEntity(
                logId = logId,
                timestamp = timestamp,
                tenantId = tenantId,
                actorId = actorId,
                workflowId = workflowId,
                actionType = actionType.name,
                toolName = toolName,
                policyVerdict = policyVerdict.name,
                inputHash = inputHash,
                outputHash = outputHash,
                previousLogHash = previousLogHash,
                currentHash = currentHash,
                details = details,
                isTampered = false
            )

            val insertedId = database.auditDao().insertRecord(record)
            record.copy(id = insertedId)
        }
    }

    suspend fun verifyIntegrity(): Boolean = withContext(Dispatchers.IO) {
        val records = database.auditDao().getAllRecords()
        CryptoManager.verifyAuditChain(records)
    }

    suspend fun simulateTamperAttack(): Boolean = withContext(Dispatchers.IO) {
        val records = database.auditDao().getAllRecords()
        if (records.isNotEmpty()) {
            val target = records.first()
            val tampered = target.copy(
                details = "FORGED_LOG: Unauthorized administrative privilege escalation injected.",
                isTampered = true
            )
            database.auditDao().updateRecord(tampered)
            true
        } else {
            false
        }
    }

    suspend fun restoreChain(): Unit = withContext(Dispatchers.IO) {
        val records = database.auditDao().getAllRecords()
        var prevHash = CryptoManager.GENESIS_HASH
        for (record in records) {
            val validHash = CryptoManager.calculateAuditHash(
                logId = record.logId,
                timestamp = record.timestamp,
                tenantId = record.tenantId,
                actorId = record.actorId,
                toolName = record.toolName,
                policyVerdict = record.policyVerdict,
                inputHash = record.inputHash,
                previousLogHash = prevHash
            )
            val cleanDetails = if (record.isTampered) "Restored WORM verified ledger block." else record.details
            database.auditDao().updateRecord(
                record.copy(
                    details = cleanDetails,
                    previousLogHash = prevHash,
                    currentHash = validHash,
                    isTampered = false
                )
            )
            prevHash = validHash
        }
    }
}
