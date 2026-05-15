package com.example.nammasanthe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.example.nammasanthe.data.model.Customer
import com.example.nammasanthe.data.repository.LedgerRepository
import com.example.nammasanthe.navigation.Screen
import com.example.nammasanthe.ui.theme.*
import com.example.nammasanthe.ui.viewmodel.CustomerViewModel
import com.example.nammasanthe.ui.viewmodel.CustomerWithBalance
import com.example.nammasanthe.ui.viewmodel.ViewModelFactory
import com.example.nammasanthe.utils.formatCurrency

@Composable
fun CustomersScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())
    val viewModel: CustomerViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    // ── KEY FIX: local state for search text ──────────────────────────────
    // Using uiState.searchQuery directly as TextField value causes the
    // letters to appear out-of-order because StateFlow emissions are async.
    // Keeping a local mutableState and syncing to the VM avoids the round-trip.
    var localSearchText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.customers) {
        viewModel.loadBalances(uiState.customers.map { it.customer })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Customers", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Customer", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Search bar — uses local state to fix cursor/letter-order bug ──
                OutlinedTextField(
                    value = localSearchText,
                    onValueChange = { newVal ->
                        localSearchText = newVal           // instant UI update
                        viewModel.onSearchQueryChange(newVal) // sync to VM for filtering
                    },
                    placeholder = { Text("Search by name or phone...", color = Color.White.copy(0.65f)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(0.65f))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor          = Color.White,
                        unfocusedTextColor        = Color.White,
                        focusedContainerColor     = Color.White.copy(alpha = 0.18f),
                        unfocusedContainerColor   = Color.White.copy(alpha = 0.18f),
                        focusedBorderColor        = Color.White.copy(alpha = 0.50f),
                        unfocusedBorderColor      = Color.Transparent,
                        cursorColor               = Color.White,
                        focusedPlaceholderColor   = Color.White.copy(0.65f),
                        unfocusedPlaceholderColor = Color.White.copy(0.65f)
                    )
                )
            }
        }

        // ── Customer list ────────────────────────────────────────────────────
        if (uiState.customers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👥", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No customers found",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        "Add your first customer to get started",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.customers) { cwb ->
                    CustomerListItem(
                        cwb = cwb,
                        onClick = { navController.navigate(Screen.CustomerProfile.createRoute(cwb.customer.id)) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── Add Customer dialog ──────────────────────────────────────────────────
    if (uiState.showAddDialog) {
        AddCustomerDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onAdd     = { name, phone, address -> viewModel.addCustomer(name, phone, address) }
        )
    }
}

// ── Customer list item ────────────────────────────────────────────────────────
@Composable
fun CustomerListItem(cwb: CustomerWithBalance, onClick: () -> Unit) {
    val isPending = cwb.pendingBalance > 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PurpleStart.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    cwb.customer.name.take(1).uppercase(),
                    color = PurpleStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cwb.customer.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                if (cwb.customer.phone.isNotBlank()) {
                    Text(cwb.customer.phone, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatCurrency(cwb.pendingBalance),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isPending) OrangePending else GreenPayment
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPending) RedCredit.copy(0.12f) else GreenPayment.copy(0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (isPending) "Pending" else "Cleared",
                        fontSize = 10.sp,
                        color = if (isPending) RedCredit else GreenPayment,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Add Customer dialog — very light blue theme ────────────────────────────────
@Composable
fun AddCustomerDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name     by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var address  by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    // Palette: very light blue background, darker blue accents, dark text
    val dialogBg   = Color(0xFFD6E6FF)   // medium-light blue (a step darker than before)
    val accentBlue = Color(0xFF1565C0)
    val borderBlue = Color(0xFFBBD4F5)
    val inputBg    = Color.White
    val labelColor = Color(0xFF444444)
    val textColor  = Color(0xFF1A1A2E)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Customer",
                fontWeight = FontWeight.Bold,
                color = accentBlue,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label       = { Text("Name *", color = labelColor) },
                    placeholder = { Text("Enter customer name", color = Color(0xFFAAAAAA)) },
                    isError     = nameError,
                    supportingText = if (nameError) ({ Text("Name is required", color = Color(0xFFD32F2F)) }) else null,
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(10.dp),
                    singleLine  = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor        = textColor,
                        unfocusedTextColor      = textColor,
                        focusedContainerColor   = inputBg,
                        unfocusedContainerColor = inputBg,
                        focusedBorderColor      = accentBlue,
                        unfocusedBorderColor    = borderBlue,
                        cursorColor             = accentBlue,
                        focusedLabelColor       = accentBlue,
                        unfocusedLabelColor     = labelColor
                    )
                )
                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label       = { Text("Phone (Optional)", color = labelColor) },
                    placeholder = { Text("Enter phone number", color = Color(0xFFAAAAAA)) },
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(10.dp),
                    singleLine  = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor        = textColor,
                        unfocusedTextColor      = textColor,
                        focusedContainerColor   = inputBg,
                        unfocusedContainerColor = inputBg,
                        focusedBorderColor      = accentBlue,
                        unfocusedBorderColor    = borderBlue,
                        cursorColor             = accentBlue,
                        focusedLabelColor       = accentBlue,
                        unfocusedLabelColor     = labelColor
                    )
                )
                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label       = { Text("Address (Optional)", color = labelColor) },
                    placeholder = { Text("Enter address", color = Color(0xFFAAAAAA)) },
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = RoundedCornerShape(10.dp),
                    minLines    = 2,
                    maxLines    = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor        = textColor,
                        unfocusedTextColor      = textColor,
                        focusedContainerColor   = inputBg,
                        unfocusedContainerColor = inputBg,
                        focusedBorderColor      = accentBlue,
                        unfocusedBorderColor    = borderBlue,
                        cursorColor             = accentBlue,
                        focusedLabelColor       = accentBlue,
                        unfocusedLabelColor     = labelColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    onAdd(name.trim(), phone.trim(), address.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                shape  = RoundedCornerShape(10.dp)
            ) {
                Text("Add Customer", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF777777))
            }
        },
        shape          = RoundedCornerShape(20.dp),
        containerColor = dialogBg
    )
}
