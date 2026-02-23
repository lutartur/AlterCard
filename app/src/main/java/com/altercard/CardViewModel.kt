package com.altercard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CardViewModel(private val repository: CardRepository) : ViewModel() {

    val allCards: LiveData<List<Card>> = repository.allCards.asLiveData()

    fun getCard(id: Int): LiveData<Card?> {
        return repository.getCard(id).asLiveData()
    }

    fun insert(card: Card) = viewModelScope.launch {
        repository.insert(card)
    }

    fun update(card: Card) = viewModelScope.launch {
        repository.update(card)
    }

    fun delete(card: Card) = viewModelScope.launch {
        repository.delete(card)
    }
}

class CardViewModelFactory(private val repository: CardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
