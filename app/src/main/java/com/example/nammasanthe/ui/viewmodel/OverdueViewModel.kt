package com.example.nammasanthe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OverdueCustomer(
    val customer: Customer,
    val pendingBalance: Double,
    val daysSinceLastCredit: Long
)

data class OverdueUiState(
    val overdueCustomers: List<OverdueCustomer> = emptyList()
)

class OverdueViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _overdueCustomers = MutableStateFlow<List<OverdueCustomer>>(emptyList())
    val uiState: StateFlow<OverdueUiState> = _overdueCustomers
        .map { OverdueUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OverdueUiState()
        )

    init {
        viewModelScope.launch {
            repository.getOverdueCustomerIds().collectLatest { ids ->
                val overdueList = mutableListOf<OverdueCustomer>()
                ids.forEach { customerId ->
                    repository.getCustomerById(customerId).firstOrNull()?.let { customer ->
                        val credit = repository.getTotalCreditByCustomer(customerId).firstOrNull() ?: 0.0
                        val payment = repository.getTotalPaymentByCustomer(customerId).firstOrNull() ?: 0.0
                        val balance = credit - payment
                        if (balance > 0) {
                            val allTx = repository.getTransactionsByCustomer(customerId).firstOrNull() ?: emptyList()
                            val lastCredit = allTx.filter { it.type.name == "CREDIT" }.maxByOrNull { it.date }
                            val days = lastCredit?.let {
                                (System.currentTimeMillis() - it.date) / (1000 * 60 * 60 * 24)
                            } ?: 0L
                            overdueList.add(OverdueCustomer(customer, balance, days))
                        }
                    }
                }
                _overdueCustomers.value = overdueList.sortedByDescending { it.daysSinceLastCredit }
            }
        }
    }
}
