package com.example.nammasanthe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.model.Transaction
import com.example.nammasanthe.data.model.TransactionType
import com.example.nammasanthe.data.repository.LedgerRepository
import com.example.nammasanthe.ui.screens.MonthSummary
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class RecentTransactionItem(
    val transaction: Transaction,
    val customerName: String
)

// One point on the line graph — now includes cashSale
data class DailyPoint(
    val label: String,
    val credit: Double,
    val payment: Double,
    val cashSale: Double = 0.0
)

data class DashboardUiState(
    // Header stats (lifetime)
    val totalGiven: Double = 0.0,
    val totalToReceive: Double = 0.0,   // Global all-time outstanding (home header)
    val totalReceived: Double = 0.0,
    // Today stats
    val todayUdariGiven: Double = 0.0,      // CREDIT only today
    val todayCashSales: Double = 0.0,       // CASH_SALE only today
    val todayTotalSales: Double = 0.0,      // CREDIT + CASH_SALE today
    val todayReceived: Double = 0.0,        // PAYMENT today
    // Today's dues = only customers who took credit TODAY, minus what THEY paid today
    // Shivam paying old dues does NOT count here. Only today's new credit matters.
    val todayDuesPending: Double = 0.0,     // Σ max(0, todayCredit_i - todayPayment_i) per customer
    // Recent
    val recentTransactions: List<RecentTransactionItem> = emptyList(),
    // 7-day graph data
    val weeklyPoints: List<DailyPoint> = emptyList(),
    // All-time graph data
    val allTimePoints: List<DailyPoint> = emptyList(),
    // Monthly summary
    val monthlySummary: List<MonthSummary> = emptyList()
)

class DashboardViewModel(private val repository: LedgerRepository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            repository.getGlobalTotalCredit(),
            repository.getGlobalTotalPayment(),
            repository.getTodayCredit(),
            repository.getTodayPayments()
        ) { totalCredit, totalPayment, todayCredit, todayPayments ->
            listOf(totalCredit, totalPayment, todayCredit, todayPayments)
        },
        combine(
            repository.getTodayCashSales(),
            repository.getRecentTransactions(20),
            repository.getAllCustomers(),
            combine(
                repository.getAllTransactions(),
                repository.getTotalPendingDues()
            ) { txns, dues -> listOf<Any>(txns, dues) }
        ) { todayCash, recentTxns, customers, txnsAndDues ->
            listOf<Any>(todayCash, recentTxns, customers, txnsAndDues)
        }
    ) { stats, extra ->
        val totalCredit   = stats[0]
        val totalPayment  = stats[1]
        val todayCredit   = stats[2]
        val todayPayments = stats[3]

        val todayCash  = extra[0] as Double
        @Suppress("UNCHECKED_CAST")
        val recentTxns = extra[1] as List<Transaction>
        @Suppress("UNCHECKED_CAST")
        val customers  = extra[2] as List<Customer>
        @Suppress("UNCHECKED_CAST")
        val txnsAndDues = extra[3] as List<Any>
        @Suppress("UNCHECKED_CAST")
        val allTxns    = txnsAndDues[0] as List<Transaction>
        val realPendingDues = txnsAndDues[1] as Double

        val customerMap = customers.associate { it.id to it.name }

        // Compute TODAY's dues correctly:
        // Only look at customers who took credit TODAY.
        // For each such customer: max(0, theirTodayCredit - theirTodayPayment)
        // Shivam paying his old dues has ZERO effect here — he took no credit today.
        val todayCal = java.util.Calendar.getInstance()
        todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        todayCal.set(java.util.Calendar.MINUTE, 0)
        todayCal.set(java.util.Calendar.SECOND, 0)
        todayCal.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = todayCal.timeInMillis
        todayCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val todayEnd = todayCal.timeInMillis

        val todayTxns = allTxns.filter { it.date in todayStart until todayEnd }

        // Customers who took credit today
        val customersWithTodayCredit = todayTxns
            .filter { it.type == TransactionType.CREDIT }
            .map { it.customerId }
            .toSet()

        // For each such customer: their today credit - their today payment (floor 0)
        val todayDuesPending = customersWithTodayCredit.sumOf { custId ->
            val creditToday  = todayTxns.filter { it.customerId == custId && it.type == TransactionType.CREDIT  }.sumOf { it.amount }
            val paymentToday = todayTxns.filter { it.customerId == custId && it.type == TransactionType.PAYMENT }.sumOf { it.amount }
            maxOf(0.0, creditToday - paymentToday)
        }

        val recentWithNames = recentTxns
            .map { tx ->
                RecentTransactionItem(transaction = tx, customerName = customerMap[tx.customerId] ?: "Unknown")
            }

        // Build 7-day points
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        val weeklyCal = Calendar.getInstance()
        val weeklyPoints = (6 downTo 0).map { daysBack ->
            weeklyCal.timeInMillis = System.currentTimeMillis()
            weeklyCal.add(Calendar.DAY_OF_YEAR, -daysBack)
            weeklyCal.set(Calendar.HOUR_OF_DAY, 0); weeklyCal.set(Calendar.MINUTE, 0)
            weeklyCal.set(Calendar.SECOND, 0); weeklyCal.set(Calendar.MILLISECOND, 0)
            val dayStart = weeklyCal.timeInMillis
            weeklyCal.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = weeklyCal.timeInMillis
            val label    = if (daysBack == 0) "Today" else sdfDay.format(Date(dayStart))
            val dayCredit   = allTxns.filter { it.type == TransactionType.CREDIT   && it.date in dayStart until dayEnd }.sumOf { it.amount }
            val dayPayment  = allTxns.filter { it.type == TransactionType.PAYMENT  && it.date in dayStart until dayEnd }.sumOf { it.amount }
            val dayCash     = allTxns.filter { it.type == TransactionType.CASH_SALE && it.date in dayStart until dayEnd }.sumOf { it.amount }
            DailyPoint(label = label, credit = dayCredit, payment = dayPayment, cashSale = dayCash)
        }

        // Build ALL-TIME daily points
        val sdfDate = SimpleDateFormat("dd-MM", Locale.getDefault())
        val allTimePoints: List<DailyPoint> = run {
            if (allTxns.isEmpty()) return@run emptyList()
            val firstDate = allTxns.minOf { it.date }
            val calStart = Calendar.getInstance().apply {
                timeInMillis = firstDate
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val calEnd = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }
            val points = mutableListOf<DailyPoint>()
            val iter = calStart.clone() as Calendar
            while (!iter.after(calEnd)) {
                val dayStart = iter.timeInMillis
                iter.add(Calendar.DAY_OF_YEAR, 1)
                val dayEnd = iter.timeInMillis
                val dayCredit  = allTxns.filter { it.type == TransactionType.CREDIT   && it.date in dayStart until dayEnd }.sumOf { it.amount }
                val dayPayment = allTxns.filter { it.type == TransactionType.PAYMENT  && it.date in dayStart until dayEnd }.sumOf { it.amount }
                val dayCash    = allTxns.filter { it.type == TransactionType.CASH_SALE && it.date in dayStart until dayEnd }.sumOf { it.amount }
                points.add(DailyPoint(label = sdfDate.format(Date(dayStart)), credit = dayCredit, payment = dayPayment, cashSale = dayCash))
            }
            points
        }

        // Monthly summary
        val sdfMonth = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        val monthlyMap = mutableMapOf<String, Triple<Double, Double, Double>>()
        allTxns.forEach { tx ->
            val monthKey = sdfMonth.format(Date(tx.date))
            val existing = monthlyMap.getOrDefault(monthKey, Triple(0.0, 0.0, 0.0))
            monthlyMap[monthKey] = when (tx.type) {
                TransactionType.CREDIT    -> existing.copy(first  = existing.first  + tx.amount)
                TransactionType.PAYMENT   -> existing.copy(second = existing.second + tx.amount)
                TransactionType.CASH_SALE -> existing.copy(third  = existing.third  + tx.amount)
            }
        }
        val monthlySummary = monthlyMap.entries
            .sortedByDescending { entry -> SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(entry.key)?.time ?: 0L }
            .map { (month, amounts) ->
                MonthSummary(
                    label       = month,
                    credit      = amounts.first,
                    payment     = amounts.second,
                    cashSale    = amounts.third
                )
            }

        DashboardUiState(
            totalGiven        = totalCredit,
            totalToReceive    = maxOf(0.0, totalCredit - totalPayment),
            totalReceived     = totalPayment,
            todayUdariGiven   = todayCredit,
            todayCashSales    = todayCash,
            todayTotalSales   = todayCredit + todayCash,
            todayReceived     = todayPayments,
            todayDuesPending  = todayDuesPending,
            recentTransactions = recentWithNames,
            weeklyPoints      = weeklyPoints,
            allTimePoints     = allTimePoints,
            monthlySummary    = monthlySummary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
