package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.focus.FocusManager
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassTheme
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Quick Calc", "Financial Tools")

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF0F9D58).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Calculator",
                    tint = Color(0xFF0F9D58),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Financial Calc",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Solve equations and plan your finances",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Beautiful glass Tab Selector
        Card(
            colors = GlassTheme.cardColors(),
            shape = RoundedCornerShape(16.dp),
            border = GlassTheme.borderStroke(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val tabBg = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                    val tabTextColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tabBg)
                            .clickable { selectedTab = index }
                            .testTag("calc_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = tabTextColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedTab == 0) {
                QuickCalcView(viewModel = viewModel)
            } else {
                FinancialToolsView(viewModel = viewModel)
            }
        }
        
        // Spacer to account for the floating navigation bar height
        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun QuickCalcView(viewModel: FinanceViewModel) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Expense saving helper sheet state
    var showSaveExpenseDialog by remember { mutableStateOf(false) }

    // Helper to evaluate simple math expressions safely without any external complex library
    fun evaluateExpression(expr: String): Double {
        // Tokenize and calculate basic additions, subtractions, multiplications, divisions
        // Note: We'll implement a clean, simple custom evaluator for arithmetic ops.
        try {
            val cleanExpr = expr.replace(" ", "").replace("×", "*").replace("÷", "/")
            if (cleanExpr.isEmpty()) return 0.0

            // Helper to evaluate basic multiplication/division
            fun evalSimple(s: String): Double {
                var sum = 0.0
                // split by + or - while keeping the operator is complex, so let's parse linearly
                val terms = mutableListOf<String>()
                val ops = mutableListOf<Char>()
                var current = StringBuilder()
                var i = 0
                while (i < s.length) {
                    val c = s[i]
                    if (c == '+' || c == '-') {
                        terms.add(current.toString())
                        ops.add(c)
                        current = StringBuilder()
                    } else {
                        current.append(c)
                    }
                    i++
                }
                terms.add(current.toString())

                // Now evaluate each term which may have * or /
                fun evalMulDiv(term: String): Double {
                    val subTerms = mutableListOf<Double>()
                    val subOps = mutableListOf<Char>()
                    var subCur = StringBuilder()
                    var j = 0
                    while (j < term.length) {
                        val c = term[j]
                        if (c == '*' || c == '/') {
                            subTerms.add(subCur.toString().toDoubleOrNull() ?: 0.0)
                            subOps.add(c)
                            subCur = StringBuilder()
                        } else {
                            subCur.append(c)
                        }
                        j++
                    }
                    subTerms.add(subCur.toString().toDoubleOrNull() ?: 0.0)

                    var res = subTerms.getOrNull(0) ?: 0.0
                    for (idx in 0 until subOps.size) {
                        val op = subOps[idx]
                        val nextVal = subTerms.getOrNull(idx + 1) ?: 0.0
                        if (op == '*') {
                            res *= nextVal
                        } else if (op == '/') {
                            res = if (nextVal != 0.0) res / nextVal else 0.0
                        }
                    }
                    return res
                }

                var total = evalMulDiv(terms[0])
                for (idx in 0 until ops.size) {
                    val op = ops[idx]
                    val nextVal = evalMulDiv(terms[idx + 1])
                    if (op == '+') {
                        total += nextVal
                    } else if (op == '-') {
                        total -= nextVal
                    }
                }
                return total
            }

            return evalSimple(cleanExpr)
        } catch (e: Exception) {
            return 0.0
        }
    }

    fun onKeyPress(key: String) {
        when (key) {
            "C" -> {
                expression = ""
                result = "0"
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "=" -> {
                try {
                    val calculated = evaluateExpression(expression)
                    // Check if is integer
                    result = if (calculated % 1.0 == 0.0) {
                        calculated.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.2f", calculated)
                    }
                } catch (e: Exception) {
                    result = "Error"
                }
            }
            "%" -> {
                if (expression.isNotEmpty() && expression.last().isDigit()) {
                    expression += "/100"
                }
            }
            "+", "-", "×", "÷" -> {
                if (expression.isNotEmpty()) {
                    val lastChar = expression.last()
                    if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷') {
                        expression = expression.dropLast(1) + key
                    } else {
                        expression += key
                    }
                }
            }
            else -> { // Numbers and dot
                expression += key
                // Auto compute temporary result in real-time
                try {
                    val temp = evaluateExpression(expression)
                    result = if (temp % 1.0 == 0.0) {
                        temp.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.2f", temp)
                    }
                } catch (e: Exception) {
                    // Ignore transient errors
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Display Screen Card
            Card(
                colors = GlassTheme.cardColors(),
                shape = RoundedCornerShape(20.dp),
                border = GlassTheme.borderStroke(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    // Running expression
                    Text(
                        text = expression.ifEmpty { "Enter equation" },
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Final result
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Quick Action Indicator (if we have a computed numeric result)
                        val numericResult = result.toDoubleOrNull() ?: 0.0
                        if (numericResult > 0) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F9D58).copy(alpha = 0.15f))
                                    .clickable { showSaveExpenseDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Save",
                                    tint = Color(0xFF0F9D58),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add as Expense",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F9D58)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Text(
                            text = result,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            // Keyboard Buttons
            val keys = listOf(
                listOf("C", "÷", "×", "⌫"),
                listOf("7", "8", "9", "-"),
                listOf("4", "5", "6", "+"),
                listOf("1", "2", "3", "%"),
                listOf("0", ".", "=")
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                keys.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            val isAction = key == "="
                            val isOperator = key == "+" || key == "-" || key == "×" || key == "÷" || key == "%"
                            val isClear = key == "C" || key == "⌫"

                            val buttonBg = when {
                                isAction -> MaterialTheme.colorScheme.primary
                                isOperator -> Color(0xFF0F9D58).copy(alpha = 0.15f)
                                isClear -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }

                            val buttonColor = when {
                                isAction -> MaterialTheme.colorScheme.onPrimary
                                isOperator -> Color(0xFF0F9D58)
                                isClear -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(if (key == "0") 2f else 1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(buttonBg)
                                    .clickable { onKeyPress(key) }
                                    .testTag("calc_key_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = buttonColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overlay dialog to Save Calculated Result as Expense
        if (showSaveExpenseDialog) {
            AlertDialog(
                onDismissRequest = { showSaveExpenseDialog = false },
                title = { Text("Add calculated amount as Expense?", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Amount: ₹$result",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Choose an expense category to add this calculated amount to your current month's transactions.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    Button(
                        onClick = { showSaveExpenseDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("Cancel")
                    }
                },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(28.dp))
            )
            // Show custom category chooser inside Dialog or just an embedded block
            var expandedCategory by remember { mutableStateOf(false) }
            val categories = listOf(
                "Food", "Groceries", "Medicine", "Education", "Transport", "Rent", "Electricity", "Internet",
                "Mobile", "EMI", "Subscriptions", "Shopping", "Entertainment",
                "Investments", "Savings", "Others"
            )
            
            AlertDialog(
                onDismissRequest = { showSaveExpenseDialog = false },
                title = { Text("Select Category", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Add ₹$result as expense to:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            val amt = result.toDoubleOrNull() ?: 0.0
                                            if (amt > 0) {
                                                // Add to VM! Let's find out how the VM saves categories
                                                // The VM's complete function is typically repository updates.
                                                // Let's call VM's update category function.
                                                // Let's save in monthlyData
                                                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                                viewModel.addDailyExpense(
                                                    date = todayStr,
                                                    category = cat,
                                                    amount = amt,
                                                    description = "Calculated Expense"
                                                )
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Logged ₹$result under $cat!")
                                                }
                                            }
                                            showSaveExpenseDialog = false
                                        }
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSaveExpenseDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    }
}

@Composable
fun FinancialToolsView(viewModel: FinanceViewModel) {
    var toolMode by remember { mutableStateOf(0) } // 0: EMI, 1: Split Bill, 2: SIP Calculator
    val tools = listOf("EMI Loan", "Split Bill", "SIP Wealth")
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Switch between different tools
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tools.forEachIndexed { index, name ->
                val selected = toolMode == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0xFF0F9D58).copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected) Color(0xFF0F9D58) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { toolMode = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color(0xFF0F9D58) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (toolMode) {
            0 -> LoanEmiView(focusManager)
            1 -> BillSplitView(focusManager)
            2 -> SipFutureValueView(focusManager)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanEmiView(focusManager: FocusManager) {
    var principal by remember { mutableStateOf("500000") }
    var rate by remember { mutableStateOf("9.5") }
    var tenure by remember { mutableStateOf("60") } // in months

    val p = principal.toDoubleOrNull() ?: 0.0
    val r = (rate.toDoubleOrNull() ?: 0.0) / 12 / 100 // monthly interest rate
    val t = tenure.toDoubleOrNull() ?: 0.0 // months

    val emi = if (p > 0 && r > 0 && t > 0) {
        (p * r * (1 + r).pow(t)) / ((1 + r).pow(t) - 1)
    } else {
        0.0
    }

    val totalPayment = if (emi > 0 && t > 0) emi * t else 0.0
    val totalInterest = if (totalPayment > p) totalPayment - p else 0.0

    Card(
        colors = GlassTheme.cardColors(),
        shape = RoundedCornerShape(20.dp),
        border = GlassTheme.borderStroke(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("EMI Calculator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            OutlinedTextField(
                value = principal,
                onValueChange = { principal = it },
                label = { Text("Loan Amount (Principal)") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F9D58),
                    focusedLabelColor = Color(0xFF0F9D58)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Interest Rate (% p.a.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F9D58),
                        focusedLabelColor = Color(0xFF0F9D58)
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = tenure,
                    onValueChange = { tenure = it },
                    label = { Text("Tenure (Months)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F9D58),
                        focusedLabelColor = Color(0xFF0F9D58)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Output Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F9D58).copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly EMI", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", emi)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F9D58))
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Interest", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", totalInterest)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", totalPayment)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillSplitView(focusManager: FocusManager) {
    var totalAmount by remember { mutableStateOf("1200") }
    var splitCount by remember { mutableStateOf("4") }
    var tipPercent by remember { mutableStateOf("10") }

    val bill = totalAmount.toDoubleOrNull() ?: 0.0
    val friends = splitCount.toIntOrNull() ?: 1
    val tip = tipPercent.toDoubleOrNull() ?: 0.0

    val calculatedTip = bill * (tip / 100)
    val totalBill = bill + calculatedTip
    val share = if (friends > 0) totalBill / friends else totalBill

    Card(
        colors = GlassTheme.cardColors(),
        shape = RoundedCornerShape(20.dp),
        border = GlassTheme.borderStroke(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Split Bill", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            OutlinedTextField(
                value = totalAmount,
                onValueChange = { totalAmount = it },
                label = { Text("Total Bill Amount") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F9D58),
                    focusedLabelColor = Color(0xFF0F9D58)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = splitCount,
                    onValueChange = { splitCount = it },
                    label = { Text("Number of People") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F9D58),
                        focusedLabelColor = Color(0xFF0F9D58)
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = tipPercent,
                    onValueChange = { tipPercent = it },
                    label = { Text("Tip Percentage (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F9D58),
                        focusedLabelColor = Color(0xFF0F9D58)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Output Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F9D58).copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Each Person's Share", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", share)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F9D58))
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Calculated Tip", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", calculatedTip)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount Due", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", totalBill)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SipFutureValueView(focusManager: FocusManager) {
    var monthlySip by remember { mutableStateOf("5000") }
    var expectedReturn by remember { mutableStateOf("12") }
    var timePeriod by remember { mutableStateOf("10") } // in years

    val p = monthlySip.toDoubleOrNull() ?: 0.0
    val i = (expectedReturn.toDoubleOrNull() ?: 0.0) / 12 / 100 // monthly interest
    val n = (timePeriod.toDoubleOrNull() ?: 0.0) * 12 // total months

    val totalInvested = p * n
    val futureValue = if (p > 0 && i > 0 && n > 0) {
        p * (((1 + i).pow(n) - 1) / i) * (1 + i)
    } else {
        0.0
    }
    val wealthGained = if (futureValue > totalInvested) futureValue - totalInvested else 0.0

    Card(
        colors = GlassTheme.cardColors(),
        shape = RoundedCornerShape(20.dp),
        border = GlassTheme.borderStroke(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("SIP Wealth Calculator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            OutlinedTextField(
                value = monthlySip,
                onValueChange = { monthlySip = it },
                label = { Text("Monthly SIP Amount") },
                prefix = { Text("₹ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0F9D58),
                    focusedLabelColor = Color(0xFF0F9D58)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = expectedReturn,
                    onValueChange = { expectedReturn = it },
                    label = { Text("Expected Return (% p.a.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F9D58),
                        focusedLabelColor = Color(0xFF0F9D58)
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = timePeriod,
                    onValueChange = { timePeriod = it },
                    label = { Text("Duration (Years)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F9D58),
                        focusedLabelColor = Color(0xFF0F9D58)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Output Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F9D58).copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Future Wealth Value", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", futureValue)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F9D58))
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Invested", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", totalInvested)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Wealth Gained", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${String.format(Locale.US, "%,.2f", wealthGained)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
