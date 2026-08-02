package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyExpenseEntity
import com.example.ui.theme.GlassTheme
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val monthlyData by viewModel.monthlyData.collectAsState()
    val dailyExpenses by viewModel.dailyExpenses.collectAsState()

    val totalExpenses = viewModel.calculateTotalExpenses(monthlyData)
    val hasData = viewModel.hasFinancialData(monthlyData)
    val income = (monthlyData.income ?: 0.0) + (monthlyData.bonus ?: 0.0)

    // Categorization into 50/30/20 groupings
    val needsSum = (monthlyData.groceries ?: 0.0) +
            (monthlyData.medicine ?: 0.0) +
            (monthlyData.education ?: 0.0) +
            (monthlyData.rent ?: 0.0) +
            (monthlyData.food ?: 0.0) +
            (monthlyData.transport ?: 0.0) +
            (monthlyData.electricity ?: 0.0) +
            (monthlyData.internet ?: 0.0) +
            (monthlyData.mobile ?: 0.0) +
            (monthlyData.emi ?: 0.0)

    val wantsSum = (monthlyData.shopping ?: 0.0) +
            (monthlyData.entertainment ?: 0.0) +
            (monthlyData.others ?: 0.0)

    val savingsSum = (monthlyData.savings ?: 0.0) +
            (monthlyData.investments ?: 0.0) +
            (monthlyData.subscriptions ?: 0.0)

    // Calculate percentages relative to TOTAL EXPENSES
    val needsPct = if (totalExpenses > 0.0) (needsSum / totalExpenses) * 100 else 0.0
    val wantsPct = if (totalExpenses > 0.0) (wantsSum / totalExpenses) * 100 else 0.0
    val savingsPct = if (totalExpenses > 0.0) (savingsSum / totalExpenses) * 100 else 0.0

    // Calculate percentages relative to INCOME
    val needsOfIncome = if (income > 0.0) (needsSum / income) * 100 else 0.0
    val wantsOfIncome = if (income > 0.0) (wantsSum / income) * 100 else 0.0
    val savingsOfIncome = if (income > 0.0) (savingsSum / income) * 100 else 0.0

    // List of active categories for breakdown list
    val categories = listOf(
        ReportCategoryItem("Groceries", monthlyData.groceries ?: 0.0, Color(0xFF059669)),
        ReportCategoryItem("Medicine", monthlyData.medicine ?: 0.0, Color(0xFFE11D48)),
        ReportCategoryItem("Education", monthlyData.education ?: 0.0, Color(0xFF8B5CF6)),
        ReportCategoryItem("Rent", monthlyData.rent ?: 0.0, Color(0xFF2563EB)),
        ReportCategoryItem("Food", monthlyData.food ?: 0.0, Color(0xFFD97706)),
        ReportCategoryItem("Transport", monthlyData.transport ?: 0.0, Color(0xFF059669)),
        ReportCategoryItem("Electricity", monthlyData.electricity ?: 0.0, Color(0xFFCA8A04)),
        ReportCategoryItem("Internet", monthlyData.internet ?: 0.0, Color(0xFF7C3AED)),
        ReportCategoryItem("Mobile", monthlyData.mobile ?: 0.0, Color(0xFFDB2777)),
        ReportCategoryItem("EMI", monthlyData.emi ?: 0.0, Color(0xFFDC2626)),
        ReportCategoryItem("Subscriptions", monthlyData.subscriptions ?: 0.0, Color(0xFF0D9488)),
        ReportCategoryItem("Shopping", monthlyData.shopping ?: 0.0, Color(0xFF7C3AED)),
        ReportCategoryItem("Entertainment", monthlyData.entertainment ?: 0.0, Color(0xFFE11D48)),
        ReportCategoryItem("Investments", monthlyData.investments ?: 0.0, Color(0xFF059669)),
        ReportCategoryItem("Savings", monthlyData.savings ?: 0.0, Color(0xFF2563EB)),
        ReportCategoryItem("Others", monthlyData.others ?: 0.0, Color(0xFF6B7280))
    )
        .filter { it.value > 0.0 }
        .sortedByDescending { it.value }

    // Comparative calculations (Daily, Weekly, Monthly actual expenses)
    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    val oneWeekMs = 7 * oneDayMs
    val oneMonthMs = 30 * oneDayMs

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dailyTotal = dailyExpenses.filter { it.date == todayStr }.sumOf { it.amount }
    val weeklyTotal = dailyExpenses.filter { now - it.createdAt < oneWeekMs }.sumOf { it.amount }
    val monthlyTotal = dailyExpenses.filter { viewModel.isDateInCurrentSalaryCycle(it.date) }.sumOf { it.amount }

    val totalUniqueDays = dailyExpenses.map { it.date }.distinct().size.coerceAtLeast(1)
    val dailyAverage = dailyExpenses.sumOf { it.amount } / totalUniqueDays

    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_root")
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        // Title Header
        Text(
            text = "Financial Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = if (isDark) Color.White else Color(0xFF141F16)
        )
        Text(
            text = "Analysis and comparison of actual logged expenditures",
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif,
            color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Time Series trend chart
        TimeSeriesChart(expenses = dailyExpenses)

        Spacer(modifier = Modifier.height(16.dp))

        // Comparative Cards (Daily, Weekly, Monthly Comparisons)
        Text(
            text = "Expenses Comparisons",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = if (isDark) Color.White else Color(0xFF141F16),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = GlassTheme.cardColors(),
            shape = GlassTheme.cornerRadius,
            border = GlassTheme.borderStroke()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                ComparisonRow(
                    title = "Daily Spending",
                    total = dailyTotal,
                    subtitle = "Average: ₹${String.format(Locale.getDefault(), "%,.0f", dailyAverage)} / day",
                    color = if (isDark) Color(0xFF81C784) else Color(0xFF0F9D58)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000))
                ComparisonRow(
                    title = "Weekly Spending (7 Days)",
                    total = weeklyTotal,
                    subtitle = "Approx. weekly limit: ₹${String.format(Locale.getDefault(), "%,.0f", income / 4.3)}",
                    color = Color(0xFF2563EB)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000))
                ComparisonRow(
                    title = "Monthly Spending (30 Days)",
                    total = monthlyTotal,
                    subtitle = "Budget Ceiling: ₹${String.format(Locale.getDefault(), "%,.0f", income)}",
                    color = Color(0xFFE11D48)
                )
            }
        }

        if (!hasData || totalExpenses <= 0.0) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(if (isDark) Color(0x22FFFFFF) else Color(0x11000000), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = if (isDark) Color(0x7FFFFFFF) else Color(0x7F000000)
                    )
                }
                Text(
                    text = "No budget planning data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isDark) Color.White else Color(0xFF141F16)
                )
                Text(
                    text = "Please configure your income and budgets in the Expenses tab to unlock budget rule analyses.",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isDark) Color(0x7FFFFFFF) else Color(0x7F000000),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            // 50/30/20 Rule Analysis Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = GlassTheme.cardColors(),
                shape = GlassTheme.cornerRadius,
                border = GlassTheme.borderStroke()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "50/30/20 Budget Analysis",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (isDark) Color.White else Color(0xFF141F16)
                        )
                        Box(
                            modifier = Modifier
                                .background(if (isDark) Color(0x1681C784) else Color(0x160F9D58), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Rule Based",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF81C784) else Color(0xFF0F9D58)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Needs
                    BudgetRuleBar(
                        title = "Essential Needs",
                        actualPct = needsPct,
                        actualOfIncome = needsOfIncome,
                        recommendedPct = 50.0,
                        color = Color(0xFF2563EB),
                        subtitle = "Rent, food, EMI, transport, utilities"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wants
                    BudgetRuleBar(
                        title = "Personal Wants",
                        actualPct = wantsPct,
                        actualOfIncome = wantsOfIncome,
                        recommendedPct = 30.0,
                        color = Color(0xFFE11D48),
                        subtitle = "Shopping, entertainment, dining out"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Savings
                    BudgetRuleBar(
                        title = "Savings & Goals",
                        actualPct = savingsPct,
                        actualOfIncome = savingsOfIncome,
                        recommendedPct = 20.0,
                        color = if (isDark) Color(0xFF81C784) else Color(0xFF0F9D58),
                        subtitle = "Savings, investments, subscriptions"
                    )
                }
            }

            // Category Breakdown Title
            Text(
                text = "Budget Category Breakdown",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = if (isDark) Color.White else Color(0xFF141F16),
                modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
            )

            // Category list card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = GlassTheme.cardColors(),
                shape = GlassTheme.cornerRadius,
                border = GlassTheme.borderStroke()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    categories.forEachIndexed { index, cat ->
                        val pct = (cat.value / totalExpenses).toFloat()
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(cat.color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cat.label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (isDark) Color.White else Color(0xFF141F16),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${(pct * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16)
                                    )
                                }
                                Text(
                                    text = "₹" + String.format(Locale("en", "IN"), "%,.0f", cat.value),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    color = if (isDark) Color.White else Color(0xFF141F16)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = cat.color,
                                trackColor = if (isDark) Color(0x11FFFFFF) else Color(0x11000000)
                            )
                        }
                        if (index < categories.lastIndex) {
                            HorizontalDivider(
                                color = if (isDark) Color(0x11FFFFFF) else Color(0x11000000),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun ComparisonRow(
    title: String,
    total: Double,
    subtitle: String,
    color: Color
) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = if (isDark) Color.White else Color(0xFF141F16)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16)
            )
        }
        Text(
            text = "₹" + String.format(Locale.getDefault(), "%,.0f", total),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = color
        )
    }
}

@Composable
fun TimeSeriesChart(
    expenses: List<DailyExpenseEntity>,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())

    val last7DaysData = remember(expenses) {
        val result = mutableListOf<Pair<String, Double>>()
        for (i in 6 downTo 0) {
            val dCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dCal.time)
            val label = sdf.format(dCal.time)
            val sum = expenses.filter { it.date == dateStr }.sumOf { it.amount }
            result.add(Pair(label, sum))
        }
        result
    }

    val maxAmount = last7DaysData.maxOfOrNull { it.second }?.coerceAtLeast(100.0) ?: 100.0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = GlassTheme.cardColors(),
        shape = GlassTheme.cornerRadius,
        border = GlassTheme.borderStroke()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "📊 Spending Time Series",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = if (isDark) Color.White else Color(0xFF141F16)
            )
            Text(
                text = "Daily actual expenditures for the last 7 days",
                fontSize = 12.sp,
                fontFamily = FontFamily.SansSerif,
                color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                last7DaysData.forEach { (label, amount) ->
                    val barHeightFraction = (amount / maxAmount).toFloat().coerceIn(0.04f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (amount > 0) {
                            Text(
                                text = "₹${amount.toInt()}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = if (isDark) Color(0xFF81C784) else Color(0xFF0F9D58),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(barHeightFraction)
                                .width(14.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (amount > 0) {
                                        if (isDark) Color(0xFF81C784) else Color(0xFF0F9D58)
                                    } else {
                                        if (isDark) Color(0x1AFFFFFF) else Color(0x1A000000)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetRuleBar(
    title: String,
    actualPct: Double,
    actualOfIncome: Double,
    recommendedPct: Double,
    color: Color,
    subtitle: String
) {
    val isDark = isSystemInDarkTheme()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isDark) Color.White else Color(0xFF141F16)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Actual: ${actualPct.toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = color
                )
                Text(
                    text = "Rec: ${recommendedPct.toInt()}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (actualPct.toFloat() / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = if (isDark) Color(0x11FFFFFF) else Color(0x11000000)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Consumes ${actualOfIncome.toInt()}% of your income",
            fontSize = 11.sp,
            fontFamily = FontFamily.SansSerif,
            color = if (isDark) Color(0x99FFFFFF) else Color(0x99141F16)
        )
    }
}

data class ReportCategoryItem(val label: String, val value: Double, val color: Color)
