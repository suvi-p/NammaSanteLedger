package com.example.nammasanthe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.model.Transaction
import com.example.nammasanthe.data.model.TransactionType
import com.example.nammasanthe.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerProfileUiState(
    val customer: Customer? = null,
    val transactions: List<Transaction> = emptyList(),
    val totalCredit: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalCashSales: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val isAdvancePaid: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val transactionToDelete: Transaction? = null,
    val showEditCustomerDialog: Boolean = false
)

class CustomerProfileViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val _customerId          = MutableStateFlow(0L)
    private val _showDeleteDialog    = MutableStateFlow(false)
    private val _transactionToDelete = MutableStateFlow<Transaction?>(null)
    private val _showEditDialog      = MutableStateFlow(false)

    fun setCustomerId(id: Long) { _customerId.value = id }

    val uiState: StateFlow<CustomerProfileUiState> = _customerId
        .filter { it != 0L }
        .flatMapLatest { customerId ->
            combine(
                repository.getCustomerById(customerId),
                repository.getTransactionsByCustomer(customerId),
                repository.getTotalCreditByCustomer(customerId),
                repository.getTotalPaymentByCustomer(customerId),
                _showDeleteDialog,
                _transactionToDelete,
                _showEditDialog
            ) { args ->
                val customer     = args[0] as Customer?
                @Suppress("UNCHECKED_CAST")
                val transactions = args[1] as List<Transaction>
                val credit       = args[2] as Double
                val payment      = args[3] as Double
                val showDialog   = args[4] as Boolean
                val txToDelete   = args[5] as Transaction?
                val showEdit     = args[6] as Boolean

                val cashSales = transactions.filter { it.type == TransactionType.CASH_SALE }.sumOf { it.amount }
                val balance   = credit - payment

                CustomerProfileUiState(
                    customer             = customer,
                    transactions         = transactions,
                    totalCredit          = credit,
                    totalPaid            = payment,
                    totalCashSales       = cashSales,
                    pendingBalance       = maxOf(0.0, balance),
                    isAdvancePaid        = balance < 0,
                    showDeleteDialog     = showDialog,
                    transactionToDelete  = txToDelete,
                    showEditCustomerDialog = showEdit
                )
            }
        }.stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomerProfileUiState()
        )

    fun confirmDeleteTransaction(transaction: Transaction) {
        _transactionToDelete.value = transaction
        _showDeleteDialog.value    = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value    = false
        _transactionToDelete.value = null
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            _transactionToDelete.value?.let { repository.deleteTransaction(it) }
            dismissDeleteDialog()
        }
    }

    fun showEditCustomerDialog()    { _showEditDialog.value = true }
    fun dismissEditCustomerDialog() { _showEditDialog.value = false }

    fun updateCustomer(name: String, phone: String) {
        viewModelScope.launch {
            val current = uiState.value.customer ?: return@launch
            repository.updateCustomer(current.copy(name = name.trim(), phone = phone.trim()))
            dismissEditCustomerDialog()
        }
    }
}
