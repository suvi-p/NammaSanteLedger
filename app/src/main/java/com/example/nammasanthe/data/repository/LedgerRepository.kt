package com.example.nammasanthe.data.repository

import com.example.nammasanthe.data.dao.CustomerDao
import com.example.nammasanthe.data.dao.TransactionDao
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class LedgerRepository(
    private val customerDao: CustomerDao,
    private val transactionDao: TransactionDao
) {

    // --- Customer Operations ---
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    fun getCustomerById(id: Long): Flow<Customer?> = customerDao.getCustomerById(id)
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    suspend fun addCustomer(customer: Customer): Long = customerDao.insert(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.update(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.delete(customer)

    // --- Transaction Operations ---
    fun getTransactionsByCustomer(customerId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCustomer(customerId)

    fun getRecentTransactions(limit: Int = 20): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit)

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun addTransaction(transaction: Transaction): Long = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)

    // --- Balance Operations ---
    fun getTotalCreditByCustomer(customerId: Long): Flow<Double> =
        transactionDao.getTotalCreditByCustomer(customerId)

    fun getTotalPaymentByCustomer(customerId: Long): Flow<Double> =
        transactionDao.getTotalPaymentByCustomer(customerId)

    fun getGlobalTotalCredit(): Flow<Double> = transactionDao.getTotalCredit()
    fun getGlobalTotalPayment(): Flow<Double> = transactionDao.getTotalPayment()
    fun getGlobalTotalCashSales(): Flow<Double> = transactionDao.getTotalCashSales()

    // --- Today's Stats ---
    fun getTodayTransactions(): Flow<List<Transaction>> {
        val (start, end) = getTodayRange()
        return transactionDao.getTodayTransactions(start, end)
    }

    fun getTodayCredit(): Flow<Double> {
        val (start, end) = getTodayRange()
        return transactionDao.getTodayCredit(start, end)
    }

    fun getTodayCashSales(): Flow<Double> {
        val (start, end) = getTodayRange()
        return transactionDao.getTodayCashSales(start, end)
    }

    fun getTodayPayments(): Flow<Double> {
        val (start, end) = getTodayRange()
        return transactionDao.getTodayPayments(start, end)
    }

    // Real total pending dues (per-customer balance > 0, summed)
    fun getTotalPendingDues(): Flow<Double> = transactionDao.getTotalPendingDues()

    // Overdue: customers with credits older than 7 days
    fun getOverdueCustomerIds(): Flow<List<Long>> {
        val cutoff = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        return transactionDao.getCustomerIdsWithOldCredit(cutoff)
    }


    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAll()
        customerDao.deleteAll()
    }
}
