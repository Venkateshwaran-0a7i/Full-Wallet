import React from 'react';
import {
  Alert,
  Image,
  Platform,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useColors } from '@/hooks/useColors';
import { useFinance } from '@/context/FinanceContext';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';

function SettingRow({
  icon,
  label,
  sublabel,
  onPress,
  rightEl,
  destructive,
}: {
  icon: React.ComponentProps<typeof Feather>['name'];
  label: string;
  sublabel?: string;
  onPress?: () => void;
  rightEl?: React.ReactNode;
  destructive?: boolean;
}) {
  const colors = useColors();
  return (
    <TouchableOpacity
      style={[styles.settingRow, { borderBottomColor: colors.border }]}
      onPress={onPress}
      activeOpacity={onPress ? 0.7 : 1}
      disabled={!onPress && !rightEl}
    >
      <View style={[styles.settingIcon, { backgroundColor: destructive ? '#FEE2E2' : colors.secondary }]}>
        <Feather name={icon} size={17} color={destructive ? '#DC2626' : colors.primary} />
      </View>
      <View style={styles.settingText}>
        <Text
          style={[
            styles.settingLabel,
            { color: destructive ? colors.destructive : colors.foreground, fontFamily: 'Inter_500Medium' },
          ]}
        >
          {label}
        </Text>
        {sublabel && (
          <Text style={[styles.settingSubLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
            {sublabel}
          </Text>
        )}
      </View>
      {rightEl ?? (onPress && !rightEl && (
        <Feather name="chevron-right" size={16} color={colors.mutedForeground} />
      ))}
    </TouchableOpacity>
  );
}

function SectionHeader({ title }: { title: string }) {
  const colors = useColors();
  return (
    <Text style={[styles.sectionHeader, { color: colors.mutedForeground, fontFamily: 'Inter_600SemiBold' }]}>
      {title}
    </Text>
  );
}

export default function SettingsScreen() {
  const insets = useSafeAreaInsets();
  const colors = useColors();
  const { themePreference, setThemePreference, clearAllData, monthlyData, goals, totalExpenses } = useFinance();

  const isWeb = Platform.OS === 'web';
  const paddingTop = isWeb ? insets.top + 67 : insets.top + 20;
  const paddingBottom = isWeb ? 34 + 90 : insets.bottom + 90;

  const isDarkMode = themePreference === 'dark';

  const handleThemeToggle = async (val: boolean) => {
    const pref = val ? 'dark' : 'light';
    await setThemePreference(pref);
    Haptics.selectionAsync();
  };

  const handleSystemTheme = async () => {
    await setThemePreference('system');
    Haptics.selectionAsync();
  };

  const handleClearData = () => {
    Alert.alert(
      'Clear All Data',
      'This will permanently delete all your expenses and savings goals. This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear All',
          style: 'destructive',
          onPress: async () => {
            await clearAllData();
            await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Warning);
          },
        },
      ],
    );
  };

  const hasData = (monthlyData.income ?? 0) > 0 || totalExpenses > 0 || goals.length > 0;

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <ScrollView
        contentContainerStyle={{ paddingTop, paddingBottom, paddingHorizontal: 20 }}
        showsVerticalScrollIndicator={false}
      >
        {/* App Identity */}
        <View style={[styles.appCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Image
            source={require('@/assets/images/icon.png')}
            style={styles.appLogo}
            resizeMode="contain"
          />
          <Text style={[styles.appName, { color: colors.primary, fontFamily: 'Inter_700Bold' }]}>
            My Wallet Is Full
          </Text>
          <Text style={[styles.appTagline, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
            Personal Finance Manager
          </Text>
          <View style={[styles.versionPill, { backgroundColor: colors.secondary }]}>
            <Text style={[styles.versionText, { color: colors.mutedForeground, fontFamily: 'Inter_500Medium' }]}>
              Version 1.0.0
            </Text>
          </View>
        </View>

        {/* Theme */}
        <SectionHeader title="APPEARANCE" />
        <View style={[styles.settingGroup, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <SettingRow
            icon="moon"
            label="Dark Mode"
            sublabel={themePreference === 'system' ? 'Following system setting' : isDarkMode ? 'On' : 'Off'}
            rightEl={
              <Switch
                value={isDarkMode}
                onValueChange={handleThemeToggle}
                trackColor={{ false: colors.border, true: colors.primary }}
                thumbColor="#FFFFFF"
              />
            }
          />
          <SettingRow
            icon="monitor"
            label="Use System Default"
            sublabel="Let your device decide"
            onPress={handleSystemTheme}
            rightEl={
              themePreference === 'system' ? (
                <Feather name="check" size={16} color={colors.primary} />
              ) : undefined
            }
          />
        </View>

        {/* Data Summary */}
        <SectionHeader title="YOUR DATA" />
        <View style={[styles.settingGroup, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <View style={styles.dataGrid}>
            <View style={[styles.dataItem, { borderColor: colors.border }]}>
              <Text style={[styles.dataValue, { color: colors.primary, fontFamily: 'Inter_700Bold' }]}>
                {(monthlyData.income ?? 0) > 0 ? `₹${(monthlyData.income ?? 0).toLocaleString('en-IN')}` : '—'}
              </Text>
              <Text style={[styles.dataLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                Monthly Income
              </Text>
            </View>
            <View style={[styles.dataItem, { borderColor: colors.border }]}>
              <Text style={[styles.dataValue, { color: colors.primary, fontFamily: 'Inter_700Bold' }]}>
                {totalExpenses > 0 ? `₹${totalExpenses.toLocaleString('en-IN')}` : '—'}
              </Text>
              <Text style={[styles.dataLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                Total Expenses
              </Text>
            </View>
            <View style={[styles.dataItem, { borderColor: colors.border }]}>
              <Text style={[styles.dataValue, { color: colors.primary, fontFamily: 'Inter_700Bold' }]}>
                {goals.length}
              </Text>
              <Text style={[styles.dataLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                Savings Goals
              </Text>
            </View>
          </View>
        </View>

        {/* Danger Zone */}
        {hasData && (
          <>
            <SectionHeader title="DATA MANAGEMENT" />
            <View style={[styles.settingGroup, { backgroundColor: colors.card, borderColor: colors.border }]}>
              <SettingRow
                icon="trash-2"
                label="Clear All Data"
                sublabel="Remove all expenses and goals"
                onPress={handleClearData}
                destructive
              />
            </View>
          </>
        )}

        {/* About */}
        <SectionHeader title="ABOUT" />
        <View style={[styles.settingGroup, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <View style={[styles.aboutBlock, { borderBottomColor: colors.border }]}>
            <Feather name="shield" size={17} color={colors.primary} />
            <View style={{ flex: 1 }}>
              <Text style={[styles.aboutTitle, { color: colors.foreground, fontFamily: 'Inter_500Medium' }]}>
                Your data stays on your device
              </Text>
              <Text style={[styles.aboutBody, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                All financial information is stored locally. No data is ever sent to any server.
              </Text>
            </View>
          </View>
          <View style={styles.aboutBlock}>
            <Feather name="info" size={17} color={colors.primary} />
            <View style={{ flex: 1 }}>
              <Text style={[styles.aboutTitle, { color: colors.foreground, fontFamily: 'Inter_500Medium' }]}>
                No demo data policy
              </Text>
              <Text style={[styles.aboutBody, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
                This app never pre-fills or suggests financial data. Every number comes from you.
              </Text>
            </View>
          </View>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  appCard: {
    borderRadius: 20,
    padding: 24,
    alignItems: 'center',
    marginBottom: 24,
    borderWidth: 1,
    gap: 6,
  },
  appLogo: { width: 72, height: 72, marginBottom: 6 },
  appName: { fontSize: 20 },
  appTagline: { fontSize: 13 },
  versionPill: {
    borderRadius: 20,
    paddingHorizontal: 12,
    paddingVertical: 5,
    marginTop: 4,
  },
  versionText: { fontSize: 12 },
  sectionHeader: {
    fontSize: 11,
    letterSpacing: 0.8,
    marginBottom: 8,
    marginLeft: 4,
    marginTop: 4,
  },
  settingGroup: {
    borderRadius: 16,
    borderWidth: 1,
    overflow: 'hidden',
    marginBottom: 20,
  },
  settingRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 13,
    borderBottomWidth: 1,
    gap: 12,
  },
  settingIcon: {
    width: 34,
    height: 34,
    borderRadius: 9,
    alignItems: 'center',
    justifyContent: 'center',
  },
  settingText: { flex: 1 },
  settingLabel: { fontSize: 14 },
  settingSubLabel: { fontSize: 12, marginTop: 1 },
  dataGrid: { flexDirection: 'row' },
  dataItem: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 16,
    borderRightWidth: 1,
  },
  dataValue: { fontSize: 18, marginBottom: 3 },
  dataLabel: { fontSize: 11, textAlign: 'center' },
  aboutBlock: {
    flexDirection: 'row',
    gap: 12,
    padding: 16,
    borderBottomWidth: 1,
    alignItems: 'flex-start',
  },
  aboutTitle: { fontSize: 13, marginBottom: 3 },
  aboutBody: { fontSize: 12, lineHeight: 18 },
});
