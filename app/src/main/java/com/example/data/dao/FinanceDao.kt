package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.entity.DailyExpenseEntity
import com.example.data.entity.MonthlyDataEntity
import com.example.data.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    @Query("SELECT * FROM monthly_data WHERE id = 1 LIMIT 1")
    fun getMonthlyData(): Flow<MonthlyDataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMonthlyData(monthlyData: MonthlyDataEntity): Long

    @Query("SELECT * FROM savings_goals ORDER BY createdAt ASC")
    fun getSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSavingsGoal(goal: SavingsGoalEntity): Long

    @Query("DELETE FROM savings_goals WHERE id = :id")
    fun deleteSavingsGoalById(id: String): Int

    @Query("SELECT * FROM daily_expenses ORDER BY date DESC, createdAt DESC")
    fun getDailyExpenses(): Flow<List<DailyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDailyExpense(expense: DailyExpenseEntity): Long

    @Query("DELETE FROM daily_expenses WHERE id = :id")
    fun deleteDailyExpenseById(id: String): Int

    @Query("DELETE FROM monthly_data")
    fun deleteMonthlyData(): Int

    @Query("DELETE FROM savings_goals")
    fun deleteSavingsGoals(): Int

    @Query("DELETE FROM daily_expenses")
    fun deleteDailyExpenses(): Int

    @Transaction
    fun clearAllData(): Boolean {
        deleteMonthlyData()
        deleteSavingsGoals()
        deleteDailyExpenses()
        return true
    }
}
