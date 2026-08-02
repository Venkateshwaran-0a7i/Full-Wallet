package com.example.data

import com.example.data.dao.FinanceDao
import com.example.data.entity.DailyExpenseEntity
import com.example.data.entity.MonthlyDataEntity
import com.example.data.entity.SavingsGoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinanceRepository(private val financeDao: FinanceDao) {
    val monthlyData: Flow<MonthlyDataEntity?> = financeDao.getMonthlyData()
    val savingsGoals: Flow<List<SavingsGoalEntity>> = financeDao.getSavingsGoals()
    val dailyExpenses: Flow<List<DailyExpenseEntity>> = financeDao.getDailyExpenses()

    suspend fun saveMonthlyData(data: MonthlyDataEntity) = withContext(Dispatchers.IO) {
        financeDao.insertMonthlyData(data)
    }

    suspend fun saveSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        financeDao.insertSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(id: String) = withContext(Dispatchers.IO) {
        financeDao.deleteSavingsGoalById(id)
    }

    suspend fun saveDailyExpense(expense: DailyExpenseEntity) = withContext(Dispatchers.IO) {
        financeDao.insertDailyExpense(expense)
    }

    suspend fun deleteDailyExpense(id: String) = withContext(Dispatchers.IO) {
        financeDao.deleteDailyExpenseById(id)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        financeDao.clearAllData()
    }
}
