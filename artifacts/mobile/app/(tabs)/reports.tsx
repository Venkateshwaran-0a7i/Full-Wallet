import React from 'react';
import {
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useColors } from '@/hooks/useColors';
import { useFinance, type ExpenseKey } from '@/context/FinanceContext';
import { Feather } from '@expo/vector-icons';

type CatMeta = { key: ExpenseKey; label: string; color: string };

const CATEGORIES: CatMeta[] = [
  { key: 'rent', label: 'Rent', color: '#2563EB' },
  { key: 'food', label: 'Food', color: '#D97706' },
  { key: 'transport', label: 'Transport', color: '#059669' },
  { key: 'electricity', label: 'Electricity', color: '#CA8A04' },
  { key: 'internet', label: 'Internet', color: '#7C3AED' },
  { key: 'mobile', label: 'Mobile', color: '#DB2777' },
  { key: 'emi', label: 'EMI', color: '#DC2626' },
  { key: 'subscriptions', label: 'Subscriptions', color: '#0D9488' },
  { key: 'shopping', label: 'Shopping', color: '#4F46E5' },
  { key: 'entertainment', label: 'Entertainment', color: '#E11D48' },
  { key: 'investments', label: 'Investments', color: '#059669' },
  { key: 'savings', label: 'Savings', color: '#2563EB' },
  { key: 'others', label: 'Others', color: '#6B7280' },
];

function StatRow({ label, value, color }: { label: string; value: string; color: string }) {
  const colors = useColors();
  return (
    <View style={styles.statRow}>
      <View style={[styles.statDot, { backgroundColor: color }]} />
      <Text style={[styles.statLabel, { color: colors.foreground, fontFamily: 'Inter_400Regular' }]}>
        {label}
      </Text>
      <Text style={[styles.statValue, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
        {value}
      </Text>
    </View>
  );
}

export default function ReportsScreen() {
  const insets = useSafeAreaInsets();
  const colors = useColors();
  const { monthlyData, totalExpenses } = useFinance();

  const income = monthlyData.income ?? 0;
  const hasData = income > 0 || totalExpenses > 0;

  const allExpenses = CATEGORIES.map((cat) => ({
    ...cat,
    value: monthlyData.expenses[cat.key] ?? 0,
  })).filter((e) => e.value > 0);

  const maxVal = allExpenses.length > 0 ? Math.max(...allExpenses.map((e) => e.value)) : 1;

  const netSavings = income - totalExpenses;
  const savingsRate = income > 0 ? (netSavings / income) * 100 : 0;
  const expenseRate = income > 0 ? (totalExpenses / income) * 100 : 0;

  const isWeb = Platform.OS === 'web';
  const paddingTop = isWeb ? insets.top + 67 : insets.top + 20;
  const paddingBottom = isWeb ? 34 + 90 : insets.bottom + 90;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView
        contentContainerStyle={{ paddingTop, paddingBottom, paddingHorizontal: 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <Text style={[styles.pageTitle, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
            Reports
          </Text>
          <Text style={[styles.pageSub, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
            Your monthly financial breakdown
          </Text>
        </View>

        {!hasData ? (
          /* Empty State */
          <View style={styles.empty}>
            <View style={[styles.emptyIconWrap, { backgroundColor: colors.secondary }]}>
              <Feather name="bar-chart-2" size={40} color={colors.mutedForeground} />
            </View>
            <Text style={[styles.emptyTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
              No data to display
            </Text>
            <Text style={[styles.emptySub, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
              Add your income and expenses in the Expenses tab to see your financial reports here.
            </Text>
          </View>
        ) : (
          <>
            {/* Overview Cards */}
            <View style={styles.overviewRow}>
              <View style={[styles.overviewCard, { backgroundColor: '#DCFCE7', flex: 1 }]}>
                <Text style={[styles.overviewLabel, { color: '#166534', fontFamily: 'Inter_500Medium' }]}>Income</Text>
                <Text style={[styles.overviewValue, { color: '#15803D', fontFamily: 'Inter_700Bold' }]}>
                  ₹{income.toLocaleString('en-IN')}
                </Text>
              </View>
              <View style={[styles.overviewCard, { backgroundColor: '#FEE2E2', flex: 1 }]}>
                <Text style={[styles.overviewLabel, { color: '#991B1B', fontFamily: 'Inter_500Medium' }]}>Expenses</Text>
                <Text style={[styles.overviewValue, { color: '#DC2626', fontFamily: 'Inter_700Bold' }]}>
                  ₹{totalExpenses.toLocaleString('en-IN')}
                </Text>
              </View>
            </View>

            <View style={styles.overviewRow}>
              <View
                style={[
                  styles.overviewCard,
                  { backgroundColor: netSavings >= 0 ? '#DBEAFE' : '#FEE2E2', flex: 1 },
                ]}
              >
                <Text
                  style={[
                    styles.overviewLabel,
                    { color: netSavings >= 0 ? '#1E40AF' : '#991B1B', fontFamily: 'Inter_500Medium' },
                  ]}
                >
                  Net Balance
                </Text>
                <Text
                  style={[
                    styles.overviewValue,
                    { color: netSavings >= 0 ? '#2563EB' : '#DC2626', fontFamily: 'Inter_700Bold' },
                  ]}
                >
                  ₹{netSavings.toLocaleString('en-IN')}
                </Text>
              </View>
              <View style={[styles.overviewCard, { backgroundColor: '#EDE9FE', flex: 1 }]}>
                <Text style={[styles.overviewLabel, { color: '#5B21B6', fontFamily: 'Inter_500Medium' }]}>Savings Rate</Text>
                <Text style={[styles.overviewValue, { color: '#7C3AED', fontFamily: 'Inter_700Bold' }]}>
                  {savingsRate >= 0 ? savingsRate.toFixed(1) : '0.0'}%
                </Text>
              </View>
            </View>

            {/* Income vs Expense Progress */}
            {income > 0 && (
              <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
                <Text style={[styles.sectionTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
                  Income Distribution
                </Text>
                <View style={styles.distRow}>
                  <View
                    style={[
                      styles.distFill,
                      {
                        flex: Math.max(expenseRate, 0.01),
                        backgroundColor: '#EF4444',
                        borderTopLeftRadius: 6,
                        borderBottomLeftRadius: 6,
                        borderTopRightRadius: expenseRate >= 100 ? 6 : 0,
                        borderBottomRightRadius: expenseRate >= 100 ? 6 : 0,
                      },
                    ]}
                  />
                  {savingsRate > 0 && (
                    <View
                      style={[
                        styles.distFill,
                        {
                          flex: Math.max(savingsRate, 0.01),
                          backgroundColor: '#22C55E',
                          borderTopRightRadius: 6,
                          borderBottomRightRadius: 6,
                          borderTopLeftRadius: 0,
                          borderBottomLeftRadius: 0,
                        },
                      ]}
                    />
                  )}
                </View>
                <View style={styles.distLegend}>
                  <View style={styles.legendItem}>
                    <View style={[styles.legendDot, { backgroundColor: '#EF4444' }]} />
                    <Text style={[styles.legendText, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                      Expenses {expenseRate.toFixed(0)}%
                    </Text>
                  </View>
                  {savingsRate > 0 && (
                    <View style={styles.legendItem}>
                      <View style={[styles.legendDot, { backgroundColor: '#22C55E' }]} />
                      <Text style={[styles.legendText, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                        Remaining {savingsRate.toFixed(0)}%
                      </Text>
                    </View>
                  )}
                </View>
              </View>
            )}

            {/* Expense Breakdown Bar Chart */}
            {allExpenses.length > 0 && (
              <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
                <Text style={[styles.sectionTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
                  Expense Breakdown
                </Text>
                {allExpenses
                  .sort((a, b) => b.value - a.value)
                  .map((exp) => {
                    const pct = (exp.value / maxVal) * 100;
                    const ofTotal = totalExpenses > 0 ? ((exp.value / totalExpenses) * 100).toFixed(0) : '0';
                    return (
                      <View key={exp.key} style={styles.barRow}>
                        <Text
                          style={[styles.barLabel, { color: colors.foreground, fontFamily: 'Inter_400Regular' }]}
                          numberOfLines={1}
                        >
                          {exp.label}
                        </Text>
                        <View style={[styles.barTrack, { backgroundColor: colors.muted }]}>
                          <View
                            style={[
                              styles.barFill,
                              { width: `${pct}%` as `${number}%`, backgroundColor: exp.color },
                            ]}
                          />
                        </View>
                        <View style={styles.barRight}>
                          <Text style={[styles.barValue, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
                            ₹{exp.value.toLocaleString('en-IN')}
                          </Text>
                          <Text style={[styles.barPct, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                            {ofTotal}%
                          </Text>
                        </View>
                      </View>
                    );
                  })}
              </View>
            )}

            {/* Summary Stats */}
            <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
              <Text style={[styles.sectionTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
                Monthly Summary
              </Text>
              <StatRow label="Monthly Income" value={`₹${income.toLocaleString('en-IN')}`} color="#22C55E" />
              <View style={[styles.divider, { backgroundColor: colors.border }]} />
              <StatRow label="Total Expenses" value={`₹${totalExpenses.toLocaleString('en-IN')}`} color="#EF4444" />
              <View style={[styles.divider, { backgroundColor: colors.border }]} />
              {(monthlyData.expenses.savings ?? 0) > 0 && (
                <>
                  <StatRow
                    label="Savings"
                    value={`₹${(monthlyData.expenses.savings ?? 0).toLocaleString('en-IN')}`}
                    color="#3B82F6"
                  />
                  <View style={[styles.divider, { backgroundColor: colors.border }]} />
                </>
              )}
              {(monthlyData.expenses.investments ?? 0) > 0 && (
                <>
                  <StatRow
                    label="Investments"
                    value={`₹${(monthlyData.expenses.investments ?? 0).toLocaleString('en-IN')}`}
                    color="#7C3AED"
                  />
                  <View style={[styles.divider, { backgroundColor: colors.border }]} />
                </>
              )}
              <StatRow
                label="Net Balance"
                value={`₹${netSavings.toLocaleString('en-IN')}`}
                color={netSavings >= 0 ? '#22C55E' : '#EF4444'}
              />
            </View>
          </>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { marginBottom: 20 },
  pageTitle: { fontSize: 26, marginBottom: 4 },
  pageSub: { fontSize: 14 },
  empty: { alignItems: 'center', paddingTop: 60, gap: 14 },
  emptyIconWrap: { width: 80, height: 80, borderRadius: 24, alignItems: 'center', justifyContent: 'center' },
  emptyTitle: { fontSize: 20, textAlign: 'center' },
  emptySub: { fontSize: 14, textAlign: 'center', lineHeight: 22, paddingHorizontal: 20 },
  overviewRow: { flexDirection: 'row', gap: 10, marginBottom: 10 },
  overviewCard: { borderRadius: 16, padding: 16 },
  overviewLabel: { fontSize: 12, marginBottom: 4 },
  overviewValue: { fontSize: 20 },
  section: { borderRadius: 18, padding: 18, marginBottom: 14, borderWidth: 1 },
  sectionTitle: { fontSize: 14, marginBottom: 16 },
  distRow: { flexDirection: 'row', height: 12, borderRadius: 6, overflow: 'hidden', marginBottom: 10 },
  distFill: { height: '100%' },
  distLegend: { flexDirection: 'row', gap: 16 },
  legendItem: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  legendDot: { width: 8, height: 8, borderRadius: 4 },
  legendText: { fontSize: 12 },
  barRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 12, gap: 10 },
  barLabel: { width: 84, fontSize: 12 },
  barTrack: { flex: 1, height: 8, borderRadius: 4, overflow: 'hidden' },
  barFill: { height: '100%', borderRadius: 4 },
  barRight: { width: 100, alignItems: 'flex-end' },
  barValue: { fontSize: 12 },
  barPct: { fontSize: 11 },
  statRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, gap: 10 },
  statDot: { width: 8, height: 8, borderRadius: 4 },
  statLabel: { flex: 1, fontSize: 14 },
  statValue: { fontSize: 14 },
  divider: { height: 1, marginLeft: 18 },
});
