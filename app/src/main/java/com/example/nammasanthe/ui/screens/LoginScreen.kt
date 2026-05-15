package com.example.nammasanthe.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nammasanthe.navigation.Screen
import com.example.nammasanthe.ui.theme.BlueAccent
import com.example.nammasanthe.ui.theme.PurpleStart

@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var hasCheckedLogin by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val phoneFocusRequester = remember { FocusRequester() }

    // ✅ Only auto-skip if already saved — but don't block UI rendering
    LaunchedEffect(Unit) {
        val savedName = prefs.getString("user_name", null)
        if (!savedName.isNullOrBlank()) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else {
            hasCheckedLogin = true
        }
    }

    // Don't render the form until we've verified there's no saved user (avoids flash)
    if (!hasCheckedLogin) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(PurpleStart, BlueAccent))),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(PurpleStart, BlueAccent))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("📒", fontSize = 44.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Namma Santhe Ledger",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Your Digital Khata Book",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name *", color = Color.White.copy(0.8f)) },
                placeholder = { Text("Enter your shop / vendor name", color = Color.White.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { phoneFocusRequester.requestFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(14.dp))

            // Phone field
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = { Text("Phone Number (Optional)", color = Color.White.copy(0.8f)) },
                placeholder = { Text("10-digit mobile number", color = Color.White.copy(0.5f)) },
                modifier = Modifier.fillMaxWidth().focusRequester(phoneFocusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (name.isNotBlank()) {
                            prefs.edit()
                                .putString("user_name", name.trim())
                                .putString("user_phone", phone.trim())
                                .apply()
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(0.7f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(28.dp))

            // Continue Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        keyboardController?.hide()
                        prefs.edit()
                            .putString("user_name", name.trim())
                            .putString("user_phone", phone.trim())
                            .apply()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = PurpleStart
                ),
                enabled = name.isNotBlank()
            ) {
                Text("Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Simple. Fast. Secure.",
                color = Color.White.copy(0.6f),
                fontSize = 12.sp
            )
        }
    }
}
