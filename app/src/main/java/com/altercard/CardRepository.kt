package com.altercard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CardRepository(private val cardDao: CardDao) {

    @Volatile
    var syncManager: SyncManager? = null

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allCards: Flow<List<Card>> = cardDao.getAllCards()
        .map { cards -> cards.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }) }

    fun getCard(id: Int): Flow<Card?> = cardDao.getCard(id)

    suspend fun insert(card: Card) {
        val stamped = card.copy(lastModified = System.currentTimeMillis())
        cardDao.insert(stamped)
        triggerAutoUpload()
    }

    suspend fun update(card: Card) {
        val stamped = card.copy(lastModified = System.currentTimeMillis())
        cardDao.update(stamped)
        triggerAutoUpload()
    }

    suspend fun delete(card: Card) {
        cardDao.delete(card)
        triggerAutoUpload()
    }

    suspend fun upsert(card: Card) {
        cardDao.upsert(card)
    }

    private fun triggerAutoUpload() {
        val sm = syncManager ?: return
        repoScope.launch {
            val currentCards = allCards.first()
            sm.autoUpload(currentCards)
        }
    }
}
