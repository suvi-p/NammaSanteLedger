package com.example.nammasanthe.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nammasanthe.R
import com.example.nammasanthe.data.database.AppDatabase
import com.example.nammasanthe.data.model.Transaction
import com.example.nammasanthe.data.model.TransactionType
import com.example.nammasanthe.data.repository.LedgerRepository
import com.example.nammasanthe.navigation.Screen
import com.example.nammasanthe.ui.theme.*
import com.example.nammasanthe.ui.viewmodel.CustomerProfileViewModel
import com.example.nammasanthe.ui.viewmodel.ViewModelFactory
import com.example.nammasanthe.utils.formatCurrency
import com.example.nammasanthe.utils.formatDate
import com.example.nammasanthe.utils.Quad

@Composable
fun CustomerProfileScreen(navController: NavController, customerId: Long) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())
    val viewModel: CustomerProfileViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(customerId) { viewModel.setCustomerId(customerId) }

    LazyColumn(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {

        // ── HEADER ────────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(PurpleStart, BlueAccent)))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Column {
                                Text(uiState.customer?.name ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                if (!uiState.customer?.phone.isNullOrBlank()) {
                                    Text("${uiState.customer?.phone}", color = Color.White.copy(0.8f), fontSize = 13.sp)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.showEditCustomerDialog() }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                            }
                            if (!uiState.customer?.phone.isNullOrBlank()) {
                                IconButton(onClick = {
                                    val phone   = uiState.customer?.phone ?: ""
                                    val pending = uiState.pendingBalance
                                    val msg = "Hello, you have a pending udari of ${formatCurrency(pending)}. Please pay soon."
                                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://wa.me/91$phone?text=${Uri.encode(msg)}".toUri()))
                                }) {
                                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                        Image(painter = painterResource(id = R.drawable.whatsapp), contentDescription = "WhatsApp", modifier = Modifier.size(22.dp))
                                    }
                                }
                                IconButton(onClick = {
                                    val phone   = uiState.customer?.phone ?: ""
                                    val pending = uiState.pendingBalance
                                    val msg = "Hello, you have a pending udari of ${formatCurrency(pending)}. Please pay soon."
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "sms:$phone".toUri()
                                        putExtra("sms_body", msg)
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                                        Image(painter = painterResource(id = R.drawable.sms), contentDescription = "SMS", modifier = Modifier.size(22.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 3 header chips — Udari, Paid, Balance
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryChip("Total Udari", formatCurrency(uiState.totalCredit), Modifier.weight(1f))
                        SummaryChip("Total Paid",  formatCurrency(uiState.totalPaid),   Modifier.weight(1f))
                        SummaryChip(
                            "Balance",
                            if (uiState.isAdvancePaid) "Advance" else formatCurrency(uiState.pendingBalance),
                            Modifier.weight(1f),
                            valueColor = if (uiState.isAdvancePaid) GreenPayment
                                         else if (uiState.pendingBalance > 0) Color(0xFFFFD54F)
                                         else Color.White
                        )
                    }


                }
            }
        }

        // ── ACTION BUTTONS — 3 buttons ────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            // Row 1: Udari + Payment
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.AddTransaction.createRoute(customerId, "CREDIT")) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = RedCredit),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Add Udari", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(
                    onClick = { navController.navigate(Screen.AddTransaction.createRoute(customerId, "PAYMENT")) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = GreenPayment),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Payment", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            // Row 2: Cash Sale (full width, distinct blue)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.AddTransaction.createRoute(customerId, "CASH_SALE")) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Cash Sale", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── LEDGER ────────────────────────────────────────────────────────────
        item {
            Text(
                "Transaction Ledger",
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary
            )
        }

        if (uiState.transactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📄", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No transactions yet", color = TextSecondary, fontSize = 14.sp)
                        Text("Add udari, payment or cash sale to get started", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // Show all transaction types; running balance tracks only CREDIT/PAYMENT
            val sorted = uiState.transactions.sortedBy { it.date }
            var runningBalance = 0.0
            val txWithBalance = sorted.map { tx ->
                when (tx.type) {
                    TransactionType.CREDIT  -> runningBalance += tx.amount
                    TransactionType.PAYMENT -> runningBalance = maxOf(0.0, runningBalance - tx.amount)
                    TransactionType.CASH_SALE -> { /* no impact on balance */ }
                }
                tx to runningBalance
            }.reversed()

            items(txWithBalance) { (tx, balance) ->
                LedgerTransactionItem(
                    transaction    = tx,
                    runningBalance = balance,
                    onDelete       = { viewModel.confirmDeleteTransaction(tx) }
                )
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }

    // ── Edit Customer Dialog ──
    if (uiState.showEditCustomerDialog) {
        var editName  by remember { mutableStateOf(uiState.customer?.name  ?: "") }
        var editPhone by remember { mutableStateOf(uiState.customer?.phone ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.dismissEditCustomerDialog() },
            title = { Text("Edit Customer", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it },
                        label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it },
                        label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
            },
            confirmButton = {
                Button(
                    onClick  = { viewModel.updateCustomer(editName, editPhone) },
                    enabled  = editName.isNotBlank(),
                    colors   = ButtonDefaults.buttonColors(containerColor = PurpleStart)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { viewModel.dismissEditCustomerDialog() }) { Text("Cancel") } },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── Delete Confirmation Dialog ──
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title  = { Text("Delete Transaction") },
            text   = { Text("Are you sure? This cannot be undone.") },
            confirmButton = {
                Button(onClick = { viewModel.deleteTransaction() }, colors = ButtonDefaults.buttonColors(containerColor = RedCredit)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { viewModel.dismissDeleteDialog() }) { Text("Cancel") } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ── Composables ───────────────────────────────────────────────────────────────

@Composable
fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.White) {
    Card(modifier = modifier, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, color = Color.White.copy(0.8f), fontSize = 11.sp)
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun UdariProfileStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
fun LedgerTransactionItem(transaction: Transaction, runningBalance: Double, onDelete: () -> Unit) {
    val (typeLabel, typeColor, sign, iconRes) = when (transaction.type) {
        TransactionType.CREDIT    -> Quad("UDARI",     RedCredit,    "+", R.drawable.arrow)
        TransactionType.PAYMENT   -> Quad("PAYMENT",   GreenPayment, "-", R.drawable.download)
        TransactionType.CASH_SALE -> Quad("CASH SALE", BlueAccent,   "+", R.drawable.cash)
    }
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(typeColor.copy(0.12f)).padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Image(painter = painterResource(id = iconRes), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(typeLabel, fontSize = 10.sp, color = typeColor, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(formatDate(transaction.date), fontSize = 11.sp, color = TextSecondary)
                    if (transaction.description.isNotBlank()) {
                        Text(transaction.description, fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$sign ${formatCurrency(transaction.amount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = typeColor)
                // For cash sales, don't show running balance (it doesn't affect the udari balance)
                if (transaction.type != TransactionType.CASH_SALE) {
                    Text("Balance: ${formatCurrency(runningBalance)}", fontSize = 10.sp, color = TextSecondary)
                } else {
                    Text("Cash", fontSize = 10.sp, color = TextSecondary)
                }
            }

        }
    }
}
