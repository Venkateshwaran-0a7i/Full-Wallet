package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.theme.GlassTheme
import kotlinx.coroutines.launch

@Composable
fun ExpensesScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val monthlyData by viewModel.monthlyData.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Transient local states for forms
    var income by remember { mutableStateOf("") }
    var bonus by remember { mutableStateOf("") }
    var groceries by remember { mutableStateOf("") }
    var medicine by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var food by remember { mutableStateOf("") }
    var transport by remember { mutableStateOf("") }
    var electricity by remember { mutableStateOf("") }
    var internet by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var emi by remember { mutableStateOf("") }
    var subscriptions by remember { mutableStateOf("") }
    var shopping by remember { mutableStateOf("") }
    var entertainment by remember { mutableStateOf("") }
    var investments by remember { mutableStateOf("") }
    var savings by remember { mutableStateOf("") }
    var others by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }

    // Auto-retrieve and synchronize data from storage automatically
    LaunchedEffect(monthlyData) {
        fun fmt(v: Double?) = v?.let { if (it > 0.0) (if (it % 1 == 0.0) String.format("%.0f", it) else it.toString()) else "" } ?: ""
        income = fmt(monthlyData.income)
        bonus = fmt(monthlyData.bonus)
        groceries = fmt(monthlyData.groceries)
        medicine = fmt(monthlyData.medicine)
        education = fmt(monthlyData.education)
        rent = fmt(monthlyData.rent)
        food = fmt(monthlyData.food)
        transport = fmt(monthlyData.transport)
        electricity = fmt(monthlyData.electricity)
        internet = fmt(monthlyData.internet)
        mobile = fmt(monthlyData.mobile)
        emi = fmt(monthlyData.emi)
        subscriptions = fmt(monthlyData.subscriptions)
        shopping = fmt(monthlyData.shopping)
        entertainment = fmt(monthlyData.entertainment)
        investments = fmt(monthlyData.investments)
        savings = fmt(monthlyData.savings)
        others = fmt(monthlyData.others)
        goal = monthlyData.goal
    }

    val onSavePlan = {
        focusManager.clearFocus()
        val dIncome = income.toDoubleOrNull()
        val dBonus = bonus.toDoubleOrNull()
        val dGroceries = groceries.toDoubleOrNull()
        val dMedicine = medicine.toDoubleOrNull()
        val dEducation = education.toDoubleOrNull()
        val dRent = rent.toDoubleOrNull()
        val dFood = food.toDoubleOrNull()
        val dTransport = transport.toDoubleOrNull()
        val dElectricity = electricity.toDoubleOrNull()
        val dInternet = internet.toDoubleOrNull()
        val dMobile = mobile.toDoubleOrNull()
        val dEmi = emi.toDoubleOrNull()
        val dSubs = subscriptions.toDoubleOrNull()
        val dShopping = shopping.toDoubleOrNull()
        val dEntertainment = entertainment.toDoubleOrNull()
        val dInvest = investments.toDoubleOrNull()
        val dSavings = savings.toDoubleOrNull()
        val dOthers = others.toDoubleOrNull()

        viewModel.updateMonthlyData(
            income = dIncome,
            bonus = dBonus,
            groceries = dGroceries,
            medicine = dMedicine,
            education = dEducation,
            rent = dRent,
            food = dFood,
            transport = dTransport,
            electricity = dElectricity,
            internet = dInternet,
            mobile = dMobile,
            emi = dEmi,
            subscriptions = dSubs,
            shopping = dShopping,
            entertainment = dEntertainment,
            investments = dInvest,
            savings = dSavings,
            others = dOthers,
            goal = goal
        )

        scope.launch {
            snackbarHostState.showSnackbar("Financial plan updated successfully!")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("expenses_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // Title Header
            Text(
                text = "Bento Budgeting",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Enter your monthly income and plan your category expenses",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Income & Bonus Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = GlassTheme.cornerRadius,
                colors = GlassTheme.cardColors(),
                border = GlassTheme.borderStroke()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Salary Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Monthly Salary (Base Income)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = income,
                                onValueChange = { income = it },
                                placeholder = { Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                prefix = { Text("₹", modifier = Modifier.padding(end = 4.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("income_input")
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    // Bonus / Extra Income Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Bonus / Additional Income",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Adds to Balance",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Earned extra from a side project, freelancing, OT, or bonus? Add it here. It instantly boosts your current balance without affecting your fixed salary schedule.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = bonus,
                                onValueChange = { bonus = it },
                                placeholder = { Text("0 (Optional)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                prefix = { Text("₹", modifier = Modifier.padding(end = 4.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("bonus_input")
                            )
                        }
                    }
                }
            }

            // Section Label
            Text(
                text = "Monthly Category Budgets",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Bento Grid of expense inputs (2-column layout)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Groceries & Medicine
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Groceries",
                            value = groceries,
                            onValueChange = { groceries = it },
                            icon = Icons.Default.ShoppingCart,
                            iconColor = Color(0xFF059669),
                            iconBg = Color(0xFFD1FAE5),
                            testTag = "groceries_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Medicine",
                            value = medicine,
                            onValueChange = { medicine = it },
                            icon = Icons.Default.MedicalServices,
                            iconColor = Color(0xFFE11D48),
                            iconBg = Color(0xFFFFE4E6),
                            testTag = "medicine_input"
                        )
                    }
                }

                // Row 2: Education & Rent
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Education",
                            value = education,
                            onValueChange = { education = it },
                            icon = Icons.Default.School,
                            iconColor = Color(0xFF8B5CF6),
                            iconBg = Color(0xFFEDE9FE),
                            testTag = "education_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Rent",
                            value = rent,
                            onValueChange = { rent = it },
                            icon = Icons.Default.Home,
                            iconColor = Color(0xFF2563EB),
                            iconBg = Color(0xFFDBEAFE),
                            testTag = "rent_input"
                        )
                    }
                }

                // Row 3: Food & Transport
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Food",
                            value = food,
                            onValueChange = { food = it },
                            icon = Icons.Default.Fastfood,
                            iconColor = Color(0xFFD97706),
                            iconBg = Color(0xFFFFE4E6), // warm tone
                            testTag = "food_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Transport",
                            value = transport,
                            onValueChange = { transport = it },
                            icon = Icons.Default.DirectionsCar,
                            iconColor = Color(0xFF059669),
                            iconBg = Color(0xFFD1FAE5),
                            testTag = "transport_input"
                        )
                    }
                }

                // Row 4: Electricity & Internet
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Electricity",
                            value = electricity,
                            onValueChange = { electricity = it },
                            icon = Icons.Default.ElectricBolt,
                            iconColor = Color(0xFFCA8A04),
                            iconBg = Color(0xFFFEF9C3),
                            testTag = "electricity_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Internet",
                            value = internet,
                            onValueChange = { internet = it },
                            icon = Icons.Default.Wifi,
                            iconColor = Color(0xFF7C3AED),
                            iconBg = Color(0xFFF3E8FF),
                            testTag = "internet_input"
                        )
                    }
                }

                // Row 5: Mobile & EMI
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Mobile",
                            value = mobile,
                            onValueChange = { mobile = it },
                            icon = Icons.Default.Smartphone,
                            iconColor = Color(0xFFDB2777),
                            iconBg = Color(0xFFFCE7F3),
                            testTag = "mobile_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "EMI",
                            value = emi,
                            onValueChange = { emi = it },
                            icon = Icons.Default.CreditCard,
                            iconColor = Color(0xFFDC2626),
                            iconBg = Color(0xFFFEE2E2),
                            testTag = "emi_input"
                        )
                    }
                }

                // Row 6: Subscriptions & Shopping
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Subscriptions",
                            value = subscriptions,
                            onValueChange = { subscriptions = it },
                            icon = Icons.Default.Subscriptions,
                            iconColor = Color(0xFF0D9488),
                            iconBg = Color(0xFFCCFBF1),
                            testTag = "subscriptions_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Shopping",
                            value = shopping,
                            onValueChange = { shopping = it },
                            icon = Icons.Default.ShoppingBag,
                            iconColor = Color(0xFF7C3AED),
                            iconBg = Color(0xFFEDE9FE),
                            testTag = "shopping_input"
                        )
                    }
                }

                // Row 7: Entertainment & Investments
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Entertainment",
                            value = entertainment,
                            onValueChange = { entertainment = it },
                            icon = Icons.Default.Tv,
                            iconColor = Color(0xFFE11D48),
                            iconBg = Color(0xFFFFE4E6),
                            testTag = "entertainment_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Investments",
                            value = investments,
                            onValueChange = { investments = it },
                            icon = Icons.Default.TrendingUp,
                            iconColor = Color(0xFF059669),
                            iconBg = Color(0xFFD1FAE5),
                            testTag = "investments_input"
                        )
                    }
                }

                // Row 8: Savings & Others
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Savings",
                            value = savings,
                            onValueChange = { savings = it },
                            icon = Icons.Default.Savings,
                            iconColor = Color(0xFF2563EB),
                            iconBg = Color(0xFFDBEAFE),
                            testTag = "savings_input"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BentoCard(
                            label = "Others",
                            value = others,
                            onValueChange = { others = it },
                            icon = Icons.Default.MoreHoriz,
                            iconColor = Color(0xFF6B7280),
                            iconBg = Color(0xFFF3F4F6),
                            testTag = "others_input"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Financial Goal Section
            Text(
                text = "What is your main financial goal?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp), // Spacious bottom so scrolling clear of floating action button
                shape = GlassTheme.cornerRadius,
                colors = GlassTheme.cardColors(),
                border = GlassTheme.borderStroke()
            ) {
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    placeholder = { Text("E.g., Try to save for a new vacation, keep restaurant visits low...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(110.dp)
                        .testTag("financial_goal_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    maxLines = 4,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        // Beautiful floating check action button to save the entire budgeting form
        ExtendedFloatingActionButton(
            onClick = { onSavePlan() },
            icon = { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(24.dp)) },
            text = { Text("Save Budget", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
                .navigationBarsPadding() // Respect bottom gesture navbar
                .testTag("save_plan_fab"),
            shape = RoundedCornerShape(16.dp)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }
}

@Composable
fun BentoCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = GlassTheme.cornerRadius,
        colors = GlassTheme.cardColors(),
        border = GlassTheme.borderStroke()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(testTag),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "0",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}
