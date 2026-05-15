package com.example.nammasanthe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.repository.LedgerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerWithBalance(
    val customer: Customer,
    val pendingBalance: Double
)

data class CustomerListUiState(
    val customers: List<CustomerWithBalance> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false
)

class CustomerViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showAddDialog = MutableStateFlow(false)
    private val _balances = MutableStateFlow<Map<Long, Double>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CustomerListUiState> = combine(
        _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) repository.getAllCustomers()
            else repository.searchCustomers(query)
        },
        _balances,
        _showAddDialog
    ) { customers, balances, showDialog ->
        CustomerListUiState(
            customers = customers.map { customer ->
                CustomerWithBalance(
                    customer = customer,
                    pendingBalance = balances[customer.id] ?: 0.0
                )
            },
            searchQuery = _searchQuery.value,
            showAddDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomerListUiState()
    )

    // FIX: Each customer gets its own coroutine so ALL balances are collected concurrently.
    // The old code ran collect() inside forEach inside a single launch — the first
    // collect() never completed (Flow is infinite), so only customer #1 ever got a balance.
    fun loadBalances(customers: List<Customer>) {
        customers.forEach { customer ->
            viewModelScope.launch {
                combine(
                    repository.getTotalCreditByCustomer(customer.id),
                    repository.getTotalPaymentByCustomer(customer.id)
                ) { credit, payment ->
                    maxOf(0.0, credit - payment)
                }.collect { balance ->
                    _balances.value = _balances.value + (customer.id to balance)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun showAddDialog() { _showAddDialog.value = true }
    fun hideAddDialog() { _showAddDialog.value = false }

    fun addCustomer(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.addCustomer(
                Customer(name = name.trim(), phone = phone.trim(), address = address.trim())
            )
        }
        _showAddDialog.value = false
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { repository.deleteCustomer(customer) }
    }
}
