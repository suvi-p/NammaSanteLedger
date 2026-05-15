package com.example.nammasanthe.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    CREDIT,    // Udari - goods given on credit
    PAYMENT,   // Payment received from customer
    CASH_SALE  // Cash sale - no udari impact
}

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val amount: Double,
    val type: TransactionType,
    val date: Long = System.currentTimeMillis(),
    val description: String = ""
)
