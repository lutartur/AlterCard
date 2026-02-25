package com.altercard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardRepository(private val cardDao: CardDao) {

    val allCards: Flow<List<Card>> = cardDao.getAllCards()
        .map { cards -> cards.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }) }

    fun getCard(id: Int): Flow<Card?> {
        return cardDao.getCard(id)
    }

    suspend fun insert(card: Card) {
        cardDao.insert(card)
    }

    suspend fun update(card: Card) {
        cardDao.update(card)
    }

    suspend fun delete(card: Card) {
        cardDao.delete(card)
    }
}
