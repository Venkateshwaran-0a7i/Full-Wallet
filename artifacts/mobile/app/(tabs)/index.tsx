import React from 'react';
import {
  Image,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useColors } from '@/hooks/useColors';
import { useFinance } from '@/context/FinanceContext';
import { Feather } from '@expo/vector-icons';
import { router } from 'expo-router';

function formatInr(value: number | null | undefined): string {
  if (value === null || value === undefined || value === 0) return '—';
  return `₹${value.toLocaleString('en-IN')}`;
}

type FeatherIconName = React.ComponentProps<typeof Feather>['name'];

interface SummaryCardProps {
  label: string;
  value: string;
  icon: FeatherIconName;
  iconColor: string;
  iconBg: string;
  iconBgDark: string;
}

function SummaryCard({ label, value, icon, iconColor, iconBg, iconBgDark }: SummaryCardProps) {
  const colors = useColors();
  const isDark = colors.background === '#0A1810';
  return (
    <View style={[styles.summaryCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={[styles.summaryIconWrap, { backgroundColor: isDark ? iconBgDark : iconBg }]}>
        <Feather name={icon} size={17} color={iconColor} />
      </View>
      <Text style={[styles.summaryLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
        {label}
      </Text>
      <Text style={[styles.summaryValue, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
        {value}
      </Text>
    </View>
  );
}

export default function DashboardScreen() {
  const insets = useSafeAreaInsets();
  const colors = useColors();
  const { monthlyData, totalExpenses, balance, goals } = useFinance();

  const income = monthlyData.income ?? 0;
  const hasData = income > 0 || totalExpenses > 0;
  const budgetPercent = income > 0 ? Math.min((totalExpenses / income) * 100, 100) : 0;

  const topExpenses = [
    { label: 'Rent', value: monthlyData.expenses.rent ?? 0, color: '#2563EB' },
    { label: 'Food', value: monthlyData.expenses.food ?? 0, color: '#D97706' },
    { label: 'Transport', value: monthlyData.expenses.transport ?? 0, color: '#059669' },
    { label: 'EMI', value: monthlyData.expenses.emi ?? 0, color: '#DC2626' },
    { label: 'Shopping', value: monthlyData.expenses.shopping ?? 0, color: '#7C3AED' },
    { label: 'Entertainment', value: monthlyData.expenses.entertainment ?? 0, color: '#E11D48' },
  ]
    .filter((e) => e.value > 0)
    .sort((a, b) => b.value - a.value)
    .slice(0, 5);

  const isWeb = Platform.OS === 'web';
  const paddingTop = isWeb ? insets.top + 67 : insets.top + 20;
  const paddingBottom = isWeb ? 34 + 90 : insets.bottom + 90;

  const balanceBg = hasData && balance < 0 ? '#EF4444' : colors.primary;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView
        contentContainerStyle={{ paddingTop, paddingBottom, paddingHorizontal: 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <Image
            source={require('@/assets/images/icon.png')}
            style={styles.logo}
            resizeMode="contain"
          />
          <View style={{ flex: 1 }}>
            <Text style={[styles.appName, { color: colors.primary, fontFamily: 'Inter_700Bold' }]}>
              My Wallet Is Full
            </Text>
            <Text style={[styles.appSub, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
              Your financial overview
            </Text>
          </View>
        </View>

        {/* Balance Hero Card */}
        <View style={[styles.heroCard, { backgroundColor: balanceBg }]}>
          <Text style={[styles.heroLabel, { fontFamily: 'Inter_500Medium' }]}>Current Balance</Text>
          <Text style={[styles.heroValue, { fontFamily: 'Inter_700Bold' }]}>
            {hasData ? `₹${balance.toLocaleString('en-IN')}` : '—'}
          </Text>
          <Text style={[styles.heroSub, { fontFamily: 'Inter_400Regular' }]}>
            {!hasData
              ? 'Add your expenses to begin tracking'
              : balance < 0
                ? 'Expenses exceed income'
                : 'You are on track this month'}
          </Text>
        </View>

        {/* Summary Grid */}
        <View style={styles.summaryGrid}>
          <SummaryCard
            label="Monthly Income"
            value={formatInr(monthlyData.income)}
            icon="trending-up"
            iconColor="#16A34A"
            iconBg="#DCFCE7"
            iconBgDark="#14532D"
          />
          <SummaryCard
            label="Total Expenses"
            value={totalExpenses > 0 ? `₹${totalExpenses.toLocaleString('en-IN')}` : '—'}
            icon="trending-down"
            iconColor="#DC2626"
            iconBg="#FEE2E2"
            iconBgDark="#450A0A"
          />
          <SummaryCard
            label="Savings"
            value={formatInr(monthlyData.expenses.savings)}
            icon="save"
            iconColor="#2563EB"
            iconBg="#DBEAFE"
            iconBgDark="#1E3A5F"
          />
          <SummaryCard
            label="Investments"
            value={formatInr(monthlyData.expenses.investments)}
            icon="activity"
            iconColor="#7C3AED"
            iconBg="#EDE9FE"
            iconBgDark="#2E1065"
          />
        </View>

        {/* Budget Progress */}
        {hasData && income > 0 && (
          <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <Text style={[styles.sectionTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
              Budget Progress
            </Text>
            <View style={[styles.progressTrack, { backgroundColor: colors.muted }]}>
              <View
                style={[
                  styles.progressFill,
                  {
                    width: `${budgetPercent}%` as `${number}%`,
                    backgroundColor:
                      budgetPercent > 90
                        ? '#EF4444'
                        : budgetPercent > 70
                          ? '#F59E0B'
                          : colors.primary,
                  },
                ]}
              />
            </View>
            <Text style={[styles.progressLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
              {budgetPercent.toFixed(0)}% of income used · ₹{(income - totalExpenses).toLocaleString('en-IN')} remaining
            </Text>
          </View>
        )}

        {/* Top Expenses */}
        {topExpenses.length > 0 && (
          <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <Text style={[styles.sectionTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
              Top Expenses
            </Text>
            {topExpenses.map((exp) => {
              const pct = totalExpenses > 0 ? (exp.value / totalExpenses) * 100 : 0;
              return (
                <View key={exp.label} style={styles.expRow}>
                  <View style={[styles.expDot, { backgroundColor: exp.color }]} />
                  <Text style={[styles.expLabel, { color: colors.foreground, fontFamily: 'Inter_400Regular' }]}>
                    {exp.label}
                  </Text>
                  <View style={[styles.expBarTrack, { backgroundColor: colors.muted }]}>
                    <View
                      style={[
                        styles.expBarFill,
                        { width: `${pct}%` as `${number}%`, backgroundColor: exp.color },
                      ]}
                    />
                  </View>
                  <Text style={[styles.expAmount, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
                    ₹{exp.value.toLocaleString('en-IN')}
                  </Text>
                </View>
              );
            })}
          </View>
        )}

        {/* Goals Preview */}
        {goals.length > 0 && (
          <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <View style={styles.sectionRow}>
              <Text style={[styles.sectionTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
                Savings Goals
              </Text>
              <TouchableOpacity onPress={() => router.push('/(tabs)/goals')}>
                <Text style={[styles.seeAll, { color: colors.primary, fontFamily: 'Inter_500Medium' }]}>
                  See all
                </Text>
              </TouchableOpacity>
            </View>
            {goals.slice(0, 2).map((goal) => {
              const prog =
                goal.targetAmount > 0
                  ? Math.min((goal.savedAmount / goal.targetAmount) * 100, 100)
                  : 0;
              return (
                <View key={goal.id} style={styles.goalItem}>
                  <View style={styles.goalItemRow}>
                    <Text style={[styles.goalItemTitle, { color: colors.foreground, fontFamily: 'Inter_500Medium' }]}>
                      {goal.title}
                    </Text>
                    <Text style={[styles.goalItemAmt, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                      ₹{goal.savedAmount.toLocaleString('en-IN')} / ₹{goal.targetAmount.toLocaleString('en-IN')}
                    </Text>
                  </View>
                  <View style={[styles.miniTrack, { backgroundColor: colors.muted }]}>
                    <View
                      style={[
                        styles.miniFill,
                        { width: `${prog}%` as `${number}%`, backgroundColor: colors.primary },
                      ]}
                    />
                  </View>
                </View>
              );
            })}
          </View>
        )}

        {/* Goal Text */}
        {!!monthlyData.goal && (
          <View style={[styles.section, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <Text style={[styles.sectionTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
              Financial Goal
            </Text>
            <Text style={[styles.goalText, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
              {monthlyData.goal}
            </Text>
          </View>
        )}

        {/* Empty State */}
        {!hasData && goals.length === 0 && (
          <View style={styles.empty}>
            <View style={[styles.emptyIconWrap, { backgroundColor: colors.secondary }]}>
              <Feather name="inbox" size={40} color={colors.mutedForeground} />
            </View>
            <Text style={[styles.emptyTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
              No expenses added yet
            </Text>
            <Text style={[styles.emptySub, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
              Start tracking your finances by adding your income and expenses.
            </Text>
            <TouchableOpacity
              style={[styles.ctaBtn, { backgroundColor: colors.primary }]}
              onPress={() => router.push('/(tabs)/expenses')}
              activeOpacity={0.8}
            >
              <Feather name="plus" size={18} color={colors.primaryForeground} />
              <Text style={[styles.ctaText, { color: colors.primaryForeground, fontFamily: 'Inter_600SemiBold' }]}>
                Add First Expense
              </Text>
            </TouchableOpacity>
          </View>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { flexDirection: 'row', alignItems: 'center', marginBottom: 20, gap: 12 },
  logo: { width: 46, height: 46 },
  appName: { fontSize: 17 },
  appSub: { fontSize: 12, marginTop: 2 },
  heroCard: {
    borderRadius: 22,
    padding: 26,
    marginBottom: 16,
  },
  heroLabel: { fontSize: 13, color: 'rgba(255,255,255,0.72)', marginBottom: 6 },
  heroValue: { fontSize: 44, color: '#FFFFFF', marginBottom: 6 },
  heroSub: { fontSize: 13, color: 'rgba(255,255,255,0.6)' },
  summaryGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginBottom: 16 },
  summaryCard: {
    width: '47.5%',
    borderRadius: 16,
    padding: 15,
    borderWidth: 1,
    gap: 5,
  },
  summaryIconWrap: {
    width: 36,
    height: 36,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 4,
  },
  summaryLabel: { fontSize: 12 },
  summaryValue: { fontSize: 17 },
  section: {
    borderRadius: 18,
    padding: 18,
    marginBottom: 14,
    borderWidth: 1,
  },
  sectionTitle: { fontSize: 14, marginBottom: 14 },
  sectionRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 },
  seeAll: { fontSize: 13 },
  progressTrack: { height: 10, borderRadius: 5, overflow: 'hidden', marginBottom: 8 },
  progressFill: { height: '100%', borderRadius: 5 },
  progressLabel: { fontSize: 12 },
  expRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8, gap: 8 },
  expDot: { width: 8, height: 8, borderRadius: 4 },
  expLabel: { width: 90, fontSize: 13 },
  expBarTrack: { flex: 1, height: 6, borderRadius: 3, overflow: 'hidden' },
  expBarFill: { height: '100%', borderRadius: 3 },
  expAmount: { fontSize: 13, width: 80, textAlign: 'right' },
  goalItem: { marginBottom: 12 },
  goalItemRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 },
  goalItemTitle: { fontSize: 14 },
  goalItemAmt: { fontSize: 12 },
  miniTrack: { height: 6, borderRadius: 3, overflow: 'hidden' },
  miniFill: { height: '100%', borderRadius: 3 },
  goalText: { fontSize: 14, lineHeight: 22 },
  empty: { alignItems: 'center', paddingVertical: 40, gap: 14 },
  emptyIconWrap: { width: 80, height: 80, borderRadius: 24, alignItems: 'center', justifyContent: 'center' },
  emptyTitle: { fontSize: 20, textAlign: 'center' },
  emptySub: { fontSize: 14, textAlign: 'center', lineHeight: 22, paddingHorizontal: 20 },
  ctaBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    borderRadius: 14,
    paddingHorizontal: 24,
    paddingVertical: 14,
    marginTop: 4,
  },
  ctaText: { fontSize: 15 },
});
