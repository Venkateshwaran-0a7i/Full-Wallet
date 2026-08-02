package com.example

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyWalletIsFullTheme
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Calendar

class MainActivity : FragmentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        setContent {
            val themePreference by viewModel.themePreference.collectAsState()
            val accentColorPreference by viewModel.accentColorPreference.collectAsState()

            MyWalletIsFullTheme(
                themePreference = themePreference,
                accentColorPreference = accentColorPreference
            ) {
                MainAppLayout(viewModel = viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val filledIcon: ImageVector, val outlinedIcon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Expenses : Screen("expenses", "Expenses", Icons.Filled.Edit, Icons.Outlined.Edit)
    object Goals : Screen("goals", "Goals", Icons.Filled.Flag, Icons.Outlined.Flag)
    object Reports : Screen("reports", "Reports", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    object Calculator : Screen("calculator", "Calc", Icons.Filled.Calculate, Icons.Outlined.Calculate)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainAppLayout(viewModel: FinanceViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    val navigationItems = listOf(
        Screen.Dashboard,
        Screen.Expenses,
        Screen.Goals,
        Screen.Reports,
        Screen.Calculator,
        Screen.Settings
    )

    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    var isUnlocked by remember { mutableStateOf(!biometricEnabled) }
    var biometricError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(biometricEnabled) {
        if (!biometricEnabled) {
            isUnlocked = true
        }
    }

    LaunchedEffect(biometricEnabled, isUnlocked) {
        if (biometricEnabled && !isUnlocked) {
            triggerBiometricPrompt(
                context = context,
                onSuccess = {
                    isUnlocked = true
                    biometricError = null
                },
                onError = { err: String ->
                    biometricError = err
                }
            )
        }
    }

    val themePref by viewModel.themePreference.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themePref.lowercase()) {
        "dark" -> true
        "light" -> false
        "system" -> isSystemDark
        else -> true
    }

    // Glassmorphic iOS diagonal linear gradient brush background
    val backgroundBrush = if (darkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0F1E15), // Very deep green-slate
                Color(0xFF070E0B), // Near black forest
                Color(0xFF050B14)  // Rich deep dark blue-black
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF1F5F9), // Clean light slate
                Color(0xFFF8FAFC), // Off-white
                Color(0xFFE2E8F0)  // Light subtle slate
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        if (biometricEnabled && !isUnlocked) {
            BiometricLockScreen(
                onUnlockRequested = {
                    triggerBiometricPrompt(
                        context = context,
                        onSuccess = {
                            isUnlocked = true
                            biometricError = null
                        },
                        onError = { err: String ->
                            biometricError = err
                        }
                    )
                },
                errorMessage = biometricError
            )
        } else {
            Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Transparent scaffold to show the glorious glass gradient background
            bottomBar = {
                // Floating Translucent Liquid Glass Bottom Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.98f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (darkTheme) Color(0x99000000) else Color(0xDDFFFFFF)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(
                            1.5.dp,
                            if (darkTheme) Color(0x55FFFFFF) else Color(0xFFCBD5E1)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navigationItems.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.Transparent)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) screen.filledIcon else screen.outlinedIcon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) {
                                                if (darkTheme) Color.White else Color.Black
                                            } else {
                                                if (darkTheme) Color(0x66FFFFFF) else Color(0x66000000)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif,
                                        color = if (isSelected) {
                                            if (darkTheme) Color.White else Color.Black
                                        } else {
                                            if (darkTheme) Color(0x77FFFFFF) else Color(0x77000000)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            val getScreenIndex: (String?) -> Int = { route ->
                val idx = navigationItems.indexOfFirst { it.route == route }
                if (idx >= 0) idx else 0
            }

            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val initialIdx = getScreenIndex(initialState.destination.route)
                    val targetIdx = getScreenIndex(targetState.destination.route)
                    if (targetIdx >= initialIdx) {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(280))
                    } else {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(280))
                    }
                },
                exitTransition = {
                    val initialIdx = getScreenIndex(initialState.destination.route)
                    val targetIdx = getScreenIndex(targetState.destination.route)
                    if (targetIdx >= initialIdx) {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(280))
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(280, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(280))
                    }
                }
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToExpenses = {
                            navController.navigate(Screen.Expenses.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToGoals = {
                            navController.navigate(Screen.Goals.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.Expenses.route) {
                    ExpensesScreen(viewModel = viewModel)
                }
                composable(Screen.Goals.route) {
                    GoalsScreen(viewModel = viewModel)
                }
                composable(Screen.Reports.route) {
                    ReportsScreen(viewModel = viewModel)
                }
                composable(Screen.Calculator.route) {
                    CalculatorScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Beautiful iOS Setup/Onboarding Dialog Overlay
        AnimatedVisibility(
            visible = !onboardingCompleted,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .verticalScroll(rememberScrollState()),
                    colors = CardDefaults.cardColors(
                        containerColor = if (darkTheme) Color(0xFA14211A) else Color(0xFAFFFFFF)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, if (darkTheme) Color(0x33FFFFFF) else Color(0x1A000000)),
                    elevation = CardDefaults.cardElevation(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👋 Welcome to FullWallet",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (darkTheme) Color.White else Color(0xFF141F16),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Please enter your monthly salary and pay schedule to initialize your budgets.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = if (darkTheme) Color(0xB3FFFFFF) else Color(0x99141F16),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        var salaryInput by remember { mutableStateOf("") }
                        var creditDay by remember { mutableStateOf(1) }

                        OutlinedTextField(
                            value = salaryInput,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    salaryInput = input
                                }
                            },
                            label = { Text("Monthly Salary (e.g. 50000)", fontFamily = FontFamily.SansSerif) },
                            placeholder = { Text("Enter Amount", fontFamily = FontFamily.SansSerif) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF0F9D58),
                                focusedLabelColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF0F9D58),
                                cursorColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF0F9D58)
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Salary Credit Date: Every ${creditDay}th",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (darkTheme) Color(0xFF81C784) else Color(0xFF0F9D58),
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = creditDay.toFloat(),
                            onValueChange = { creditDay = it.toInt() },
                            valueRange = 1f..31f,
                            steps = 30,
                            colors = SliderDefaults.colors(
                                thumbColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF0F9D58),
                                activeTrackColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF0F9D58),
                                inactiveTrackColor = if (darkTheme) Color(0x3F81C784) else Color(0x3F0F9D58)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val salary = salaryInput.toDoubleOrNull() ?: 0.0
                                if (salary > 0) {
                                    // 1. Complete onboarding
                                    viewModel.completeOnboarding(salary, creditDay)

                                    // 2. Open Google Calendar to schedule salary credits
                                    try {
                                        val intent = Intent(Intent.ACTION_INSERT).apply {
                                            data = CalendarContract.Events.CONTENT_URI
                                            putExtra(CalendarContract.Events.TITLE, "💰 Salary Credit [FullWallet]")
                                            putExtra(CalendarContract.Events.DESCRIPTION, "Monthly salary credited today! Monitor your budgets on Full Wallet.")
                                            
                                            val cal = Calendar.getInstance().apply {
                                                set(Calendar.DAY_OF_MONTH, creditDay.coerceIn(1, 28)) // coerce to avoid month overflow
                                            }
                                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 60 * 60 * 1000)
                                            // Make it recur monthly on this day
                                            putExtra("rrule", "FREQ=MONTHLY")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore calendar failure if device does not support it
                                    }
                                }
                            },
                            enabled = salaryInput.toDoubleOrNull() != null && (salaryInput.toDoubleOrNull() ?: 0.0) > 0.0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (darkTheme) Color(0xFF81C784) else Color(0xFF0F9D58),
                                disabledContainerColor = if (darkTheme) Color(0x22FFFFFF) else Color(0x11000000)
                            )
                        ) {
                            Text(
                                text = "🚀 Sync with Google Calendar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                color = if (darkTheme) Color(0xFF0A1810) else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
}

fun triggerBiometricPrompt(
    context: android.content.Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context as? FragmentActivity
    if (activity == null) {
        onSuccess()
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Authentication failed. Try again.")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("FullWallet Security Lock")
        .setSubtitle("Authenticate to access your financial workspace")
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    try {
        biometricPrompt.authenticate(promptInfo)
    } catch (e: Exception) {
        onError("Biometrics not configured: ${e.localizedMessage}")
    }
}

@Composable
fun BiometricLockScreen(
    onUnlockRequested: () -> Unit,
    errorMessage: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FullWallet Locked",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Biometric authentication or device PIN is required to access your account.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (!errorMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onUnlockRequested,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .testTag("unlock_app_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unlock App",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
