package com.example.nammasanthe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nammasanthe.data.database.AppDatabase
import com.example.nammasanthe.data.repository.LedgerRepository
import com.example.nammasanthe.navigation.Screen
import com.example.nammasanthe.ui.theme.*
import com.example.nammasanthe.ui.viewmodel.OverdueCustomer
import com.example.nammasanthe.ui.viewmodel.OverdueViewModel
import com.example.nammasanthe.ui.viewmodel.ViewModelFactory
import com.example.nammasanthe.utils.formatCurrency
import androidx.compose.ui.platform.LocalContext

// Cream background for card list area
private val CreamBg   = Color(0xFFFFF8F1)
private val OverdueBlue = Color(0xFF1565C0)

@Composable
fun OverdueScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())
    val viewModel: OverdueViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── White top bar with red left accent ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Red left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(64.dp)
                    .background(Color(0xFFE53935))
                    .align(Alignment.CenterStart)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFFE53935))
                }
                Column {
                    Text(
                        "Overdue Payments",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        "Pending for more than 7 days",
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }

        // Red + blue two-tone divider under header
        Row(modifier = Modifier.fillMaxWidth().height(3.dp)) {
            Box(modifier = Modifier.weight(0.4f).fillMaxHeight().background(Color(0xFFE53935)))
            Box(modifier = Modifier.weight(0.6f).fillMaxHeight().background(OverdueBlue.copy(alpha = 0.25f)))
        }

        // ── List area with light-cream background ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CreamBg)
        ) {
            if (uiState.overdueCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GreenPayment.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✅", fontSize = 30.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No Overdue Payments",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1A1A2E)
                            )
                            Text(
                                "All payments are up to date!",
                                fontSize = 13.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.overdueCustomers) { overdueCustomer ->
                        OverdueCustomerCard(
                            overdueCustomer = overdueCustomer,
                            onViewProfile = {
                                navController.navigate(
                                    Screen.CustomerProfile.createRoute(overdueCustomer.customer.id)
                                )
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun OverdueCustomerCard(
    overdueCustomer: OverdueCustomer,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Name row + pending amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(OverdueBlue.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            overdueCustomer.customer.name.take(1).uppercase(),
                            color = OverdueBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            overdueCustomer.customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1A1A2E)
                        )
                        if (overdueCustomer.customer.phone.isNotBlank()) {
                            Text(
                                overdueCustomer.customer.phone,
                                fontSize = 12.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                }

                // Pending amount chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(OrangePending.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        formatCurrency(overdueCustomer.pendingBalance),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OrangePending
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Blue "View Profile" button — full width
            Button(
                onClick = onViewProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OverdueBlue),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "View Profile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
