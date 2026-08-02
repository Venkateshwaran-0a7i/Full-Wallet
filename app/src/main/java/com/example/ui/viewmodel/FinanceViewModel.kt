package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.data.entity.DailyExpenseEntity
import com.example.data.entity.MonthlyDataEntity
import com.example.data.entity.SavingsGoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val sharedPrefs = application.getSharedPreferences("mwif_prefs", Context.MODE_PRIVATE)

    val monthlyData: StateFlow<MonthlyDataEntity>
    val savingsGoals: StateFlow<List<SavingsGoalEntity>>
    val dailyExpenses: StateFlow<List<DailyExpenseEntity>>

    private val _themePreference = MutableStateFlow("dark")
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    private val _accentColorPreference = MutableStateFlow("green")
    val accentColorPreference: StateFlow<String> = _accentColorPreference.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    private val _salaryDayOfMonth = MutableStateFlow(1)
    val salaryDayOfMonth: StateFlow<Int> = _salaryDayOfMonth.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _geminiModel = MutableStateFlow("gemini-3.5-flash")
    val geminiModel: StateFlow<String> = _geminiModel.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _isGeminiProcessing = MutableStateFlow(false)
    val isGeminiProcessing: StateFlow<Boolean> = _isGeminiProcessing.asStateFlow()

    private val _geminiFeedback = MutableStateFlow<String?>(null)
    val geminiFeedback: StateFlow<String?> = _geminiFeedback.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())

        _themePreference.value = sharedPrefs.getString("theme_preference", "dark") ?: "dark"
        _accentColorPreference.value = sharedPrefs.getString("accent_color_preference", "green") ?: "green"
        _biometricEnabled.value = sharedPrefs.getBoolean("biometric_enabled", false)
        _salaryDayOfMonth.value = sharedPrefs.getInt("salary_day_of_month", 1)
        _geminiApiKey.value = sharedPrefs.getString("gemini_api_key", "") ?: ""
        
        val savedModel = sharedPrefs.getString("gemini_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"
        val cleanModel = if (savedModel == "gemini-2.5-flash" || savedModel.contains("2.5") || savedModel.contains("1.5")) {
            "gemini-3.5-flash"
        } else {
            savedModel
        }
        _geminiModel.value = cleanModel
        if (cleanModel != savedModel) {
            sharedPrefs.edit().putString("gemini_model", cleanModel).apply()
        }
        
        _onboardingCompleted.value = sharedPrefs.getBoolean("onboarding_completed", false)

        monthlyData = repository.monthlyData
            .map { it ?: MonthlyDataEntity() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = MonthlyDataEntity()
            )

        savingsGoals = repository.savingsGoals
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        dailyExpenses = repository.dailyExpenses
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Reactive Auto-Save System
        viewModelScope.launch {
            combine(monthlyData, savingsGoals, dailyExpenses, salaryDayOfMonth) { data, goals, expenses, salaryDay ->
                // Bundle them together for auto-save
                buildJsonBackup(data, goals, expenses, salaryDay)
            }.collectLatest { jsonString ->
                autoSaveBackup(jsonString)
            }
        }
    }

    // --- Core Calculations ---

    fun calculateTotalExpenses(data: MonthlyDataEntity): Double {
        return (data.groceries ?: 0.0) +
                (data.medicine ?: 0.0) +
                (data.education ?: 0.0) +
                (data.rent ?: 0.0) +
                (data.food ?: 0.0) +
                (data.transport ?: 0.0) +
                (data.electricity ?: 0.0) +
                (data.internet ?: 0.0) +
                (data.mobile ?: 0.0) +
                (data.emi ?: 0.0) +
                (data.subscriptions ?: 0.0) +
                (data.shopping ?: 0.0) +
                (data.entertainment ?: 0.0) +
                (data.investments ?: 0.0) +
                (data.savings ?: 0.0) +
                (data.others ?: 0.0)
    }

    fun hasFinancialData(data: MonthlyDataEntity): Boolean {
        val income = (data.income ?: 0.0) + (data.bonus ?: 0.0)
        val totalExp = calculateTotalExpenses(data)
        return income > 0 || totalExp > 0
    }

    fun isDateInCurrentSalaryCycle(dateStr: String): Boolean {
        val salaryDay = salaryDayOfMonth.value
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try { sdf.parse(dateStr) } catch (e: Exception) { null } ?: return false
        
        val today = Calendar.getInstance()
        
        val cycleStart = Calendar.getInstance().apply {
            val maxDayThisMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
            val coercedSalaryDay = salaryDay.coerceIn(1, maxDayThisMonth)
            
            if (today.get(Calendar.DAY_OF_MONTH) >= coercedSalaryDay) {
                set(Calendar.DAY_OF_MONTH, coercedSalaryDay)
            } else {
                add(Calendar.MONTH, -1)
                val maxDayPrevMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, salaryDay.coerceIn(1, maxDayPrevMonth))
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val cycleEnd = Calendar.getInstance().apply {
            time = cycleStart.time
            add(Calendar.MONTH, 1)
        }
        
        val expenseCal = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return !expenseCal.before(cycleStart) && expenseCal.before(cycleEnd)
    }

    fun calculateBalance(data: MonthlyDataEntity, expenses: List<DailyExpenseEntity>): Double {
        val totalIncome = (data.income ?: 0.0) + (data.bonus ?: 0.0)
        val actualExpensesSum = expenses
            .filter { isDateInCurrentSalaryCycle(it.date) }
            .sumOf { it.amount }
        return totalIncome - actualExpensesSum
    }

    // --- DB Mutations ---

    fun updateMonthlyData(
        income: Double?, bonus: Double? = null, groceries: Double? = null, medicine: Double? = null, education: Double? = null,
        rent: Double?, food: Double?, transport: Double?,
        electricity: Double?, internet: Double?, mobile: Double?, emi: Double?,
        subscriptions: Double?, shopping: Double?, entertainment: Double?,
        investments: Double?, savings: Double?, others: Double?, goal: String
    ) {
        viewModelScope.launch {
            val entity = MonthlyDataEntity(
                id = 1,
                income = income, bonus = bonus, goal = goal, groceries = groceries, medicine = medicine, education = education,
                rent = rent, food = food,
                transport = transport, electricity = electricity, internet = internet,
                mobile = mobile, emi = emi, subscriptions = subscriptions,
                shopping = shopping, entertainment = entertainment,
                investments = investments, savings = savings, others = others
            )
            repository.saveMonthlyData(entity)
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double) {
        viewModelScope.launch {
            val goal = SavingsGoalEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                targetAmount = targetAmount,
                savedAmount = 0.0,
                createdAt = System.currentTimeMillis()
            )
            repository.saveSavingsGoal(goal)
        }
    }

    fun updateGoalSaved(id: String, savedAmount: Double) {
        viewModelScope.launch {
            val existing = savingsGoals.value.find { it.id == id }
            if (existing != null) {
                val updated = existing.copy(savedAmount = savedAmount)
                repository.saveSavingsGoal(updated)
            }
        }
    }

    fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
        }
    }

    fun addDailyExpense(date: String, category: String, amount: Double, description: String) {
        viewModelScope.launch {
            val expense = DailyExpenseEntity(
                id = UUID.randomUUID().toString(),
                date = date,
                category = category,
                amount = amount,
                description = description
            )
            repository.saveDailyExpense(expense)
        }
    }

    fun updateDailyExpense(id: String, date: String, category: String, amount: Double, description: String) {
        viewModelScope.launch {
            val existing = dailyExpenses.value.find { it.id == id }
            val expense = DailyExpenseEntity(
                id = id,
                date = date,
                category = category,
                amount = amount,
                description = description,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )
            repository.saveDailyExpense(expense)
        }
    }

    fun deleteDailyExpense(id: String) {
        viewModelScope.launch {
            repository.deleteDailyExpense(id)
        }
    }

    // --- Preferences ---

    fun setThemePreference(pref: String) {
        _themePreference.value = pref
        sharedPrefs.edit().putString("theme_preference", pref).apply()
    }

    fun setAccentColorPreference(accent: String) {
        _accentColorPreference.value = accent
        sharedPrefs.edit().putString("accent_color_preference", accent).apply()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        _biometricEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun setSalaryDayOfMonth(day: Int) {
        val safeDay = day.coerceIn(1, 31)
        _salaryDayOfMonth.value = safeDay
        sharedPrefs.edit().putInt("salary_day_of_month", safeDay).apply()
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        sharedPrefs.edit().putString("gemini_api_key", key).apply()
    }

    fun setGeminiModel(model: String) {
        val clean = if (model == "gemini-2.5-flash" || model.contains("2.5") || model.contains("1.5")) {
            "gemini-3.5-flash"
        } else {
            model
        }
        _geminiModel.value = clean
        sharedPrefs.edit().putString("gemini_model", clean).apply()
    }

    fun completeOnboarding(salary: Double, salaryDay: Int) {
        _onboardingCompleted.value = true
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        setSalaryDayOfMonth(salaryDay)
        viewModelScope.launch {
            val current = monthlyData.value
            val entity = current.copy(income = salary)
            repository.saveMonthlyData(entity)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    // --- JSON Backup / Import / Export Systems ---

    private fun buildJsonBackup(
        data: MonthlyDataEntity,
        goals: List<SavingsGoalEntity>,
        expenses: List<DailyExpenseEntity>,
        salaryDay: Int
    ): String {
        try {
            val root = JSONObject()

            // Monthly Data
            val mObj = JSONObject()
            mObj.put("income", data.income)
            mObj.put("bonus", data.bonus)
            mObj.put("goal", data.goal)
            mObj.put("groceries", data.groceries)
            mObj.put("medicine", data.medicine)
            mObj.put("education", data.education)
            mObj.put("rent", data.rent)
            mObj.put("food", data.food)
            mObj.put("transport", data.transport)
            mObj.put("electricity", data.electricity)
            mObj.put("internet", data.internet)
            mObj.put("mobile", data.mobile)
            mObj.put("emi", data.emi)
            mObj.put("subscriptions", data.subscriptions)
            mObj.put("shopping", data.shopping)
            mObj.put("entertainment", data.entertainment)
            mObj.put("investments", data.investments)
            mObj.put("savings", data.savings)
            mObj.put("others", data.others)
            root.put("monthlyData", mObj)

            // Savings Goals
            val goalsArr = JSONArray()
            goals.forEach { g ->
                val gObj = JSONObject()
                gObj.put("id", g.id)
                gObj.put("title", g.title)
                gObj.put("targetAmount", g.targetAmount)
                gObj.put("savedAmount", g.savedAmount)
                gObj.put("createdAt", g.createdAt)
                goalsArr.put(gObj)
            }
            root.put("savingsGoals", goalsArr)

            // Daily Expenses
            val expensesArr = JSONArray()
            expenses.forEach { e ->
                val eObj = JSONObject()
                eObj.put("id", e.id)
                eObj.put("date", e.date)
                eObj.put("category", e.category)
                eObj.put("amount", e.amount)
                eObj.put("description", e.description)
                eObj.put("createdAt", e.createdAt)
                expensesArr.put(eObj)
            }
            root.put("dailyExpenses", expensesArr)

            // Metadata / Config
            root.put("salaryDayOfMonth", salaryDay)
            root.put("exportTime", System.currentTimeMillis())

            return root.toString(2)
        } catch (e: Exception) {
            Log.e("FinanceViewModel", "Error building backup JSON", e)
            return ""
        }
    }

    private suspend fun autoSaveBackup(jsonString: String) = withContext(Dispatchers.IO) {
        if (jsonString.isEmpty()) return@withContext
        try {
            // Write to app internal files directory (Always succeeds)
            val internalFile = File(getApplication<Application>().filesDir, "walletreport_backup.json")
            internalFile.writeText(jsonString)

            // Write to app Documents directory (Always succeeds on Android 10+)
            val documentsDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (documentsDir != null) {
                val fullWalletDir = File(documentsDir, "FullWallet")
                if (!fullWalletDir.exists()) fullWalletDir.mkdirs()
                val externalFile = File(fullWalletDir, "walletreport.json")
                externalFile.writeText(jsonString)
            }

            // Write directly to standard storage Documents directory for user accessibility
            val publicDocumentsDir = File(Environment.getExternalStorageDirectory(), "Documents/FullWallet")
            try {
                if (!publicDocumentsDir.exists()) {
                    publicDocumentsDir.mkdirs()
                }
                val publicFile = File(publicDocumentsDir, "walletreport.json")
                publicFile.writeText(jsonString)
            } catch (ignored: Exception) {
                // Ignore permission/sandbox restrictions on some SDK levels
            }
        } catch (e: Exception) {
            Log.e("FinanceViewModel", "Error during reactive auto-save", e)
        }
    }

    fun exportBackupString(): String {
        return buildJsonBackup(
            monthlyData.value,
            savingsGoals.value,
            dailyExpenses.value,
            salaryDayOfMonth.value
        )
    }

    suspend fun importBackupString(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // Import Monthly Data
            if (root.has("monthlyData")) {
                val mObj = root.getJSONObject("monthlyData")
                val entity = MonthlyDataEntity(
                    id = 1,
                    income = if (mObj.isNull("income")) null else mObj.getDouble("income"),
                    bonus = if (mObj.isNull("bonus")) null else mObj.getDouble("bonus"),
                    goal = mObj.optString("goal", ""),
                    groceries = if (mObj.isNull("groceries")) null else mObj.getDouble("groceries"),
                    medicine = if (mObj.isNull("medicine")) null else mObj.getDouble("medicine"),
                    education = if (mObj.isNull("education")) null else mObj.getDouble("education"),
                    rent = if (mObj.isNull("rent")) null else mObj.getDouble("rent"),
                    food = if (mObj.isNull("food")) null else mObj.getDouble("food"),
                    transport = if (mObj.isNull("transport")) null else mObj.getDouble("transport"),
                    electricity = if (mObj.isNull("electricity")) null else mObj.getDouble("electricity"),
                    internet = if (mObj.isNull("internet")) null else mObj.getDouble("internet"),
                    mobile = if (mObj.isNull("mobile")) null else mObj.getDouble("mobile"),
                    emi = if (mObj.isNull("emi")) null else mObj.getDouble("emi"),
                    subscriptions = if (mObj.isNull("subscriptions")) null else mObj.getDouble("subscriptions"),
                    shopping = if (mObj.isNull("shopping")) null else mObj.getDouble("shopping"),
                    entertainment = if (mObj.isNull("entertainment")) null else mObj.getDouble("entertainment"),
                    investments = if (mObj.isNull("investments")) null else mObj.getDouble("investments"),
                    savings = if (mObj.isNull("savings")) null else mObj.getDouble("savings"),
                    others = if (mObj.isNull("others")) null else mObj.getDouble("others")
                )
                repository.saveMonthlyData(entity)
            }

            // Import Savings Goals
            if (root.has("savingsGoals")) {
                val goalsArr = root.getJSONArray("savingsGoals")
                for (i in 0 until goalsArr.length()) {
                    val gObj = goalsArr.getJSONObject(i)
                    val goal = SavingsGoalEntity(
                        id = gObj.optString("id", UUID.randomUUID().toString()),
                        title = gObj.optString("title", "Goal"),
                        targetAmount = gObj.optDouble("targetAmount", 0.0),
                        savedAmount = gObj.optDouble("savedAmount", 0.0),
                        createdAt = gObj.optLong("createdAt", System.currentTimeMillis())
                    )
                    repository.saveSavingsGoal(goal)
                }
            }

            // Import Daily Expenses
            if (root.has("dailyExpenses")) {
                val expensesArr = root.getJSONArray("dailyExpenses")
                for (i in 0 until expensesArr.length()) {
                    val eObj = expensesArr.getJSONObject(i)
                    val expense = DailyExpenseEntity(
                        id = eObj.optString("id", UUID.randomUUID().toString()),
                        date = eObj.optString("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())),
                        category = eObj.optString("category", "Others"),
                        amount = eObj.optDouble("amount", 0.0),
                        description = eObj.optString("description", ""),
                        createdAt = eObj.optLong("createdAt", System.currentTimeMillis())
                    )
                    repository.saveDailyExpense(expense)
                }
            }

            // Salary Day Preference
            if (root.has("salaryDayOfMonth")) {
                setSalaryDayOfMonth(root.getInt("salaryDayOfMonth"))
            }

            true
        } catch (e: Exception) {
            Log.e("FinanceViewModel", "Error importing backup string", e)
            false
        }
    }

    // --- Gemini Natural Language Assistant ---

    fun clearGeminiFeedback() {
        _geminiFeedback.value = null
    }

    fun askGemini(userInput: String) {
        if (userInput.isBlank()) return
        _isGeminiProcessing.value = true
        _geminiFeedback.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = getEffectiveApiKey()
            if (apiKey.isBlank()) {
                _geminiFeedback.value = "Error: Gemini API Key is missing. Please set your key in the Settings tab."
                _isGeminiProcessing.value = false
                return@launch
            }

            val currentDatabaseJson = exportBackupString()
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val systemPrompt = """
                You are the AI Assistant for "Full Wallet", a modern offline personal budget tracking app.
                You have direct READ and WRITE access to the complete database JSON representation of the application.
                
                Current Database State JSON:
                $currentDatabaseJson
                
                Today's date is: $todayDate.
                
                Your job is to analyze the user's natural language input, answer questions about their expenses, income, savings goals, remaining budget, and perform updates if they request them.
                
                When replying, you must return a raw JSON object ONLY.
                The JSON must follow one of these exact schemas:
                
                1. If you are just answering a question, or if there is no database update requested (READ ONLY):
                {
                  "action": "unknown",
                  "message": "Write your detailed, helpful reply here. You can calculate totals, analyze category expenditures, compare actual expenses with limits, and give expert advice based on the provided Current Database State JSON."
                }
                
                2. If the user wants to add a daily expense (WRITE ACCESS):
                {
                  "action": "add_expense",
                  "date": "yyyy-MM-dd",
                  "category": "Food" | "Groceries" | "Medicine" | "Education" | "Transport" | "Rent" | "Electricity" | "Internet" | "Mobile" | "EMI" | "Subscriptions" | "Shopping" | "Entertainment" | "Investments" | "Savings" | "Others",
                  "amount": 250.0,
                  "description": "Short description of the expense",
                  "message": "A friendly message confirming you logged the expense, and optionally giving context (e.g. 'Added ₹250 for food. You have ₹1250 remaining in your Food budget.')."
                }
                
                3. If the user wants to update monthly budget parameters, income, or savings goals (WRITE ACCESS):
                {
                  "action": "update_budget",
                  "income": 50000.0,
                  "rent": 12000.0,
                  "food": 8000.0,
                  "transport": 3000.0,
                  "electricity": 2000.0,
                  "internet": 1000.0,
                  "mobile": 500.0,
                  "emi": 4000.0,
                  "subscriptions": 600.0,
                  "shopping": 5000.0,
                  "entertainment": 3000.0,
                  "investments": 10000.0,
                  "savings": 5000.0,
                  "others": 2000.0,
                  "goal": "Text description of the monthly focus/goal",
                  "message": "A friendly confirmation of the budget updates."
                }
                
                4. If the user wants to add a new savings goal (WRITE ACCESS):
                {
                  "action": "add_goal",
                  "title": "New iPhone",
                  "targetAmount": 80000.0,
                  "message": "A friendly confirmation of the new goal."
                }
                
                5. If you want to make direct modifications to the database yourself (e.g., updating multiple items, changing salaryDayOfMonth, deleting/clearing items, or any general database-wide modification) (FULL READ & WRITE ACCESS):
                {
                  "action": "update_full_json",
                  "json": <the entire updated JSON database, following the exact structure of the provided Database State JSON, including modified values>,
                  "message": "A friendly explanation of what you updated in the database."
                }
                
                IMPORTANT:
                - Do not wrap the JSON in markdown blocks (like ```json ... ```). Output the clean JSON directly.
                - Base all answers on the provided Database State JSON. Perform actual math correctly.
            """.trimIndent()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            // Construct the REST body according to Option B Direct REST guidance
            val jsonBody = JSONObject()
            val contentsArr = JSONArray()
            val contentObj = JSONObject()
            val partsArr = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", userInput)
            partsArr.put(partObj)
            contentObj.put("parts", partsArr)
            contentsArr.put(contentObj)
            jsonBody.put("contents", contentsArr)

            // Add system instruction
            val systemInstructionObj = JSONObject()
            val systemPartsArr = JSONArray()
            val systemPartObj = JSONObject()
            systemPartObj.put("text", systemPrompt)
            systemPartsArr.put(systemPartObj)
            systemInstructionObj.put("parts", systemPartsArr)
            jsonBody.put("systemInstruction", systemInstructionObj)

            // Add responseFormat JSON
            val genConfigObj = JSONObject()
            genConfigObj.put("responseMimeType", "application/json")
            jsonBody.put("generationConfig", genConfigObj)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val modelName = geminiModel.value
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        val errMsg = try {
                            val errJson = JSONObject(errBody)
                            val errorObj = errJson.optJSONObject("error")
                            errorObj?.optString("message") ?: errBody
                        } catch (e: Exception) {
                            errBody.takeIf { it.isNotBlank() } ?: response.message
                        }
                        _geminiFeedback.value = "API Error: $errMsg"
                        _isGeminiProcessing.value = false
                        return@launch
                    }

                    val respBody = response.body?.string() ?: ""
                    val rootResp = JSONObject(respBody)
                    val candidates = rootResp.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        processGeminiResult(text.trim())
                    } else {
                        _geminiFeedback.value = "Error: Received empty response from Gemini."
                    }
                }
            } catch (e: Exception) {
                _geminiFeedback.value = "Network Error: ${e.localizedMessage}"
            } finally {
                _isGeminiProcessing.value = false
            }
        }
    }

    private suspend fun processGeminiResult(rawJson: String) = withContext(Dispatchers.Main) {
        try {
            val cleanJson = if (rawJson.startsWith("```json")) {
                rawJson.removePrefix("```json").removeSuffix("```").trim()
            } else if (rawJson.startsWith("```")) {
                rawJson.removePrefix("```").removeSuffix("```").trim()
            } else {
                rawJson
            }

            val obj = JSONObject(cleanJson)
            val action = obj.optString("action")
            val hasCustomMessage = obj.has("message") && obj.getString("message").isNotBlank()
            val customMessage = if (hasCustomMessage) obj.getString("message") else ""

            when (action) {
                "add_expense" -> {
                    val date = obj.optString("date")
                    val category = obj.optString("category", "Others")
                    val amount = obj.optDouble("amount", 0.0)
                    val description = obj.optString("description", "")

                    addDailyExpense(date, category, amount, description)
                    _geminiFeedback.value = if (hasCustomMessage) customMessage else "Success! Logged a daily expense of ₹${amount} under '$category' on $date."
                }
                "update_budget" -> {
                    // Start with current, apply updates
                    val current = monthlyData.value
                    val income = if (obj.has("income")) obj.getDouble("income") else current.income
                    val bonus = if (obj.has("bonus")) obj.getDouble("bonus") else current.bonus
                    val groceries = if (obj.has("groceries")) obj.getDouble("groceries") else current.groceries
                    val medicine = if (obj.has("medicine")) obj.getDouble("medicine") else current.medicine
                    val education = if (obj.has("education")) obj.getDouble("education") else current.education
                    val rent = if (obj.has("rent")) obj.getDouble("rent") else current.rent
                    val food = if (obj.has("food")) obj.getDouble("food") else current.food
                    val transport = if (obj.has("transport")) obj.getDouble("transport") else current.transport
                    val electricity = if (obj.has("electricity")) obj.getDouble("electricity") else current.electricity
                    val internet = if (obj.has("internet")) obj.getDouble("internet") else current.internet
                    val mobile = if (obj.has("mobile")) obj.getDouble("mobile") else current.mobile
                    val emi = if (obj.has("emi")) obj.getDouble("emi") else current.emi
                    val subscriptions = if (obj.has("subscriptions")) obj.getDouble("subscriptions") else current.subscriptions
                    val shopping = if (obj.has("shopping")) obj.getDouble("shopping") else current.shopping
                    val entertainment = if (obj.has("entertainment")) obj.getDouble("entertainment") else current.entertainment
                    val investments = if (obj.has("investments")) obj.getDouble("investments") else current.investments
                    val savings = if (obj.has("savings")) obj.getDouble("savings") else current.savings
                    val others = if (obj.has("others")) obj.getDouble("others") else current.others
                    val goal = if (obj.has("goal")) obj.getString("goal") else current.goal

                    updateMonthlyData(
                        income = income,
                        bonus = bonus,
                        groceries = groceries,
                        medicine = medicine,
                        education = education,
                        rent = rent,
                        food = food,
                        transport = transport,
                        electricity = electricity,
                        internet = internet,
                        mobile = mobile,
                        emi = emi,
                        subscriptions = subscriptions,
                        shopping = shopping,
                        entertainment = entertainment,
                        investments = investments,
                        savings = savings,
                        others = others,
                        goal = goal
                    )
                    _geminiFeedback.value = if (hasCustomMessage) customMessage else "Success! Updated your monthly budget limits."
                }
                "add_goal" -> {
                    val title = obj.optString("title", "Goal")
                    val targetAmount = obj.optDouble("targetAmount", 0.0)

                    addSavingsGoal(title, targetAmount)
                    _geminiFeedback.value = if (hasCustomMessage) customMessage else "Success! Added a new savings goal: '$title' of ₹${targetAmount}."
                }
                "update_full_json" -> {
                    val fullJsonObj = obj.optJSONObject("json")
                    val fullJsonStr = if (fullJsonObj != null) {
                        fullJsonObj.toString()
                    } else {
                        obj.optString("json")
                    }

                    if (!fullJsonStr.isNullOrEmpty()) {
                        val success = importBackupString(fullJsonStr)
                        if (success) {
                            _geminiFeedback.value = if (hasCustomMessage) customMessage else "Success! Full database updated by Gemini Assistant."
                        } else {
                            _geminiFeedback.value = "Error: Failed to apply database updates."
                        }
                    } else {
                        _geminiFeedback.value = "Error: Full database update JSON payload is empty."
                    }
                }
                else -> {
                    _geminiFeedback.value = if (hasCustomMessage) customMessage else "I understood you, but no updates were made."
                }
            }
        } catch (e: Exception) {
            _geminiFeedback.value = "Parsed message incorrectly: ${e.localizedMessage}. Please try specifying clearly like: 'Spent 200 on groceries today'."
        }
    }

    private fun getEffectiveApiKey(): String {
        if (_geminiApiKey.value.isNotEmpty()) return _geminiApiKey.value
        // Check for common fallback environment variable name
        return System.getenv("GEMINI_API_KEY") ?: ""
    }
}
