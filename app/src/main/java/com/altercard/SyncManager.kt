package com.altercard

import android.content.Intent
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException

private const val TAG = "SyncManager"

sealed class SyncResult {
    object Success : SyncResult()
    data class NeedsPermission(val intent: Intent) : SyncResult()
    data class Failure(val message: String, val cause: Throwable? = null) : SyncResult()
}

class SyncManager(
    private val cardRepository: CardRepository,
    private val driveRepository: DriveRepository
) {

    suspend fun autoUpload(allCards: List<Card>) {
        try {
            driveRepository.upload(allCards)
            Log.d(TAG, "Auto-upload succeeded (${allCards.size} cards)")
        } catch (e: UserRecoverableAuthIOException) {
            Log.w(TAG, "Auto-upload skipped — Drive permission not yet granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Auto-upload failed — non-fatal", e)
        }
    }

    suspend fun restoreFromDrive(): SyncResult {
        return try {
            val remoteCards = driveRepository.download() ?: return SyncResult.Success
            for (card in remoteCards) {
                cardRepository.upsert(card)
            }
            Log.d(TAG, "Restored ${remoteCards.size} cards from Drive")
            SyncResult.Success
        } catch (e: UserRecoverableAuthIOException) {
            Log.w(TAG, "Drive permission required", e)
            SyncResult.NeedsPermission(e.intent)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            SyncResult.Failure("${e.javaClass.simpleName}: ${e.localizedMessage}", e)
        }
    }

    suspend fun manualSync(localCards: List<Card>): SyncResult {
        return try {
            val remoteCards = driveRepository.download()
            if (remoteCards == null) {
                driveRepository.upload(localCards)
                return SyncResult.Success
            }
            val merged = merge(localCards, remoteCards)
            applyMergeToDb(localCards, merged)
            driveRepository.upload(merged)
            Log.d(TAG, "Manual sync succeeded — ${merged.size} cards after merge")
            SyncResult.Success
        } catch (e: UserRecoverableAuthIOException) {
            Log.w(TAG, "Drive permission required", e)
            SyncResult.NeedsPermission(e.intent)
        } catch (e: Exception) {
            Log.e(TAG, "Manual sync failed", e)
            SyncResult.Failure("${e.javaClass.simpleName}: ${e.localizedMessage}", e)
        }
    }

    internal fun merge(local: List<Card>, remote: List<Card>): List<Card> {
        val byId = mutableMapOf<Int, Card>()
        for (card in local) byId[card.id] = card
        for (card in remote) {
            val existing = byId[card.id]
            if (existing == null || card.lastModified > existing.lastModified) {
                byId[card.id] = card
            }
        }
        return byId.values.toList()
    }

    private suspend fun applyMergeToDb(localCards: List<Card>, merged: List<Card>) {
        val localById = localCards.associateBy { it.id }
        for (card in merged) {
            val local = localById[card.id]
            if (local == null) {
                cardRepository.upsert(card)
            } else if (card.lastModified > local.lastModified) {
                cardRepository.upsert(card)
            }
        }
    }
}
