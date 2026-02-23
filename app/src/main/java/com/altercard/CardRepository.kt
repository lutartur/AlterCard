package com.altercard

import kotlinx.coroutines.flow.Flow

class CardRepository(private val cardDao: CardDao) {

    val allCards: Flow<List<Card>> = cardDao.getAllCards()

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
