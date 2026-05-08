package com.numisproerp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numisproerp.data.entities.OtherExpense
import com.numisproerp.data.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExpensesUiState(
    val expenses: List<OtherExpense> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val expensesFlow = repository.getAllExpenses()
            val expenses = expensesFlow.first()

            _uiState.value = _uiState.value.copy(
                expenses = expenses,
                isLoading = false
            )
        }
    }

    fun toggleAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }

    fun addExpense(category: String, amount: String, comment: String) {
        viewModelScope.launch {
            val amountDouble = amount.toDoubleOrNull()

            if (category.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Введіть категорію витрати")
                return@launch
            }

            if (amountDouble == null || amountDouble <= 0) {
                _uiState.value = _uiState.value.copy(errorMessage = "Введіть коректну суму")
                return@launch
            }

            val expense = OtherExpense(
                expenseId = "EXP-${System.currentTimeMillis()}",
                date = System.currentTimeMillis(),
                category = category,
                amount = amountDouble,
                comment = comment
            )

            repository.insertOtherExpense(expense)

            _uiState.value = _uiState.value.copy(
                showAddDialog = false,
                showSuccessMessage = true,
                errorMessage = ""
            )

            loadExpenses()
        }
    }

    fun formatDate(timestamp: Long): String {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(Date(timestamp))
    }
}
