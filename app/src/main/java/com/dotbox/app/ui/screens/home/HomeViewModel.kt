package com.dotbox.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotbox.app.data.model.ToolCategory
import com.dotbox.app.data.model.ToolId
import com.dotbox.app.data.repository.ToolsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val tools: List<ToolId> = ToolId.entries,
    val favoriteIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedCategory: ToolCategory? = null,
    val isSearchActive: Boolean = false,
)

class HomeViewModel(private val repository: ToolsRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ToolCategory?>(null)
    val selectedCategory: StateFlow<ToolCategory?> = _selectedCategory.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        _searchQuery,
        _selectedCategory,
        repository.favoriteIds,
        _isSearchActive,
    ) { query, category, favIds, searchActive ->
        val filteredTools = when {
            query.isNotBlank() -> repository.searchTools(query)
            category != null -> repository.getToolsByCategory(category)
            else -> repository.allTools
        }
        HomeUiState(
            tools = filteredTools,
            favoriteIds = favIds.toSet(),
            searchQuery = query,
            selectedCategory = category,
            isSearchActive = searchActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _selectedCategory.value = null
        }
    }

    fun onSearchActiveChanged(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun onCategorySelected(category: ToolCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
        _searchQuery.value = ""
        _isSearchActive.value = false
    }

    fun toggleFavorite(toolId: ToolId) {
        viewModelScope.launch {
            val currentFavs = uiState.value.favoriteIds
            if (toolId.name in currentFavs) {
                repository.removeFavorite(toolId)
            } else {
                repository.addFavorite(toolId)
            }
        }
    }

    companion object {
        fun factory(repository: ToolsRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}
