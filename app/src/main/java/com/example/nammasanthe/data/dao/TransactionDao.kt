package com.example.nammasanthe.data.dao

import androidx.room.*
import com.example.nammasanthe.data.model.Transaction
import com.example.nammasanthe.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsByCustomer(customerId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Total credit (udari) for a customer
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE customerId = :customerId AND type = 'CREDIT'")
    fun getTotalCreditByCustomer(customerId: Long): Flow<Double>

    // Total payment for a customer
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE customerId = :customerId AND type = 'PAYMENT'")
    fun getTotalPaymentByCustomer(customerId: Long): Flow<Double>

    // Global total credit
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'CREDIT'")
    fun getTotalCredit(): Flow<Double>

    // Global total payment
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'PAYMENT'")
    fun getTotalPayment(): Flow<Double>

    // Global total cash sales
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'CASH_SALE'")
    fun getTotalCashSales(): Flow<Double>

    // Today's transactions
    @Query("SELECT * FROM transactions WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getTodayTransactions(startOfDay: Long, endOfDay: Long): Flow<List<Transaction>>

    // Today's credit
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'CREDIT' AND date >= :startOfDay AND date < :endOfDay")
    fun getTodayCredit(startOfDay: Long, endOfDay: Long): Flow<Double>

    // Today's cash sales
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'CASH_SALE' AND date >= :startOfDay AND date < :endOfDay")
    fun getTodayCashSales(startOfDay: Long, endOfDay: Long): Flow<Double>

    // Today's payments
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'PAYMENT' AND date >= :startOfDay AND date < :endOfDay")
    fun getTodayPayments(startOfDay: Long, endOfDay: Long): Flow<Double>

    // Real total pending dues:
    // Per-customer: balance = SUM(CREDIT) - SUM(PAYMENT). Sum only positive balances.
    @Query("""
        SELECT COALESCE(SUM(net), 0.0) FROM (
            SELECT 
                customerId,
                SUM(CASE WHEN type = 'CREDIT'  THEN amount ELSE 0 END) -
                SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE 0 END) AS net
            FROM transactions
            GROUP BY customerId
        ) WHERE net > 0
    """)
    fun getTotalPendingDues(): Flow<Double>

    // Customers with last credit older than X days and still pending balance
    @Query("""
        SELECT DISTINCT customerId FROM transactions 
        WHERE type = 'CREDIT' AND date < :cutoffDate
    """)
    fun getCustomerIdsWithOldCredit(cutoffDate: Long): Flow<List<Long>>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
