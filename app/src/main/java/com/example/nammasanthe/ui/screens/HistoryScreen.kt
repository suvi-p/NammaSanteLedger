package com.example.nammasanthe.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nammasanthe.R
import com.example.nammasanthe.data.database.AppDatabase
import com.example.nammasanthe.data.model.TransactionType
import com.example.nammasanthe.data.repository.LedgerRepository
import com.example.nammasanthe.ui.theme.*
import com.example.nammasanthe.ui.viewmodel.DayGroup
import com.example.nammasanthe.ui.viewmodel.HistoryViewModel
import com.example.nammasanthe.ui.viewmodel.TransactionWithCustomer
import com.example.nammasanthe.ui.viewmodel.ViewModelFactory
import com.example.nammasanthe.utils.Quad
import com.example.nammasanthe.utils.formatCurrency
import com.example.nammasanthe.utils.formatDate
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())
    val viewModel: HistoryViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    val todayLabel = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF4527A0), Color(0xFF7B1FA2))))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("History", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Daily sales, udari & payments",
                    color = Color.White.copy(0.7f), fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (uiState.dayGroups.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No history yet", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Add transactions to see history", color = TextSecondary, fontSize = 13.sp)
                }
            }
            return
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            uiState.dayGroups.forEach { dayGroup ->
                item(key = "header_${dayGroup.dateLabel}") {
                    DaySummaryHeader(dayGroup = dayGroup, todayLabel = todayLabel)
                }
                items(items = dayGroup.transactions, key = { it.transaction.id }) { txWithCust ->
                    TransactionHistoryRow(txWithCust = txWithCust)
                }
                item(key = "divider_${dayGroup.dateLabel}") { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun DaySummaryHeader(dayGroup: DayGroup, todayLabel: String) {
    val isToday = dayGroup.dateLabel == todayLabel
    val headerLabel = if (isToday) "Today — ${dayGroup.dateLabel}" else dayGroup.dateLabel

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = if (isToday) Color(0xFFEDE7FF) else CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    headerLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isToday) Color(0xFF5C35CC) else TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8EAF6))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${dayGroup.transactions.size} txn${if (dayGroup.transactions.size != 1) "s" else ""}",
                        fontSize = 11.sp, color = Color(0xFF3949AB), fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Line 1 — Total Sales (prominent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Total Sales  ",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    formatCurrency(dayGroup.totalSales),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5C35CC)
                )
            }
            Spacer(Modifier.height(3.dp))
            // Line 2 — Dues Pending
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "New Dues Today  ",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    formatCurrency(dayGroup.pending),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePending
                )
            }

            Spacer(Modifier.height(12.dp))

            // 2-stat row — Udari and Payments only (Pending is a lifetime figure, not daily)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HistoryStatChip("Udari",    formatCurrency(dayGroup.totalCredit),  RedCredit)
                HistoryDivider()
                HistoryStatChip("Payments", formatCurrency(dayGroup.totalPayment), GreenPayment)
            }
        }
    }
}

@Composable
fun HistoryDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color(0xFFE0E0E0))
    )
}

@Composable
fun HistoryStatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun DayStatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun TransactionHistoryRow(txWithCust: TransactionWithCustomer) {
    val tx = txWithCust.transaction
    val customerName = txWithCust.customerName

    val (bgColor, label, sign, iconRes) = when (tx.type) {
        TransactionType.CREDIT    -> Quad(RedCredit,    "Udari",     "+", R.drawable.arrow)
        TransactionType.PAYMENT   -> Quad(GreenPayment, "Payment",   "-", R.drawable.download)
        TransactionType.CASH_SALE -> Quad(BlueAccent,   "Cash Sale", "+", R.drawable.cash)
    }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        shape     = RoundedCornerShape(11.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)).background(bgColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(customerName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bgColor.copy(0.1f)).padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(label, fontSize = 11.sp, color = bgColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(formatDate(tx.date), fontSize = 12.sp, color = TextSecondary)
                    }
                    if (tx.description.isNotBlank()) {
                        Text(tx.description, fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
            Text("$sign ${formatCurrency(tx.amount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = bgColor)
        }
    }
}
