package com.example.nammasanthe.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.nammasanthe.navigation.Screen
import com.example.nammasanthe.ui.theme.*
import com.example.nammasanthe.ui.viewmodel.DailyPoint
import com.example.nammasanthe.ui.viewmodel.DashboardViewModel
import com.example.nammasanthe.ui.viewmodel.RecentTransactionItem
import com.example.nammasanthe.ui.viewmodel.ViewModelFactory
import com.example.nammasanthe.utils.formatCurrency
import com.example.nammasanthe.utils.formatDate

// ── Shared data class ─────────────────────────────────────────────────────────
data class MonthSummary(
    val label: String,
    val credit: Double,
    val payment: Double,
    val cashSale: Double = 0.0
)

// ── Home Screen ───────────────────────────────────────────────────────────────
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val vendorName = prefs.getString("user_name", "Namma Santhe") ?: "Namma Santhe"

    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())
    val viewModel: DashboardViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        // ── HEADER ──
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
                        Column {
                            Text(vendorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Digital Khata", color = Color.White.copy(0.75f), fontSize = 12.sp)
                        }
                        IconButton(onClick = { navController.navigate(Screen.Menu.route) }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UdariStatCard("Total Given (Credit)", formatCurrency(uiState.totalGiven), "", Modifier.weight(1f))
                        UdariStatCard("Total Outstanding", formatCurrency(uiState.totalToReceive), "", Modifier.weight(1f), valueColor = Color(0xFFFFD54F))
                        UdariStatCard("Received", formatCurrency(uiState.totalReceived), "", Modifier.weight(1f), valueColor = Color(0xFFA5D6A7), topLabel = "Total")
                    }
                }
            }
        }

        // ── DAILY SUMMARY BANNER ──
        item {
            DailySummaryBanner(
                totalSales  = uiState.todayTotalSales,
                udariGiven  = uiState.todayUdariGiven,
                cashSales   = uiState.todayCashSales,
                received    = uiState.todayReceived,
                pending     = uiState.todayDuesPending
            )
        }

        // ── QUICK ACTIONS ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f).height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(PurpleStart, Color(0xFF9C27B0))))
                        .clickable { navController.navigate(Screen.Customers.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥", fontSize = 24.sp)
                        Text("Customers", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f).height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFEF5350))))
                        .clickable { navController.navigate(Screen.Overdue.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.overdue),
                            contentDescription = "Overdue",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Overdue", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        // ── RECENT TRANSACTIONS ──
        item {
            Text(
                "Recent Transactions",
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary
            )
        }

        if (uiState.recentTransactions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📝", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No transactions yet", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(uiState.recentTransactions.take(10)) { item ->
                RecentTransactionRow(item = item)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Daily Summary Banner ──────────────────────────────────────────────────────
@Composable
fun DailySummaryBanner(
    totalSales: Double,
    udariGiven: Double,
    cashSales: Double,
    received: Double,
    pending: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row with "Today" label and summary text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8EAF6))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Today", fontSize = 11.sp, color = Color(0xFF3949AB), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Line 1 — Total Sales (prominent)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total Sales  ", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text(formatCurrency(totalSales), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5C35CC))
            }
            Spacer(Modifier.height(4.dp))
            // Line 2 — Dues Pending
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Today's Dues  ", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text(formatCurrency(pending), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OrangePending)
            }

            Spacer(Modifier.height(14.dp))

            // 4-stat grid: Udari, Cash Sales, Payments, Pending
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TodayStatItem(label = "Udari",      value = formatCurrency(udariGiven), color = RedCredit)
                VertDivider()
                TodayStatItem(label = "Cash Sales", value = formatCurrency(cashSales),  color = BlueAccent)
                VertDivider()
                TodayStatItem(label = "Payments",   value = formatCurrency(received),   color = GreenPayment)
                VertDivider()
                TodayStatItem(label = "Today Dues", value = formatCurrency(pending),    color = OrangePending)
            }
        }
    }
}

@Composable
fun VertDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(Color(0xFFE0E0E0))
    )
}

@Composable
fun TodayStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 9.sp, color = TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ── Full Dashboard Analytics Screen ──────────────────────────────────────────
@Composable
fun FullDashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val repository = LedgerRepository(db.customerDao(), db.transactionDao())
    val viewModel: DashboardViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BackgroundLight)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(PurpleStart, BlueAccent)))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text("Dashboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Complete transaction trend", color = Color.White.copy(0.75f), fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UdariStatCard("Total Given (Credit)", formatCurrency(uiState.totalGiven), "", Modifier.weight(1f))
                        UdariStatCard("Total Outstanding", formatCurrency(uiState.totalToReceive), "", Modifier.weight(1f), valueColor = Color(0xFFFFD54F))
                        UdariStatCard("Received", formatCurrency(uiState.totalReceived), "", Modifier.weight(1f), valueColor = Color(0xFFA5D6A7), topLabel = "Total")
                    }
                }
            }
        }

        if (uiState.allTimePoints.isNotEmpty()) {
            item { AllTimeLineGraph(points = uiState.allTimePoints) }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📈", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No data yet", color = TextSecondary, fontSize = 14.sp)
                        Text("Add transactions to see the trend", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }

        if (uiState.monthlySummary.isNotEmpty()) {
            item {
                Text(
                    "Monthly Summary",
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary
                )
            }
            items(uiState.monthlySummary) { month -> MonthlySummaryRow(month) }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── All-Time Scrollable Line Graph — Udari + Payment lines ──────────────────────
@Composable
fun AllTimeLineGraph(points: List<DailyPoint>) {
    var selectedIdx by remember { mutableStateOf<Int?>(null) }

    val rawMax = points.maxOf { maxOf(it.credit, it.payment) }.takeIf { it > 0 } ?: 1000.0
    val maxVal = Math.ceil(rawMax / 1000.0) * 1000.0

    val yTicks: List<Double> = run {
        val ticks = mutableListOf<Double>()
        var v = 0.0
        while (v <= maxVal + 0.001) { ticks.add(v); v += 1000.0 }
        ticks
    }

    fun fmtYAxis(v: Double): String = when {
        v == 0.0           -> "0"
        v >= 1_00_000.0    -> "${(v / 1_00_000.0).toInt()}L"
        v % 1_000.0 == 0.0 -> "${(v / 1_000.0).toInt()}k"
        v >= 1_000.0       -> "${"%.1f".format(v / 1_000.0)}k"
        else               -> v.toInt().toString()
    }

    fun fmtExact(v: Double): String = "₹${"%,.0f".format(v)}"

    val graphHeightDp   = 200.dp
    val yAxisWidthDp    = 42.dp
    val pointWidthDp    = 40.dp
    val totalGraphWidth = maxOf(pointWidthDp * points.size, 280.dp)

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Complete Sales Trend", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            Text("Day 1 to today — tap a dot to inspect", fontSize = 11.sp, color = TextSecondary)
            Spacer(Modifier.height(6.dp))

            // Legend — 2 lines
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot("Udari (Credit)", RedCredit)
                LegendDot("Payment", GreenPayment)
            }

            // Tooltip
            val sel = selectedIdx
            if (sel != null && sel in points.indices) {
                val pt = points[sel]
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(10.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color(0xFFF2F7FF)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pt.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TooltipCol("Udari",   fmtExact(pt.credit),  RedCredit)
                            TooltipCol("Payment", fmtExact(pt.payment), GreenPayment)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                // Fixed Y-axis
                Canvas(modifier = Modifier.width(yAxisWidthDp).height(graphHeightDp)) {
                    val h = size.height; val padT = 16f; val padB = 28f; val graphH = h - padT - padB
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY; textSize = 22f
                        textAlign = android.graphics.Paint.Align.RIGHT; isAntiAlias = true
                    }
                    yTicks.forEach { tick ->
                        val y = padT + graphH * (1f - (tick / maxVal).toFloat())
                        drawContext.canvas.nativeCanvas.drawText(fmtYAxis(tick), size.width - 4f, y + 7f, paint)
                    }
                }

                // Scrollable graph
                Box(modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())) {
                    Canvas(
                        modifier = Modifier
                            .width(totalGraphWidth)
                            .height(graphHeightDp)
                            .pointerInput(points) {
                                detectTapGestures { tap ->
                                    if (points.size < 2) return@detectTapGestures
                                    val padL = 8f; val padR = 8f; val padT = 16f; val padB = 28f
                                    val gW = size.width - padL - padR; val gH = size.height - padT - padB
                                    val n = points.size
                                    fun xOf(i: Int) = padL + i * gW / (n - 1)
                                    fun yOf(v: Double) = padT + gH - (v / maxVal * gH).toFloat()
                                    val hitR = 24f
                                    var found: Int? = null
                                    for (i in points.indices) {
                                        val cx = xOf(i)
                                        val dx = tap.x - cx
                                        if (dx * dx + (tap.y - yOf(points[i].credit)) .let { it * it } <= hitR * hitR ||
                                            dx * dx + (tap.y - yOf(points[i].payment)).let { it * it } <= hitR * hitR) {
                                            found = i; break
                                        }
                                    }
                                    selectedIdx = if (found == selectedIdx) null else found
                                }
                            }
                    ) {
                        val w = size.width; val h = size.height
                        val padL = 8f; val padR = 8f; val padT = 16f; val padB = 28f
                        val gW = w - padL - padR; val gH = h - padT - padB
                        val n = points.size
                        if (n < 2) return@Canvas

                        fun xOf(i: Int)    = padL + i * gW / (n - 1)
                        fun yOf(v: Double) = padT + gH - (v / maxVal * gH).toFloat()

                        // Grid
                        yTicks.forEach { tick ->
                            drawLine(Color.LightGray.copy(alpha = 0.35f),
                                Offset(padL, yOf(tick)), Offset(w - padR, yOf(tick)), strokeWidth = 1f)
                        }

                        // Credit fill + line
                        drawPath(Path().apply {
                            points.forEachIndexed { i, p -> if (i == 0) moveTo(xOf(i), yOf(p.credit)) else lineTo(xOf(i), yOf(p.credit)) }
                            lineTo(xOf(n-1), padT+gH); lineTo(xOf(0), padT+gH); close()
                        }, Brush.verticalGradient(listOf(RedCredit.copy(alpha = 0.18f), Color.Transparent), startY = padT, endY = padT+gH))
                        drawPath(Path().apply {
                            points.forEachIndexed { i, p -> if (i == 0) moveTo(xOf(i), yOf(p.credit)) else lineTo(xOf(i), yOf(p.credit)) }
                        }, RedCredit, style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // Payment fill + line
                        drawPath(Path().apply {
                            points.forEachIndexed { i, p -> if (i == 0) moveTo(xOf(i), yOf(p.payment)) else lineTo(xOf(i), yOf(p.payment)) }
                            lineTo(xOf(n-1), padT+gH); lineTo(xOf(0), padT+gH); close()
                        }, Brush.verticalGradient(listOf(GreenPayment.copy(alpha = 0.15f), Color.Transparent), startY = padT, endY = padT+gH))
                        drawPath(Path().apply {
                            points.forEachIndexed { i, p -> if (i == 0) moveTo(xOf(i), yOf(p.payment)) else lineTo(xOf(i), yOf(p.payment)) }
                        }, GreenPayment, style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // Dots + labels
                        val labelEvery = maxOf(1, n / 10)
                        val labelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY; textSize = 20f
                            textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
                        }
                        points.forEachIndexed { i, p ->
                            val xC = xOf(i); val isSelec = selectedIdx == i; val dotR = if (isSelec) 8f else 4.5f
                            if (isSelec) drawLine(Color(0xFFDDDDDD), Offset(xC, padT), Offset(xC, padT+gH), 1.5f)
                            drawCircle(Color.White, dotR+2.5f, Offset(xC, yOf(p.credit)));  drawCircle(RedCredit,    dotR, Offset(xC, yOf(p.credit)))
                            drawCircle(Color.White, dotR+2.5f, Offset(xC, yOf(p.payment))); drawCircle(GreenPayment, dotR, Offset(xC, yOf(p.payment)))
                            if (i % labelEvery == 0) drawContext.canvas.nativeCanvas.drawText(p.label, xC, padT+gH+22f, labelPaint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
fun TooltipCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ── Monthly Summary Row ───────────────────────────────────────────────────────
@Composable
fun MonthlySummaryRow(month: MonthSummary) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(month.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    val totalSales = month.credit + month.cashSale
                    Text("Total Sales: ${formatCurrency(totalSales)}", fontSize = 11.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Udari",   fontSize = 10.sp, color = TextSecondary)
                        Text(formatCurrency(month.credit),  fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RedCredit)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Payment", fontSize = 10.sp, color = TextSecondary)
                        Text(formatCurrency(month.payment), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenPayment)
                    }
                }
            }
        }
    }
}

// ── Udari Header Stat Card ────────────────────────────────────────────────────
@Composable
fun UdariStatCard(
    label: String, value: String, emoji: String,
    modifier: Modifier = Modifier, valueColor: Color = Color.White,
    topLabel: String = ""
) {
    Card(
        modifier = modifier,
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                maxLines = 1, softWrap = false)
            if (topLabel.isNotEmpty()) {
                Text(topLabel, color = Color.White.copy(0.65f), fontSize = 10.sp)
            }
            Text(label, color = Color.White.copy(0.8f), fontSize = 10.sp)
        }
    }
}

// ── Today Stat Card ───────────────────────────────────────────────────────────
@Composable
fun StatCard(
    label: String, value: String, sub: String,
    modifier: Modifier = Modifier, valueColor: Color = TextPrimary
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(sub,   fontSize = 10.sp, color = TextSecondary)
        }
    }
}

// ── Recent Transaction Row ────────────────────────────────────────────────────
@Composable
fun RecentTransactionRow(item: RecentTransactionItem) {
    val transaction = item.transaction
    val color = when (transaction.type) {
        TransactionType.CREDIT    -> RedCredit
        TransactionType.PAYMENT   -> GreenPayment
        TransactionType.CASH_SALE -> BlueAccent
    }
    val iconRes = when (transaction.type) {
        TransactionType.CREDIT    -> R.drawable.arrow
        TransactionType.PAYMENT   -> R.drawable.download
        TransactionType.CASH_SALE -> R.drawable.cash
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    val displayName = if (transaction.type == TransactionType.CASH_SALE) "Cash Sale" else item.customerName
                    Text(displayName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    val typeLabel = when (transaction.type) {
                        TransactionType.CREDIT    -> "Udari (Credit)"
                        TransactionType.PAYMENT   -> "Payment"
                        TransactionType.CASH_SALE -> "Cash Sale"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color.copy(0.1f)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                            Text(typeLabel, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(formatDate(transaction.date), fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
            val sign = if (transaction.type == TransactionType.PAYMENT) "-" else "+"
            Text(
                "$sign${formatCurrency(transaction.amount)}",
                fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = color
            )
        }
    }
}
