package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.theme.GlassTheme
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val salaryDayOfMonth by viewModel.salaryDayOfMonth.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val themePreference by viewModel.themePreference.collectAsState()
    val accentColorPreference by viewModel.accentColorPreference.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showClearConfirm by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(geminiApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    var importDialogVisible by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }

    var exportDialogVisible by remember { mutableStateOf(false) }
    var exportText by remember { mutableStateOf("") }

    // Sync input with state changes
    LaunchedEffect(geminiApiKey) {
        apiKeyInput = geminiApiKey
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // Title Header
            Column {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure your layout, preferences and database backups",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Appearance & Theme Section Card
            SettingsSectionCard(
                title = "Appearance & Theme",
                icon = Icons.Outlined.Palette,
                iconColor = MaterialTheme.colorScheme.primary,
                iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Customize your app theme mode and primary accent color.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    // Theme Mode Selector
                    Text(
                        text = "Theme Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(
                            Triple("dark", "Dark", Icons.Default.DarkMode),
                            Triple("light", "Light", Icons.Default.LightMode),
                            Triple("system", "System", Icons.Default.SettingsSuggest)
                        )

                        themes.forEach { (mode, label, icon) ->
                            val isSelected = themePreference.equals(mode, ignoreCase = true)
                            Surface(
                                onClick = { viewModel.setThemePreference(mode) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_option_$mode")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Color Choice Selector
                    Text(
                        text = "Accent Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val colorOptions = listOf(
                        "Green" to Color(0xFF0F9D58),
                        "Emerald" to Color(0xFF059669),
                        "Blue" to Color(0xFF2563EB),
                        "Purple" to Color(0xFF7C3AED),
                        "Amber" to Color(0xFFD97706),
                        "Rose" to Color(0xFFE11D48),
                        "Teal" to Color(0xFF0D9488),
                        "Coral" to Color(0xFFEA580C)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorOptions.forEach { (name, color) ->
                            val isSelected = accentColorPreference.equals(name, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setAccentColorPreference(name.lowercase()) }
                                    .testTag("color_option_${name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected $name",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Security & Privacy Card (Biometrics)
            SettingsSectionCard(
                title = "Security & Privacy",
                icon = Icons.Outlined.Fingerprint,
                iconColor = MaterialTheme.colorScheme.primary,
                iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Biometric App Lock",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Require fingerprint, face unlock, or device PIN to open the app.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { viewModel.setBiometricEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("biometric_switch")
                    )
                }
            }
            SettingsSectionCard(
                title = "Salary Calendar Day",
                icon = Icons.Outlined.CalendarMonth,
                iconColor = Color(0xFFD97706),
                iconBg = Color(0xFFFEF3C7)
            ) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose the day of the month when your salary is credited. This day will be highlighted with a distinctive indicator on your interactive finance calendar.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Slider(
                            value = salaryDayOfMonth.toFloat(),
                            onValueChange = { viewModel.setSalaryDayOfMonth(it.toInt()) },
                            valueRange = 1f..31f,
                            steps = 30,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD97706).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Day $salaryDayOfMonth",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }

            // Gemini Config Card
            SettingsSectionCard(
                title = "Gemini AI Assistant",
                icon = Icons.Outlined.AutoAwesome,
                iconColor = Color(0xFF7C3AED),
                iconBg = Color(0xFFF3E8FF),
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enable natural language budget updates and insights with the knowledge of your full offline JSON file. Enter your personal Gemini API Key below.",
                        fontSize = 13.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.DarkGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Key Visibility",
                                    tint = Color.Black
                                )
                            }
                        }
                    )

                    Button(
                        onClick = {
                            viewModel.setGeminiApiKey(apiKeyInput.trim())
                            scope.launch {
                                snackbarHostState.showSnackbar("Gemini API Key saved successfully.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save API Key", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Data Management / JSON Backup Card
            SettingsSectionCard(
                title = "Data Backups & Sync",
                icon = Icons.Outlined.Backup,
                iconColor = Color(0xFF2563EB),
                iconBg = Color(0xFFDBEAFE)
            ) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Export your financial database, category budgets, savings goals, and daily transaction logs as a single JSON file. You can import this JSON file on other devices or restore the database anytime.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val json = viewModel.exportBackupString()
                                exportText = json
                                exportDialogVisible = true

                                // Also write to clipboard
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("FullWallet_Backup", json)
                                clipboard.setPrimaryClip(clip)

                                scope.launch {
                                    snackbarHostState.showSnackbar("Backup copied to clipboard & auto-saved to Documents folder!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                importText = ""
                                importError = null
                                importDialogVisible = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Security & Privacy Info Card
            SettingsSectionCard(
                title = "Security & Privacy",
                icon = Icons.Outlined.Shield,
                iconColor = Color(0xFF059669),
                iconBg = Color(0xFFD1FAE5)
            ) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Your data stays on your device",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "All financial information (income, expenses, category budgets, savings goals) is stored locally using an encrypted Room database. No information is ever synced, shared, or sent to any server.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            // Data Reset Card
            SettingsSectionCard(
                title = "Danger Zone",
                icon = Icons.Outlined.DeleteForever,
                iconColor = MaterialTheme.colorScheme.error,
                iconBg = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Resetting your database is irreversible. This clears all income, expenses, financial goal text, daily transactions, and savings goals immediately.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Button(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_data_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset All Data", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // About App Card
            SettingsSectionCard(
                title = "About App",
                icon = Icons.Outlined.Info,
                iconColor = MaterialTheme.colorScheme.secondary,
                iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Full Wallet",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version 2.0.0 (v2 Upgrade)\nBuilt with Jetpack Compose & SQLite Room\nLocal files auto-saved in: documents/FullWallet/walletreport.json\nDesigned for ultimate privacy and speed.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )

        // Reset Confirm Dialog
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Confirm Database Reset", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete all financial logs, calendar expenses, and savings goals? This action cannot be reversed.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllData()
                            showClearConfirm = false
                            scope.launch {
                                snackbarHostState.showSnackbar("All financial data cleared successfully.")
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Export Dialog
        if (exportDialogVisible) {
            AlertDialog(
                onDismissRequest = { exportDialogVisible = false },
                title = { Text("Exported JSON Data", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("The JSON report has been copied to your clipboard and auto-saved to FullWallet/walletreport.json inside your Documents folder. You can also manually copy it below:")
                        OutlinedTextField(
                            value = exportText,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            readOnly = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { exportDialogVisible = false }) {
                        Text("Close")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Import Dialog
        if (importDialogVisible) {
            AlertDialog(
                onDismissRequest = { importDialogVisible = false },
                title = { Text("Import JSON Data", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Paste your Full Wallet JSON backup text below to restore your monthly data, category budgets, savings goals, salary dates and transaction history:")
                        OutlinedTextField(
                            value = importText,
                            onValueChange = {
                                importText = it
                                importError = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            placeholder = { Text("Paste JSON string here...") },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                            isError = importError != null
                        )
                        if (importError != null) {
                            Text(
                                text = importError!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importText.isBlank()) {
                                importError = "Please enter some JSON content."
                                return@Button
                            }
                            scope.launch {
                                val success = viewModel.importBackupString(importText.trim())
                                if (success) {
                                    importDialogVisible = false
                                    snackbarHostState.showSnackbar("Financial data successfully imported!")
                                } else {
                                    importError = "Invalid JSON format or structure. Please verify."
                                }
                            }
                        }
                    ) {
                        Text("Restore Data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { importDialogVisible = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = if (containerColor != null) {
            CardDefaults.cardColors(containerColor = containerColor)
        } else {
            GlassTheme.cardColors()
        },
        shape = GlassTheme.cornerRadius,
        border = GlassTheme.borderStroke()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor ?: MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}
