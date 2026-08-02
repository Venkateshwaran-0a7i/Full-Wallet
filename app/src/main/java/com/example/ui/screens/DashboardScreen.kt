package com.example.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.DailyExpenseEntity
import com.example.data.entity.MonthlyDataEntity
import com.example.data.entity.SavingsGoalEntity
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.theme.GlassTheme
import java.text.SimpleDateFormat
import java.util.*

// Helper format INR
fun formatInr(value: Double?): String {
    if (value == null || value == 0.0) return "—"
    return "₹" + String.format(Locale("en", "IN"), "%,.0f", value)
}

// Helper models for listing
data class ExpenseItem(val label: String, val value: Double, val color: Color)

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToExpenses: () -> Unit,
    onNavigateToGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val monthlyData by viewModel.monthlyData.collectAsState()
    val goals by viewModel.savingsGoals.collectAsState()
    val dailyExpenses by viewModel.dailyExpenses.collectAsState()

    val totalExpenses = viewModel.calculateTotalExpenses(monthlyData)
    val balance = viewModel.calculateBalance(monthlyData, dailyExpenses)
    val hasData = viewModel.hasFinancialData(monthlyData)
    val totalIncome = (monthlyData.income ?: 0.0) + (monthlyData.bonus ?: 0.0)

    val budgetPercent = if (totalIncome > 0.0) {
        val pct = (totalExpenses / totalIncome) * 100
        pct.coerceAtMost(100.0)
    } else 0.0

    // Top Expenses categories derived from monthly settings
    val topExpenses = listOf(
        ExpenseItem("Groceries", monthlyData.groceries ?: 0.0, Color(0xFF059669)),
        ExpenseItem("Medicine", monthlyData.medicine ?: 0.0, Color(0xFFE11D48)),
        ExpenseItem("Education", monthlyData.education ?: 0.0, Color(0xFF8B5CF6)),
        ExpenseItem("Rent", monthlyData.rent ?: 0.0, Color(0xFF2563EB)),
        ExpenseItem("Food", monthlyData.food ?: 0.0, Color(0xFFD97706)),
        ExpenseItem("Transport", monthlyData.transport ?: 0.0, Color(0xFF059669)),
        ExpenseItem("EMI", monthlyData.emi ?: 0.0, Color(0xFFDC2626)),
        ExpenseItem("Shopping", monthlyData.shopping ?: 0.0, Color(0xFF7C3AED)),
        ExpenseItem("Entertainment", monthlyData.entertainment ?: 0.0, Color(0xFFE11D48)),
        ExpenseItem("Electricity", monthlyData.electricity ?: 0.0, Color(0xFFCA8A04)),
        ExpenseItem("Internet", monthlyData.internet ?: 0.0, Color(0xFF7C3AED)),
        ExpenseItem("Mobile", monthlyData.mobile ?: 0.0, Color(0xFFDB2777)),
        ExpenseItem("Subscriptions", monthlyData.subscriptions ?: 0.0, Color(0xFF0D9488)),
        ExpenseItem("Investments", monthlyData.investments ?: 0.0, Color(0xFF059669)),
        ExpenseItem("Savings", monthlyData.savings ?: 0.0, Color(0xFF2563EB)),
        ExpenseItem("Others", monthlyData.others ?: 0.0, Color(0xFF6B7280))
    )
        .filter { it.value > 0.0 }
        .sortedByDescending { it.value }
        .take(5)

    // Calendar state
    var calendarYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-based
    var selectedDayNum by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    val selectedDateString = remember(calendarYear, calendarMonth, selectedDayNum) {
        String.format(Locale.getDefault(), "%04d-%02d-%02d", calendarYear, calendarMonth + 1, selectedDayNum)
    }

    // Gemini assistant state
    var geminiInputText by remember { mutableStateOf("") }
    val isGeminiProcessing by viewModel.isGeminiProcessing.collectAsState()
    val geminiFeedbackMessage by viewModel.geminiFeedback.collectAsState()

    // Add Expense dialog
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<DailyExpenseEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_root")
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.full_wallet_1784010019703),
                contentDescription = "Full Wallet Logo",
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Full Wallet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your financial overview",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- SECTION 1: Gemini Natural Language Assistant ---
        GeminiAssistantCard(
            inputText = geminiInputText,
            onInputTextChange = { geminiInputText = it },
            isProcessing = isGeminiProcessing,
            feedback = geminiFeedbackMessage,
            onSubmit = {
                viewModel.askGemini(geminiInputText)
                geminiInputText = ""
            },
            onClearFeedback = { viewModel.clearGeminiFeedback() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Balance Card
        val isNegativeBalance = hasData && balance < 0.0
        val heroBg = if (isNegativeBalance) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
        val onHeroTextColor = Color.White

        val balanceGradient = if (isNegativeBalance) {
            Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
        } else {
            Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF047857)))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(balanceGradient)
                    .fillMaxWidth()
                    .padding(26.dp)
            ) {
                Text(
                    text = "Current Balance",
                    fontSize = 13.sp,
                    color = onHeroTextColor.copy(alpha = 0.72f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (hasData) "₹" + String.format(Locale("en", "IN"), "%,.0f", balance) else "—",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = onHeroTextColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        !hasData -> "Add your expenses to begin tracking"
                        balance < 0.0 -> "Expenses exceed income"
                        else -> "You are on track this month"
                    },
                    fontSize = 13.sp,
                    color = onHeroTextColor.copy(alpha = 0.60f)
                )
            }
        }

        // Summary Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SummaryCard(
                    label = if ((monthlyData.bonus ?: 0.0) > 0.0) "Total Income" else "Monthly Income",
                    value = formatInr(totalIncome),
                    icon = Icons.Outlined.TrendingUp,
                    iconTint = Color(0xFF16A34A),
                    iconBg = Color(0xFFDCFCE7),
                    iconBgDark = Color(0xFF14532D)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SummaryCard(
                    label = "Total Expenses",
                    value = if (totalExpenses > 0.0) "₹" + String.format(Locale("en", "IN"), "%,.0f", totalExpenses) else "—",
                    icon = Icons.Outlined.TrendingDown,
                    iconTint = Color(0xFFDC2626),
                    iconBg = Color(0xFFFEE2E2),
                    iconBgDark = Color(0xFF450A0A)
                )
            }
        }
        if ((monthlyData.bonus ?: 0.0) > 0.0) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCard(
                        label = "Base Salary",
                        value = formatInr(monthlyData.income),
                        icon = Icons.Outlined.Payments,
                        iconTint = Color(0xFF0284C7),
                        iconBg = Color(0xFFE0F2FE),
                        iconBgDark = Color(0xFF0C4A6E)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SummaryCard(
                        label = "Bonus / Extra",
                        value = formatInr(monthlyData.bonus),
                        icon = Icons.Default.Stars,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        iconBgDark = Color(0xFF451A03)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                SummaryCard(
                    label = "Savings",
                    value = formatInr(monthlyData.savings),
                    icon = Icons.Default.Save,
                    iconTint = Color(0xFF2563EB),
                    iconBg = Color(0xFFDBEAFE),
                    iconBgDark = Color(0xFF1E3A5F)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SummaryCard(
                    label = "Investments",
                    value = formatInr(monthlyData.investments),
                    icon = Icons.Default.TrendingUp,
                    iconTint = Color(0xFF7C3AED),
                    iconBg = Color(0xFFEDE9FE),
                    iconBgDark = Color(0xFF2E1065)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION 2: Interactive Finance Calendar ---
        InteractiveCalendarSection(
            viewModel = viewModel,
            calendarYear = calendarYear,
            calendarMonth = calendarMonth,
            selectedDayNum = selectedDayNum,
            dailyExpenses = dailyExpenses,
            onMonthPrev = {
                if (calendarMonth == 0) {
                    calendarMonth = 11
                    calendarYear -= 1
                } else {
                    calendarMonth -= 1
                }
                selectedDayNum = 1
            },
            onMonthNext = {
                if (calendarMonth == 11) {
                    calendarMonth = 0
                    calendarYear += 1
                } else {
                    calendarMonth += 1
                }
                selectedDayNum = 1
            },
            onDayClick = { selectedDayNum = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Day specific logs display
        DailyExpenseLogsSection(
            selectedDate = selectedDateString,
            dailyExpenses = dailyExpenses,
            onAddClick = { showAddExpenseDialog = true },
            onEditClick = { expenseToEdit = it },
            onDeleteClick = { viewModel.deleteDailyExpense(it) },
            onSyncCalendarClick = { exp ->
                try {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, "[FullWallet] ${exp.category} Expense")
                        putExtra(CalendarContract.Events.DESCRIPTION, "Description: ${exp.description}\nAmount: ₹${exp.amount}\nLogged offline via Full Wallet App.")
                        putExtra(CalendarContract.Events.EVENT_LOCATION, "Local App Log")

                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateObj = sdf.parse(exp.date)
                        val startTime = dateObj?.time ?: System.currentTimeMillis()
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + 30 * 60 * 1000) // 30 mins
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                }
            },
            salaryDayOfMonth = viewModel.salaryDayOfMonth.collectAsState().value,
            selectedDayNum = selectedDayNum,
            onSalaryCalendarSync = {
                try {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, "💰 Salary Credit [FullWallet]")
                        putExtra(CalendarContract.Events.DESCRIPTION, "Monthly salary credited today! Monitor your budgets and savings on Full Wallet.")

                        val cal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, calendarYear)
                            set(Calendar.MONTH, calendarMonth)
                            set(Calendar.DAY_OF_MONTH, selectedDayNum)
                        }
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 60 * 60 * 1000)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Budget Progress
        if (hasData && totalIncome > 0.0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                colors = GlassTheme.cardColors(),
                shape = GlassTheme.cornerRadius,
                border = GlassTheme.borderStroke()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Budget Progress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val progressFillColor = when {
                        budgetPercent > 90.0 -> Color(0xFFEF4444)
                        budgetPercent > 70.0 -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    LinearProgressIndicator(
                        progress = { budgetPercent.toFloat() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = progressFillColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${budgetPercent.toInt()}% of income used · ₹" +
                                String.format(Locale("en", "IN"), "%,.0f", (totalIncome - totalExpenses).coerceAtLeast(0.0)) +
                                " remaining",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Top Expenses
        if (topExpenses.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                colors = GlassTheme.cardColors(),
                shape = GlassTheme.cornerRadius,
                border = GlassTheme.borderStroke()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Top Monthly Category Targets",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    topExpenses.forEach { exp ->
                        val pct = (exp.value / totalExpenses).toFloat()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(exp.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = exp.label,
                                fontSize = 13.sp,
                                modifier = Modifier.width(90.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = exp.color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹" + String.format(Locale("en", "IN"), "%,.0f", exp.value),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(80.dp),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        // Savings Goals
        if (goals.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
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
                            text = "Active Savings Goals",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "See all",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .testTag("see_all_goals_button")
                                .clickable { onNavigateToGoals() }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    goals.take(2).forEach { goal ->
                        val prog = if (goal.targetAmount > 0) {
                            (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                        } else 0f

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = goal.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "₹" + String.format(Locale("en", "IN"), "%,.0f", goal.savedAmount) +
                                            " / ₹" + String.format(Locale("en", "IN"), "%,.0f", goal.targetAmount),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { prog },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Financial Goal Card
        if (monthlyData.goal.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                colors = GlassTheme.cardColors(),
                shape = GlassTheme.cornerRadius,
                border = GlassTheme.borderStroke()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Financial Goal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = monthlyData.goal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Add Daily Expense Manual Dialog
    if (showAddExpenseDialog) {
        var expenseAmount by remember { mutableStateOf("") }
        var expenseDesc by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("Others") }

        val categories = listOf(
            "Food", "Groceries", "Medicine", "Education", "Transport", "Rent", "Electricity", "Internet",
            "Mobile", "EMI", "Subscriptions", "Shopping", "Entertainment",
            "Investments", "Savings", "Others"
        )

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Log Expense for $selectedDateString", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = expenseDesc,
                        onValueChange = { expenseDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.addDailyExpense(
                                date = selectedDateString,
                                category = selectedCategory,
                                amount = amt,
                                description = expenseDesc.ifBlank { selectedCategory }
                            )
                            showAddExpenseDialog = false
                        }
                    }
                ) {
                    Text("Save Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Edit Daily Expense Log Dialog
    if (expenseToEdit != null) {
        val exp = expenseToEdit!!
        var expenseAmount by remember(exp) { mutableStateOf(if (exp.amount % 1 == 0.0) String.format("%.0f", exp.amount) else exp.amount.toString()) }
        var expenseDesc by remember(exp) { mutableStateOf(exp.description) }
        var selectedCategory by remember(exp) { mutableStateOf(exp.category) }

        val categories = listOf(
            "Food", "Groceries", "Medicine", "Education", "Transport", "Rent", "Electricity", "Internet",
            "Mobile", "EMI", "Subscriptions", "Shopping", "Entertainment",
            "Investments", "Savings", "Others"
        )

        AlertDialog(
            onDismissRequest = { expenseToEdit = null },
            title = { Text("Edit Expense Log", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = expenseDesc,
                        onValueChange = { expenseDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = expenseAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.updateDailyExpense(
                                id = exp.id,
                                date = exp.date,
                                category = selectedCategory,
                                amount = amt,
                                description = expenseDesc.ifBlank { selectedCategory }
                            )
                            expenseToEdit = null
                        }
                    }
                ) {
                    Text("Update Log")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToEdit = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// --- SUB-COMPONENTS ---



@Composable
fun InteractiveCalendarSection(
    viewModel: FinanceViewModel,
    calendarYear: Int,
    calendarMonth: Int,
    selectedDayNum: Int,
    dailyExpenses: List<DailyExpenseEntity>,
    onMonthPrev: () -> Unit,
    onMonthNext: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    val salaryDayOfMonth = viewModel.salaryDayOfMonth.collectAsState().value

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Calculate Calendar grid
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, calendarYear)
        set(Calendar.MONTH, calendarMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday...
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val blankSlots = firstDayOfWeek - 1
    val totalSlots = blankSlots + maxDays

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = GlassTheme.cardColors(),
        shape = GlassTheme.cornerRadius,
        border = GlassTheme.borderStroke()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Switcher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${monthNames[calendarMonth]} $calendarYear",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onMonthPrev) {
                        Icon(Icons.Default.ArrowBackIos, contentDescription = "Prev Month", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMonthNext) {
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Month", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers
            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Days Grid (laid out dynamically using rows)
            val rowsCount = (totalSlots + 6) / 7
            for (row in 0 until rowsCount) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0 until 7) {
                        val index = row * 7 + col
                        if (index < blankSlots || index >= totalSlots) {
                            Box(modifier = Modifier.weight(1f))
                        } else {
                            val dayNum = index - blankSlots + 1
                            val isSelected = dayNum == selectedDayNum

                            val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", calendarYear, calendarMonth + 1, dayNum)
                            val hasSalary = dayNum == salaryDayOfMonth

                            // Calculate daily total
                            val daySum = dailyExpenses
                                .filter { it.date == dateStr }
                                .sumOf { it.amount }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            hasSalary -> Color(0xFFDCFCE7) // Light salary green
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDayClick(dayNum) }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || hasSalary) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            hasSalary -> Color(0xFF16A34A)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    if (daySum > 0) {
                                        Text(
                                            text = "₹${daySum.toInt()}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else if (hasSalary) {
                                        Text(
                                            text = "Salary",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyExpenseLogsSection(
    selectedDate: String,
    dailyExpenses: List<DailyExpenseEntity>,
    onAddClick: () -> Unit,
    onEditClick: (DailyExpenseEntity) -> Unit,
    onDeleteClick: (String) -> Unit,
    onSyncCalendarClick: (DailyExpenseEntity) -> Unit,
    salaryDayOfMonth: Int,
    selectedDayNum: Int,
    onSalaryCalendarSync: () -> Unit
) {
    val logsOnDay = dailyExpenses.filter { it.date == selectedDate }
    val dayTotal = logsOnDay.sumOf { it.amount }
    val isSalaryDay = selectedDayNum == salaryDayOfMonth

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = GlassTheme.cardColors(),
        shape = GlassTheme.cornerRadius,
        border = GlassTheme.borderStroke()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Logs for $selectedDate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (dayTotal > 0) {
                        Text(
                            text = "Daily Total: ₹${String.format(Locale("en", "IN"), "%,.0f", dayTotal)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isSalaryDay) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .clickable { onSalaryCalendarSync() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Salary Credit Scheduled today. Sync to Google Calendar?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF15803D),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (logsOnDay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions logged on this day.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    logsOnDay.forEach { exp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.description,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = exp.category,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "₹" + String.format(Locale("en", "IN"), "%,.0f", exp.amount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )

                                IconButton(onClick = { onEditClick(exp) }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Log",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(onClick = { onSyncCalendarClick(exp) }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Sync to Google Calendar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(onClick = { onDeleteClick(exp.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    iconBgDark: Color,
    modifier: Modifier = Modifier
) {
    val isSystemDark = isSystemInDarkTheme()
    val bg = if (isSystemDark) iconBgDark else iconBg

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = GlassTheme.cardColors(),
        shape = GlassTheme.cornerRadius,
        border = GlassTheme.borderStroke()
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = iconTint
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = label,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
    )
}

@Composable
fun GeminiAssistantCard(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isProcessing: Boolean,
    feedback: String?,
    onSubmit: () -> Unit,
    onClearFeedback: () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val geminiBorderColor = if (darkTheme) Color(0x66A78BFA) else Color(0x667C3AED)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = GlassTheme.cardColors(),
        shape = GlassTheme.cornerRadius,
        border = BorderStroke(1.dp, geminiBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF3E8FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Icon",
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini Finance Assistant",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = { Text("e.g. 'Spent 150 on dinner today' or ask anything", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                IconButton(
                    onClick = onSubmit,
                    enabled = !isProcessing && inputText.isNotBlank(),
                    modifier = Modifier
                        .background(
                            if (inputText.isNotBlank() && !isProcessing) Color(0xFF7C3AED) else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .size(40.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (!feedback.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val feedbackBg = if (darkTheme) Color(0xFF1F1530) else Color(0xFFF3E8FF)
                val feedbackBorder = if (darkTheme) Color(0xFF9F6BFA) else Color(0xFF7C3AED)
                val feedbackTitleColor = if (darkTheme) Color(0xFFC084FC) else Color(0xFF7C3AED)
                val feedbackTextColor = if (darkTheme) Color(0xFFF3E8FF) else Color(0xFF1F1530)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(feedbackBg, RoundedCornerShape(10.dp))
                        .border(1.dp, feedbackBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gemini Response",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = feedbackTitleColor
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = feedbackTitleColor,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onClearFeedback() }
                            )
                        }
                        Text(
                            text = feedback,
                            fontSize = 13.sp,
                            color = feedbackTextColor,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
