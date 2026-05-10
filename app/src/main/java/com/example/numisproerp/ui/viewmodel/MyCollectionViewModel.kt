package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.CollectionItem
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI стан "Моєї колекції".
 *
 * `allItems` — повний список з БД, незалежно від фільтрів/пошуку.
 * `items` — те, що показується на екрані: відфільтровано, відсортовано і
 * враховує текстовий пошук з `searchQuery`.
 *
 * Списки `categories/materials/qualities/series/nominals` будуються з
 * `allItems` і використовуються в діалогах багатокритерійного фільтру.
 */
data class MyCollectionUiState(
    val allItems: List<CollectionItem> = emptyList(),
    val items: List<CollectionItem> = emptyList(),
    val isLoading: Boolean = false,
    val totalEstimatedValue: Double = 0.0,
    val showAddDialog: Boolean = false,
    val editingItem: CollectionItem? = null,
    val errorMessage: String = "",
    val successMessage: String = "",

    val searchQuery: String = "",
    val sortBy: String = "date_desc",
    val showSortDialog: Boolean = false,
    val showFilterDialog: Boolean = false,
    val filterCategory: String = "",
    val filterMaterial: String = "",
    val filterQuality: String = "",
    val filterSeries: String = "",
    val filterNominal: String = "",
    val categories: List<String> = emptyList(),
    val materials: List<String> = emptyList(),
    val qualities: List<String> = emptyList(),
    val seriesList: List<String> = emptyList(),
    val nominals: List<String> = emptyList()
)

@HiltViewModel
class MyCollectionViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyCollectionUiState())
    val uiState: StateFlow<MyCollectionUiState> = _uiState.asStateFlow()

    init {
        observeCollection()
    }

    private fun observeCollection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAllCollectionItems().collectLatest { items ->
                _uiState.value = _uiState.value.copy(
                    allItems = items,
                    categories = items.map { it.category }.filter { it.isNotBlank() }.distinct().sorted(),
                    materials = items.map { it.material }.filter { it.isNotBlank() }.distinct().sorted(),
                    qualities = items.map { it.quality }.filter { it.isNotBlank() }.distinct().sorted(),
                    seriesList = items.map { it.series }.filter { it.isNotBlank() }.distinct().sorted(),
                    nominals = items.map { it.nominal }.filter { it.isNotBlank() }.distinct().sorted(),
                    isLoading = false
                )
                applyFiltersAndSort()
            }
        }
    }

    private fun applyFiltersAndSort() {
        val s = _uiState.value
        val q = s.searchQuery.trim().lowercase()
        var list = s.allItems

        if (s.filterCategory.isNotEmpty()) list = list.filter { it.category == s.filterCategory }
        if (s.filterMaterial.isNotEmpty()) list = list.filter { it.material == s.filterMaterial }
        if (s.filterQuality.isNotEmpty()) list = list.filter { it.quality == s.filterQuality }
        if (s.filterSeries.isNotEmpty()) list = list.filter { it.series == s.filterSeries }
        if (s.filterNominal.isNotEmpty()) list = list.filter { it.nominal == s.filterNominal }

        if (q.isNotEmpty()) {
            list = list.filter {
                it.name.lowercase().contains(q) ||
                    it.series.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.material.lowercase().contains(q) ||
                    it.nominal.lowercase().contains(q) ||
                    it.quality.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)
            }
        }

        list = when (s.sortBy) {
            "name_asc" -> list.sortedBy { it.name.lowercase() }
            "name_desc" -> list.sortedByDescending { it.name.lowercase() }
            "value_desc" -> list.sortedByDescending { it.estimatedValue }
            "value_asc" -> list.sortedBy { it.estimatedValue }
            "qty_desc" -> list.sortedByDescending { it.quantity }
            "qty_asc" -> list.sortedBy { it.quantity }
            "date_asc" -> list.sortedBy { it.dateAdded }
            "date_desc" -> list.sortedByDescending { it.dateAdded }
            "category" -> list.sortedBy { it.category.lowercase() }
            "material" -> list.sortedBy { it.material.lowercase() }
            else -> list
        }

        val total = list.sumOf { it.estimatedValue * it.quantity }
        _uiState.value = _uiState.value.copy(items = list, totalEstimatedValue = total)
    }

    fun updateSearchQuery(q: String) {
        _uiState.value = _uiState.value.copy(searchQuery = q)
        applyFiltersAndSort()
    }

    fun setSortBy(sortBy: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy, showSortDialog = false)
        applyFiltersAndSort()
    }

    fun toggleSortDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSortDialog = show)
    }

    fun toggleFilterDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showFilterDialog = show)
    }

    fun setFilterCategory(value: String) {
        _uiState.value = _uiState.value.copy(filterCategory = value)
        applyFiltersAndSort()
    }

    fun setFilterMaterial(value: String) {
        _uiState.value = _uiState.value.copy(filterMaterial = value)
        applyFiltersAndSort()
    }

    fun setFilterQuality(value: String) {
        _uiState.value = _uiState.value.copy(filterQuality = value)
        applyFiltersAndSort()
    }

    fun setFilterSeries(value: String) {
        _uiState.value = _uiState.value.copy(filterSeries = value)
        applyFiltersAndSort()
    }

    fun setFilterNominal(value: String) {
        _uiState.value = _uiState.value.copy(filterNominal = value)
        applyFiltersAndSort()
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            filterCategory = "",
            filterMaterial = "",
            filterQuality = "",
            filterSeries = "",
            filterNominal = "",
            searchQuery = ""
        )
        applyFiltersAndSort()
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingItem = null, errorMessage = "")
    }

    fun openEditDialog(item: CollectionItem) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingItem = item, errorMessage = "")
    }

    fun closeDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingItem = null, errorMessage = "")
    }

    fun saveItem(
        name: String,
        series: String,
        category: String,
        material: String,
        nominal: String,
        quality: String,
        description: String,
        photoPath: String,
        estimatedValue: Double,
        quantity: Int,
        sourceUrl: String = ""
    ) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Введіть назву товару")
            return
        }
        if (quantity <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Кількість має бути > 0")
            return
        }

        viewModelScope.launch {
            val editing = _uiState.value.editingItem
            if (editing == null) {
                val newItem = CollectionItem(
                    collectionId = "COL_${UUID.randomUUID().toString().take(8).uppercase()}",
                    name = name.trim(),
                    series = series.trim(),
                    category = category.trim(),
                    material = material.trim(),
                    nominal = nominal.trim(),
                    quality = quality.trim(),
                    description = description.trim(),
                    photoPath = photoPath,
                    estimatedValue = estimatedValue,
                    quantity = quantity,
                    dateAdded = System.currentTimeMillis(),
                    sourceUrl = sourceUrl.trim()
                )
                repository.addCollectionItem(newItem)
                _uiState.value = _uiState.value.copy(
                    showAddDialog = false,
                    editingItem = null,
                    successMessage = "Товар «${newItem.name}» додано до колекції"
                )
            } else {
                val updated = editing.copy(
                    name = name.trim(),
                    series = series.trim(),
                    category = category.trim(),
                    material = material.trim(),
                    nominal = nominal.trim(),
                    quality = quality.trim(),
                    description = description.trim(),
                    photoPath = photoPath,
                    estimatedValue = estimatedValue,
                    quantity = quantity,
                    sourceUrl = sourceUrl.trim()
                )
                repository.updateCollectionItem(updated)
                _uiState.value = _uiState.value.copy(
                    showAddDialog = false,
                    editingItem = null,
                    successMessage = "Зміни збережено"
                )
            }
        }
    }

    fun deleteItem(item: CollectionItem) {
        viewModelScope.launch {
            repository.deleteCollectionItem(item)
            _uiState.value = _uiState.value.copy(successMessage = "Товар видалено з колекції")
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = "", successMessage = "")
    }
}
