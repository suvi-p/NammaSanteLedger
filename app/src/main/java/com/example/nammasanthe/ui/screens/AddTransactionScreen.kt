package com.example.nammasanthe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nammasanthe.data.database.AppDatabase
import com.example.nammasanthe.data.model.TransactionType
import com.example.nammasanthe.data.repository.LedgerRepository
import com.example.nammasanthe.ui.theme.*
import com.example.nammasanthe.ui.viewmodel.AddTransactionViewModel
import com.example.nammasanthe.ui.viewmodel.ViewModelFactory

@Composable
fun AddTransactionScreen(navController: NavController, customerId: Long, initialType: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())
    val viewModel: AddTransactionViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    
    val allowedTypes = listOf(TransactionType.CREDIT, TransactionType.PAYMENT, TransactionType.CASH_SALE)

    LaunchedEffect(customerId, initialType) {
        viewModel.setCustomerId(customerId, initialType)
    }

    if (uiState.isSaved) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(PurpleStart, BlueAccent)))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column {
                        Text("Add Transaction", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(uiState.customer?.name ?: "", color = Color.White.copy(0.8f), fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Type Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allowedTypes.forEach { type ->
                        val isSelected = uiState.selectedType == type
                        val label = when (type) {
                            TransactionType.CREDIT  -> "Udari (Credit)"
                            TransactionType.PAYMENT -> "Payment"
                            TransactionType.CASH_SALE -> "Cash Sale"
                            else -> ""
                        }
                        val selectedColor = when (type) {
                            TransactionType.CREDIT    -> RedCredit
                            TransactionType.PAYMENT   -> GreenPayment
                            TransactionType.CASH_SALE -> BlueAccent
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) selectedColor else Color.Transparent)
                                .clickable { viewModel.onTypeSelected(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // ── Amount Display ──
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Amount", color = TextSecondary, fontSize = 13.sp)
                Text(
                    "₹${uiState.amountStr}",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (uiState.selectedType) {
                        TransactionType.CREDIT  -> RedCredit
                        TransactionType.PAYMENT -> GreenPayment
                        else -> BlueAccent
                    }
                )
            }
        }

        // ── Quick Add Buttons ──
        Text("Quick Add", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), fontSize = 13.sp, color = TextSecondary)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(50, 100, 200, 500, 1000).forEach { amount ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PurpleStart.copy(alpha = 0.1f))
                        .clickable { viewModel.onQuickAdd(amount) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+$amount", fontSize = 11.sp, color = PurpleStart, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Description ──
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text("Description (Optional)") },
            placeholder = { Text("Add note...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // ── Calculator ──
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("⌫","0","✓"))
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        val bgColor = when (key) {
                            "✓" -> when (uiState.selectedType) {
                                TransactionType.CREDIT    -> RedCredit
                                TransactionType.PAYMENT   -> GreenPayment
                                TransactionType.CASH_SALE -> BlueAccent
                                else -> BlueAccent
                            }
                            "⌫" -> TextSecondary.copy(alpha = 0.2f)
                            else -> CardWhite
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable {
                                    when (key) {
                                        "⌫" -> viewModel.onBackspace()
                                        "✓" -> viewModel.saveTransaction()
                                        else -> viewModel.onDigitPressed(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                key,
                                fontSize = if (key == "✓") 22.sp else 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (key == "✓") Color.White else TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
