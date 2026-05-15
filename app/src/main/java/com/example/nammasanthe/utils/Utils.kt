package com.example.nammasanthe.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatCurrency(amount: Double): String {
    return "₹${String.format("%.0f", amount)}"
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun daysBetween(timestamp: Long): Long {
    val now = System.currentTimeMillis()
    return (now - timestamp) / (1000 * 60 * 60 * 24)
}

// Helper data class to destructure 4 values
data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
