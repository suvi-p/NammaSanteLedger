package com.example.nammasanthe.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun EditProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Use the same keys LoginScreen saves to
    var name by remember { mutableStateOf(prefs.getString("user_name", "") ?: "") }
    var phone by remember { mutableStateOf(prefs.getString("user_phone", "") ?: "") }
    var saved by remember { mutableStateOf(false) }

    val darkBlue = Color(0xFF0D47A1)
    val mediumBlue = Color(0xFF1976D2)
    val lightBlue = Color(0xFFE3F2FD)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlue)
    ) {

        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(darkBlue, mediumBlue))
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
                    "Edit Profile",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Avatar preview
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(darkBlue, mediumBlue))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (name.isNotBlank()) name.take(1).uppercase() else "?",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                "Vendor / Shop Owner",
                fontSize = 13.sp,
                color = Color(0xFF555555)
            )

            Spacer(Modifier.height(8.dp))

            // Name Field — enabled
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; saved = false },
                label = { Text("Your Name") },
                placeholder = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = true,
                singleLine = true
            )

            // Phone Field — enabled
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; saved = false },
                label = { Text("Phone Number") },
                placeholder = { Text("Enter your phone number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = true,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    prefs.edit()
                        .putString("user_name", name.trim())
                        .putString("user_phone", phone.trim())
                        .apply()
                    saved = true
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mediumBlue),
                enabled = name.isNotBlank()
            ) {
                Text(
                    "Save Changes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (saved) {
                Text(
                    "✅ Profile saved!",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
