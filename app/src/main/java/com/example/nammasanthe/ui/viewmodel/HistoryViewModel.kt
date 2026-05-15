package com.example.nammasanthe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.model.Transaction
import com.example.nammasanthe.data.model.TransactionType
import com.example.nammasanthe.data.repository.LedgerRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class TransactionWithCustomer(
    val transaction: Transaction,
    val customerName: String
)

data class DayGroup(
    val dateLabel: String,
    val transactions: List<TransactionWithCustomer>,
    val totalCredit: Double,        // Udari
    val totalPayment: Double,       // Payments received
    val totalCashSales: Double,     // Cash sales
    val totalSales: Double,         // Udari + Cash Sales
    val pending: Double             // Real global outstanding at end of that day
)

data class HistoryUiState(val dayGroups: List<DayGroup> = emptyList())

class HistoryViewModel(private val repository: LedgerRepository) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getAllTransactions(),
        repository.getAllCustomers()
    ) { transactions: List<Transaction>, customers: List<Customer> ->

        val customerMap = customers.associate { it.id to it.name }

        // Include ALL types now (CREDIT, PAYMENT, CASH_SALE)
        val sorted = transactions.sortedByDescending { it.date }

        val grouped = LinkedHashMap<String, MutableList<Transaction>>()
        for (tx in sorted) {
            val dateKey = dateFormatter.format(Date(tx.date))
            grouped.getOrPut(dateKey) { mutableListOf() }.add(tx)
        }

        // Build list of (dateLabel, endOfDayTimestamp) so we can compute
        // global outstanding up to end of each day
        val dayGroups = grouped.map { (dateLabel, txList) ->
            val txWithCust = txList.map { tx ->
                TransactionWithCustomer(
                    transaction  = tx,
                    customerName = if (tx.type == TransactionType.CASH_SALE) "Cash Sale" else (customerMap[tx.customerId] ?: "Unknown Customer")
                )
            }
            val credit    = txList.filter { it.type == TransactionType.CREDIT    }.sumOf { it.amount }
            val payment   = txList.filter { it.type == TransactionType.PAYMENT   }.sumOf { it.amount }
            val cashSales = txList.filter { it.type == TransactionType.CASH_SALE }.sumOf { it.amount }

            // Today's dues for this day:
            // Only customers who took credit ON THIS DAY.
            // Their payment on this day reduces only their own due.
            // Customers who only paid old dues on this day are NOT counted.
            val customersWithCreditThisDay = txList
                .filter { it.type == TransactionType.CREDIT }
                .map { it.customerId }
                .toSet()

            val pendingUpToDay = customersWithCreditThisDay.sumOf { custId ->
                val creditDay  = txList.filter { it.customerId == custId && it.type == TransactionType.CREDIT  }.sumOf { it.amount }
                val paymentDay = txList.filter { it.customerId == custId && it.type == TransactionType.PAYMENT }.sumOf { it.amount }
                maxOf(0.0, creditDay - paymentDay)
            }

            DayGroup(
                dateLabel      = dateLabel,
                transactions   = txWithCust,
                totalCredit    = credit,
                totalPayment   = payment,
                totalCashSales = cashSales,
                totalSales     = credit + cashSales,
                pending        = pendingUpToDay
            )
        }

        HistoryUiState(dayGroups = dayGroups)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )
}
