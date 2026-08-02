import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { Appearance } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

export type ExpenseKey =
  | 'rent'
  | 'food'
  | 'transport'
  | 'electricity'
  | 'internet'
  | 'mobile'
  | 'emi'
  | 'subscriptions'
  | 'shopping'
  | 'entertainment'
  | 'investments'
  | 'savings'
  | 'others';

export interface MonthlyData {
  income: number | null;
  expenses: Partial<Record<ExpenseKey, number | null>>;
  goal: string;
}

export interface SavingsGoal {
  id: string;
  title: string;
  targetAmount: number;
  savedAmount: number;
  createdAt: string;
}

interface FinanceContextType {
  monthlyData: MonthlyData;
  goals: SavingsGoal[];
  themePreference: 'system' | 'light' | 'dark';
  totalExpenses: number;
  balance: number;
  isLoaded: boolean;
  updateMonthlyData: (data: Partial<MonthlyData>) => Promise<void>;
  addGoal: (title: string, targetAmount: number) => Promise<void>;
  updateGoalSaved: (id: string, savedAmount: number) => Promise<void>;
  deleteGoal: (id: string) => Promise<void>;
  setThemePreference: (pref: 'system' | 'light' | 'dark') => Promise<void>;
  clearAllData: () => Promise<void>;
}

const STORAGE_KEYS = {
  MONTHLY_DATA: '@mwif/monthly_data',
  GOALS: '@mwif/goals',
  THEME: '@mwif/theme',
};

const defaultMonthlyData: MonthlyData = {
  income: null,
  expenses: {},
  goal: '',
};

const FinanceContext = createContext<FinanceContextType | null>(null);

export function FinanceProvider({ children }: { children: React.ReactNode }) {
  const [monthlyData, setMonthlyData] = useState<MonthlyData>(defaultMonthlyData);
  const [goals, setGoals] = useState<SavingsGoal[]>([]);
  const [themePreference, setThemePreferenceState] = useState<'system' | 'light' | 'dark'>('system');
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    async function load() {
      try {
        const [dataStr, goalsStr, themeStr] = await Promise.all([
          AsyncStorage.getItem(STORAGE_KEYS.MONTHLY_DATA),
          AsyncStorage.getItem(STORAGE_KEYS.GOALS),
          AsyncStorage.getItem(STORAGE_KEYS.THEME),
        ]);
        if (dataStr) setMonthlyData(JSON.parse(dataStr) as MonthlyData);
        if (goalsStr) setGoals(JSON.parse(goalsStr) as SavingsGoal[]);
        if (themeStr) {
          const pref = themeStr as 'system' | 'light' | 'dark';
          setThemePreferenceState(pref);
          Appearance.setColorScheme(pref === 'system' ? null : pref);
        }
      } catch {
        // silently ignore storage errors
      } finally {
        setIsLoaded(true);
      }
    }
    load();
  }, []);

  const totalExpenses = Object.values(monthlyData.expenses).reduce(
    (sum, val) => sum + (val ?? 0),
    0,
  );
  const balance = (monthlyData.income ?? 0) - totalExpenses;

  const updateMonthlyData = useCallback(
    async (data: Partial<MonthlyData>) => {
      const updated = { ...monthlyData, ...data };
      setMonthlyData(updated);
      await AsyncStorage.setItem(STORAGE_KEYS.MONTHLY_DATA, JSON.stringify(updated));
    },
    [monthlyData],
  );

  const addGoal = useCallback(
    async (title: string, targetAmount: number) => {
      const newGoal: SavingsGoal = {
        id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
        title,
        targetAmount,
        savedAmount: 0,
        createdAt: new Date().toISOString(),
      };
      const updated = [...goals, newGoal];
      setGoals(updated);
      await AsyncStorage.setItem(STORAGE_KEYS.GOALS, JSON.stringify(updated));
    },
    [goals],
  );

  const updateGoalSaved = useCallback(
    async (id: string, savedAmount: number) => {
      const updated = goals.map((g) => (g.id === id ? { ...g, savedAmount } : g));
      setGoals(updated);
      await AsyncStorage.setItem(STORAGE_KEYS.GOALS, JSON.stringify(updated));
    },
    [goals],
  );

  const deleteGoal = useCallback(
    async (id: string) => {
      const updated = goals.filter((g) => g.id !== id);
      setGoals(updated);
      await AsyncStorage.setItem(STORAGE_KEYS.GOALS, JSON.stringify(updated));
    },
    [goals],
  );

  const setThemePreference = useCallback(async (pref: 'system' | 'light' | 'dark') => {
    setThemePreferenceState(pref);
    Appearance.setColorScheme(pref === 'system' ? null : pref);
    await AsyncStorage.setItem(STORAGE_KEYS.THEME, pref);
  }, []);

  const clearAllData = useCallback(async () => {
    setMonthlyData(defaultMonthlyData);
    setGoals([]);
    await Promise.all([
      AsyncStorage.removeItem(STORAGE_KEYS.MONTHLY_DATA),
      AsyncStorage.removeItem(STORAGE_KEYS.GOALS),
    ]);
  }, []);

  return (
    <FinanceContext.Provider
      value={{
        monthlyData,
        goals,
        themePreference,
        totalExpenses,
        balance,
        isLoaded,
        updateMonthlyData,
        addGoal,
        updateGoalSaved,
        deleteGoal,
        setThemePreference,
        clearAllData,
      }}
    >
      {children}
    </FinanceContext.Provider>
  );
}

export function useFinance(): FinanceContextType {
  const ctx = useContext(FinanceContext);
  if (!ctx) throw new Error('useFinance must be used inside FinanceProvider');
  return ctx;
}
