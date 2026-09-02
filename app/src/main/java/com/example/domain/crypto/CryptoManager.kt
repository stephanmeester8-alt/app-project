package com.example.domain.crypto

import com.example.data.local.AuditRecordEntity
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    const val GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"
    private const val DEFAULT_SECRET_KEY = "aivaults-enterprise-hmac-master-key-prod"

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hmacSha256(data: String, key: String = DEFAULT_SECRET_KEY): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun calculateAuditHash(
        logId: String,
        timestamp: String,
        tenantId: String,
        actorId: String,
        toolName: String,
        policyVerdict: String,
        inputHash: String,
        previousLogHash: String
    ): String {
        val payload = "$logId:$timestamp:$tenantId:$actorId:$toolName:$policyVerdict:$inputHash:$previousLogHash"
        return sha256(payload)
    }

    /**
     * Verifies the cryptographic WORM integrity across the audit ledger.
     * Returns true if all SHA-256 block links and hashes are unmodified.
     */
    fun verifyAuditChain(records: List<AuditRecordEntity>): Boolean {
        if (records.isEmpty()) return true
        var prevHash = GENESIS_HASH

        // Sort ascending by ID / timestamp for chain verification
        val sorted = records.sortedBy { it.id }
        for (record in sorted) {
            if (record.previousLogHash != prevHash) {
                return false
            }
            val calculated = calculateAuditHash(
                logId = record.logId,
                timestamp = record.timestamp,
                tenantId = record.tenantId,
                actorId = record.actorId,
                toolName = record.toolName,
                policyVerdict = record.policyVerdict,
                inputHash = record.inputHash,
                previousLogHash = record.previousLogHash
            )
            if (calculated != record.currentHash) {
                return false
            }
            prevHash = record.currentHash
        }
        return true
    }

    /**
     * Finds the index of the first corrupted or tampered record, if any.
     */
    fun findCorruptedRecordIndex(records: List<AuditRecordEntity>): Int? {
        if (records.isEmpty()) return null
        var prevHash = GENESIS_HASH
        val sorted = records.sortedBy { it.id }

        for ((index, record) in sorted.withIndex()) {
            if (record.previousLogHash != prevHash) {
                return index
            }
            val calculated = calculateAuditHash(
                logId = record.logId,
                timestamp = record.timestamp,
                tenantId = record.tenantId,
                actorId = record.actorId,
                toolName = record.toolName,
                policyVerdict = record.policyVerdict,
                inputHash = record.inputHash,
                previousLogHash = record.previousLogHash
            )
            if (calculated != record.currentHash) {
                return index
            }
            prevHash = record.currentHash
        }
        return null
    }
}
