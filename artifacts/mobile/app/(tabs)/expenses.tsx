import React, { useEffect, useState } from 'react';
import {
  Alert,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  useColorScheme,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useColors } from '@/hooks/useColors';
import { useFinance, type ExpenseKey } from '@/context/FinanceContext';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';

type CatItem = {
  key: ExpenseKey;
  label: string;
  lightBg: string;
  darkBg: string;
  color: string;
};

const EXPENSE_CATEGORIES: CatItem[] = [
  { key: 'rent', label: 'Rent', lightBg: '#DBEAFE', darkBg: '#1E3A5F', color: '#2563EB' },
  { key: 'food', label: 'Food', lightBg: '#FEF3C7', darkBg: '#451A03', color: '#D97706' },
  { key: 'transport', label: 'Transport', lightBg: '#D1FAE5', darkBg: '#052E16', color: '#059669' },
  { key: 'electricity', label: 'Electricity', lightBg: '#FEF9C3', darkBg: '#422006', color: '#CA8A04' },
  { key: 'internet', label: 'Internet', lightBg: '#EDE9FE', darkBg: '#2E1065', color: '#7C3AED' },
  { key: 'mobile', label: 'Mobile', lightBg: '#FCE7F3', darkBg: '#500724', color: '#DB2777' },
  { key: 'emi', label: 'EMI', lightBg: '#FEE2E2', darkBg: '#450A0A', color: '#DC2626' },
  { key: 'subscriptions', label: 'Subscriptions', lightBg: '#CCFBF1', darkBg: '#042F2E', color: '#0D9488' },
  { key: 'shopping', label: 'Shopping', lightBg: '#E0E7FF', darkBg: '#1E1B4B', color: '#4F46E5' },
  { key: 'entertainment', label: 'Entertainment', lightBg: '#FFE4E6', darkBg: '#4C0519', color: '#E11D48' },
  { key: 'investments', label: 'Investments', lightBg: '#ECFDF5', darkBg: '#022C22', color: '#059669' },
  { key: 'savings', label: 'Savings', lightBg: '#EFF6FF', darkBg: '#172554', color: '#2563EB' },
  { key: 'others', label: 'Others', lightBg: '#F3F4F6', darkBg: '#1F2937', color: '#6B7280' },
];

function numToStr(val: number | null | undefined): string {
  if (val === null || val === undefined) return '';
  return val.toString();
}

function strToNum(s: string): number | null {
  const trimmed = s.trim();
  if (!trimmed) return null;
  const n = parseFloat(trimmed);
  return isNaN(n) ? null : Math.max(0, n);
}

interface ExpenseCardProps {
  cat: CatItem;
  value: string;
  onChange: (v: string) => void;
}

function ExpenseCard({ cat, value, onChange }: ExpenseCardProps) {
  const colors = useColors();
  const scheme = useColorScheme();
  const isDark = scheme === 'dark';
  const bg = isDark ? cat.darkBg : cat.lightBg;

  return (
    <View style={[styles.bentoCard, { backgroundColor: bg }]}>
      <Text style={[styles.bentoLabel, { color: cat.color, fontFamily: 'Inter_600SemiBold' }]}>
        {cat.label}
      </Text>
      <View style={styles.bentoInputRow}>
        <Text style={[styles.rupee, { color: isDark ? 'rgba(255,255,255,0.5)' : 'rgba(0,0,0,0.35)', fontFamily: 'Inter_500Medium' }]}>
          ₹
        </Text>
        <TextInput
          style={[styles.bentoInput, { color: isDark ? '#FFFFFF' : '#0A1F14', fontFamily: 'Inter_600SemiBold' }]}
          value={value}
          onChangeText={onChange}
          keyboardType="numeric"
          placeholder="0"
          placeholderTextColor={isDark ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.25)'}
          returnKeyType="next"
        />
      </View>
    </View>
  );
}

export default function ExpensesScreen() {
  const insets = useSafeAreaInsets();
  const colors = useColors();
  const scheme = useColorScheme();
  const isDark = scheme === 'dark';
  const { monthlyData, updateMonthlyData } = useFinance();

  // Local form state
  const [income, setIncome] = useState<string>(numToStr(monthlyData.income));
  const [expFields, setExpFields] = useState<Partial<Record<ExpenseKey, string>>>({});
  const [goal, setGoal] = useState<string>(monthlyData.goal);
  const [saved, setSaved] = useState(false);

  // Sync from context on mount
  useEffect(() => {
    setIncome(numToStr(monthlyData.income));
    const fields: Partial<Record<ExpenseKey, string>> = {};
    for (const [k, v] of Object.entries(monthlyData.expenses)) {
      fields[k as ExpenseKey] = numToStr(v as number | null);
    }
    setExpFields(fields);
    setGoal(monthlyData.goal);
  }, []); // run once on mount

  const handleExpChange = (key: ExpenseKey, val: string) => {
    setExpFields((prev) => ({ ...prev, [key]: val }));
    setSaved(false);
  };

  const handleSave = async () => {
    const parsedExpenses: Partial<Record<ExpenseKey, number | null>> = {};
    for (const cat of EXPENSE_CATEGORIES) {
      parsedExpenses[cat.key] = strToNum(expFields[cat.key] ?? '');
    }
    await updateMonthlyData({
      income: strToNum(income),
      expenses: parsedExpenses,
      goal: goal.trim(),
    });
    await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const isWeb = Platform.OS === 'web';
  const paddingTop = isWeb ? insets.top + 67 : insets.top + 20;
  const paddingBottom = isWeb ? 34 + 100 : insets.bottom + 100;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <KeyboardAwareScrollViewCompat
        contentContainerStyle={{ paddingTop, paddingBottom, paddingHorizontal: 20 }}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
        bottomOffset={20}
      >
        {/* Header */}
        <View style={styles.pageHeader}>
          <Text style={[styles.pageTitle, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
            Monthly Expenses
          </Text>
          <Text style={[styles.pageSub, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
            Enter your income and expenses for this month
          </Text>
        </View>

        {/* Income Card - Full Width Featured */}
        <View style={[styles.incomeCard, { backgroundColor: isDark ? '#14532D' : '#DCFCE7' }]}>
          <Text style={[styles.incomeLabel, { color: '#15803D', fontFamily: 'Inter_700Bold' }]}>
            Monthly Income
          </Text>
          <Text style={[styles.incomeHint, { color: isDark ? '#4ADE80' : '#166534', fontFamily: 'Inter_400Regular' }]}>
            Your total monthly earnings
          </Text>
          <View style={styles.incomeInputRow}>
            <Text style={[styles.incomeRupee, { color: '#15803D', fontFamily: 'Inter_700Bold' }]}>₹</Text>
            <TextInput
              style={[styles.incomeInput, { color: isDark ? '#FFFFFF' : '#14532D', fontFamily: 'Inter_700Bold' }]}
              value={income}
              onChangeText={(v) => { setIncome(v); setSaved(false); }}
              keyboardType="numeric"
              placeholder="0"
              placeholderTextColor={isDark ? 'rgba(255,255,255,0.3)' : 'rgba(20,83,45,0.35)'}
              returnKeyType="next"
            />
          </View>
        </View>

        {/* Expense Categories Bento Grid */}
        <Text style={[styles.gridTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
          Expense Categories
        </Text>
        <View style={styles.bentoGrid}>
          {EXPENSE_CATEGORIES.map((cat) => (
            <View key={cat.key} style={styles.bentoWrap}>
              <ExpenseCard
                cat={cat}
                value={expFields[cat.key] ?? ''}
                onChange={(v) => handleExpChange(cat.key, v)}
              />
            </View>
          ))}
        </View>

        {/* Goal Section */}
        <Text style={[styles.gridTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
          Financial Goal
        </Text>
        <View style={[styles.goalCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <TextInput
            style={[
              styles.goalInput,
              { color: colors.foreground, fontFamily: 'Inter_400Regular' },
            ]}
            value={goal}
            onChangeText={(v) => { setGoal(v); setSaved(false); }}
            multiline
            numberOfLines={4}
            placeholder="e.g. Buy a Laptop, Emergency Fund, Vacation, New Bike..."
            placeholderTextColor={colors.mutedForeground}
            textAlignVertical="top"
          />
        </View>

        {/* Save Button */}
        <TouchableOpacity
          style={[styles.saveBtn, { backgroundColor: saved ? '#22C55E' : colors.primary }]}
          onPress={handleSave}
          activeOpacity={0.85}
        >
          <Feather name={saved ? 'check' : 'save'} size={18} color={colors.primaryForeground} />
          <Text style={[styles.saveBtnText, { color: colors.primaryForeground, fontFamily: 'Inter_600SemiBold' }]}>
            {saved ? 'Saved!' : 'Save Expenses'}
          </Text>
        </TouchableOpacity>

        <Text style={[styles.note, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
          All fields are optional. Leave empty to skip.
        </Text>
      </KeyboardAwareScrollViewCompat>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  pageHeader: { marginBottom: 24 },
  pageTitle: { fontSize: 26, marginBottom: 4 },
  pageSub: { fontSize: 14 },
  incomeCard: {
    borderRadius: 20,
    padding: 22,
    marginBottom: 24,
  },
  incomeLabel: { fontSize: 16, marginBottom: 2 },
  incomeHint: { fontSize: 12, marginBottom: 12 },
  incomeInputRow: { flexDirection: 'row', alignItems: 'center' },
  incomeRupee: { fontSize: 32, marginRight: 4 },
  incomeInput: { fontSize: 40, flex: 1 },
  gridTitle: { fontSize: 15, marginBottom: 12 },
  bentoGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginBottom: 24 },
  bentoWrap: { width: '47.5%' },
  bentoCard: { borderRadius: 16, padding: 14, minHeight: 88, justifyContent: 'space-between' },
  bentoLabel: { fontSize: 12, marginBottom: 10 },
  bentoInputRow: { flexDirection: 'row', alignItems: 'center' },
  rupee: { fontSize: 16, marginRight: 2 },
  bentoInput: { fontSize: 20, flex: 1 },
  goalCard: {
    borderRadius: 16,
    borderWidth: 1,
    padding: 16,
    marginBottom: 20,
    minHeight: 110,
  },
  goalInput: { fontSize: 15, lineHeight: 24, flex: 1, minHeight: 80 },
  saveBtn: {
    borderRadius: 16,
    paddingVertical: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    marginBottom: 12,
  },
  saveBtnText: { fontSize: 16 },
  note: { fontSize: 12, textAlign: 'center' },
});
