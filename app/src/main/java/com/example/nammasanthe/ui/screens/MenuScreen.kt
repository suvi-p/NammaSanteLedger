package com.example.nammasanthe.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nammasanthe.data.database.AppDatabase
import com.example.nammasanthe.data.repository.LedgerRepository
import com.example.nammasanthe.navigation.Screen
import com.example.nammasanthe.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(navController: NavController) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val name = prefs.getString("user_name", "Vendor") ?: "Vendor"
    val phone = prefs.getString("user_phone", "") ?: ""

    val coroutineScope = rememberCoroutineScope()
    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())

    var showSignOutDialog by remember { mutableStateOf(false) }

    // Sign Out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text  = { Text("Are you sure you want to sign out? All data including customers and transactions will be erased.", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.clearAllData()
                            prefs.edit().clear().apply()
                            showSignOutDialog = false
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(PurpleStart, BlueAccent))
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "Menu",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // User Card — tappable to navigate to Edit Profile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.EditProfile.route) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF5FF)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(PurpleStart, BlueAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        if (phone.isNotBlank()) {
                            Text(
                                text = "$phone",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        } else {
                            Text(
                                text = "Tap to edit your profile",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Text("⚙️", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))



            MenuItemRow(
                emoji = "📈",
                title = "Dashboard",
                subtitle = "View detailed trend graph and analytics",
                bgColor = PurpleStart
            ) { navController.navigate(Screen.FullDashboard.route) }

            Spacer(Modifier.height(10.dp))

            MenuItemRow(
                emoji = "🧾",
                title = "History",
                subtitle = "Daily transactions — sold, received, pending",
                bgColor = Color(0xFF1565C0)
            ) { navController.navigate(Screen.History.route) }

            Spacer(Modifier.height(10.dp))

            MenuItemRow(
                emoji = "",
                title = "Sign Out",
                subtitle = "Exit and return to login screen",
                bgColor = Color(0xFFE53935),
                iconRes = com.example.nammasanthe.R.drawable.signout
            ) { showSignOutDialog = true }

            Spacer(Modifier.height(24.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Namma Santhe Ledger",
                    color = PurpleStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text("Version 1.0.0", color = TextSecondary, fontSize = 12.sp)
                Text(
                    "Digital Khata for Small Shopkeepers",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun MenuItemRow(
    emoji: String,
    title: String,
    subtitle: String,
    bgColor: Color,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (iconRes != null) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = iconRes),
                            contentDescription = title,
                            modifier = androidx.compose.ui.Modifier.size(22.dp)
                        )
                    } else {
                        Text(emoji, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                    Text(subtitle, fontSize = 12.sp, color = TextSecondary)
                }
            }
            Text("›", fontSize = 22.sp, color = TextSecondary)
        }
    }
}
