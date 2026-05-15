package com.example.nammasanthe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.model.Transaction
import com.example.nammasanthe.data.model.TransactionType
import com.example.nammasanthe.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val customer: Customer? = null,
    val amountStr: String = "0",
    val selectedType: TransactionType = TransactionType.CREDIT,
    val description: String = "",
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddTransactionViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _customerId = MutableStateFlow(0L)

    init {
        viewModelScope.launch {
            _customerId.filter { it != 0L }.collectLatest { id ->
                repository.getCustomerById(id).collectLatest { customer ->
                    _uiState.update { it.copy(customer = customer) }
                }
            }
        }
    }

    fun setCustomerId(id: Long, initialType: String) {
        _customerId.value = id
        _uiState.update {
            it.copy(selectedType = TransactionType.valueOf(initialType))
        }
    }

    fun onDigitPressed(digit: String) {
        _uiState.update { state ->
            val current = state.amountStr
            val newAmount = if (current == "0") digit else current + digit
            if (newAmount.length <= 7) state.copy(amountStr = newAmount)
            else state
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            val current = state.amountStr
            val newAmount = if (current.length <= 1) "0" else current.dropLast(1)
            state.copy(amountStr = newAmount)
        }
    }

    fun onQuickAdd(amount: Int) {
        _uiState.update { state ->
            val current = state.amountStr.toLongOrNull() ?: 0L
            val newAmount = current + amount
            if (newAmount.toString().length <= 7)
                state.copy(amountStr = newAmount.toString())
            else state
        }
    }

    fun onTypeSelected(type: TransactionType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onDescriptionChange(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount") }
            return
        }
        val customerId = _customerId.value
        if (customerId == 0L) {
            _uiState.update { it.copy(errorMessage = "Invalid customer") }
            return
        }
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    customerId = customerId,
                    amount = amount,
                    type = state.selectedType,
                    description = state.description
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
