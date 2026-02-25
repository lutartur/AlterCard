package com.altercard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class NeedsPermission(val intent: android.content.Intent) : SyncState()
    data class Error(val message: String) : SyncState()
}

class CardViewModel(
    private val repository: CardRepository,
    private val application: AltercardApplication
) : ViewModel() {

    val allCards: LiveData<List<Card>> = repository.allCards.asLiveData()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun getCard(id: Int): LiveData<Card?> = repository.getCard(id).asLiveData()

    fun insert(card: Card) = viewModelScope.launch { repository.insert(card) }
    fun update(card: Card) = viewModelScope.launch { repository.update(card) }
    fun delete(card: Card) = viewModelScope.launch { repository.delete(card) }

    fun restoreFromDrive() {
        val syncManager = application.buildSyncManager() ?: return
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val result = syncManager.restoreFromDrive()
            _syncState.value = when (result) {
                is SyncResult.Success -> SyncState.Success
                is SyncResult.NeedsPermission -> SyncState.NeedsPermission(result.intent)
                is SyncResult.Failure -> SyncState.Error(result.message)
            }
        }
    }

    fun manualSync() {
        val syncManager = application.buildSyncManager() ?: run {
            _syncState.value = SyncState.Error("Not signed in to Google")
            return
        }
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val localCards = repository.allCardsForSync.first()
            val result = syncManager.manualSync(localCards)
            _syncState.value = when (result) {
                is SyncResult.Success -> SyncState.Success
                is SyncResult.NeedsPermission -> SyncState.NeedsPermission(result.intent)
                is SyncResult.Failure -> SyncState.Error(result.message)
            }
        }
    }

    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}

class CardViewModelFactory(
    private val repository: CardRepository,
    private val application: AltercardApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
