package com.albumstore.todo.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.albumstore.todo.data.collection.CollectionItem
import com.albumstore.todo.data.collection.CollectionItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class CollectionUiState(
    val items: List<CollectionItem> = emptyList(),
    val fetching: Boolean = false,
    val error: String? = null
)

class CollectionViewModel(
    private val collectionRepository: CollectionItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState

    init {
        fetchCollectionItems(online = true) // Fetch items initially
    }

    /**
     * Fetch collection items from the repository.
     * @param online Fetch from the online API or local database based on this flag.
     */
    fun fetchCollectionItems(online: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                _uiState.value = _uiState.value.copy(fetching = true, error = null)
                println("Fetching collection items from ${if (online) "online" else "local"}")
                try {
                    val items = collectionRepository.fetchCollectionItems(online)
                    _uiState.value = _uiState.value.copy(items = items, fetching = false)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        fetching = false,
                        error = e.localizedMessage ?: "Failed to fetch collection items"
                    )
                }
            }
        }
    }

    /**
     * Add a new collection item.
     * @param item The collection item to be added.
     */
    fun addCollectionItem(item: CollectionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    collectionRepository.createCollectionItem(item, online = true)
                    println("Collection item added successfully.")
                    val currentItems = _uiState.value.items.toMutableList()
                    item.id = "00000000-0000-0000-0000-000000000000"
                    currentItems.add(item) // Add the new item directly to the list
                    _uiState.value = _uiState.value.copy(items = currentItems)
                } catch (e: Exception) {
                    println("Failed to add collection item: ${e.localizedMessage}")
                    _uiState.value = _uiState.value.copy(
                        error = e.localizedMessage ?: "Failed to add collection item"
                    )
                }
            }
        }
    }

    /**
     * Delete a collection item by its ID.
     * @param id The ID of the collection item to delete.
     */
    fun deleteCollectionItemById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    collectionRepository.deleteCollectionItem(id, online = true)
                    val updatedItems = _uiState.value.items.toMutableList().apply {
                        removeAll { it.id == id }
                    }
                    _uiState.value = _uiState.value.copy(items = updatedItems)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.localizedMessage ?: "Failed to delete collection item"
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Factory for creating an instance of `CollectionViewModel` with a repository.
         * @param repository The `CollectionItemRepository` to use.
         */
        fun Factory(repository: CollectionItemRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(CollectionViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return CollectionViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}
