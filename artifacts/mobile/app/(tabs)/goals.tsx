import React, { useState } from 'react';
import {
  Alert,
  FlatList,
  Modal,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useColors } from '@/hooks/useColors';
import { useFinance, type SavingsGoal } from '@/context/FinanceContext';
import { Feather } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';

function GoalCard({ goal, onDelete, onUpdateSaved }: {
  goal: SavingsGoal;
  onDelete: (id: string) => void;
  onUpdateSaved: (id: string, amount: number) => void;
}) {
  const colors = useColors();
  const progress = goal.targetAmount > 0 ? Math.min((goal.savedAmount / goal.targetAmount) * 100, 100) : 0;
  const isComplete = progress >= 100;

  const handleAddSaved = () => {
    Alert.prompt(
      'Update Savings',
      `Current: ₹${goal.savedAmount.toLocaleString('en-IN')}\nEnter new total saved amount:`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Update',
          onPress: (text) => {
            const val = parseFloat(text ?? '');
            if (!isNaN(val) && val >= 0) {
              onUpdateSaved(goal.id, val);
              Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
            }
          },
        },
      ],
      'plain-text',
      goal.savedAmount.toString(),
      'numeric',
    );
  };

  const handleDelete = () => {
    Alert.alert(
      'Delete Goal',
      `Remove "${goal.title}" from your goals?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: () => {
            onDelete(goal.id);
            Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
          },
        },
      ],
    );
  };

  return (
    <View style={[styles.goalCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
      <View style={styles.goalCardTop}>
        <View style={styles.goalTitleRow}>
          {isComplete && (
            <View style={[styles.completeBadge, { backgroundColor: '#DCFCE7' }]}>
              <Feather name="check-circle" size={12} color="#15803D" />
              <Text style={[styles.completeBadgeText, { fontFamily: 'Inter_600SemiBold' }]}>Achieved</Text>
            </View>
          )}
          <Text style={[styles.goalTitle, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
            {goal.title}
          </Text>
        </View>
        <TouchableOpacity onPress={handleDelete} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
          <Feather name="trash-2" size={16} color={colors.mutedForeground} />
        </TouchableOpacity>
      </View>

      <View style={styles.goalAmounts}>
        <View>
          <Text style={[styles.amtLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
            Saved
          </Text>
          <Text style={[styles.amtValue, { color: colors.primary, fontFamily: 'Inter_700Bold' }]}>
            ₹{goal.savedAmount.toLocaleString('en-IN')}
          </Text>
        </View>
        <View style={[styles.amtDivider, { backgroundColor: colors.border }]} />
        <View>
          <Text style={[styles.amtLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
            Target
          </Text>
          <Text style={[styles.amtValue, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
            ₹{goal.targetAmount.toLocaleString('en-IN')}
          </Text>
        </View>
        <View style={[styles.amtDivider, { backgroundColor: colors.border }]} />
        <View>
          <Text style={[styles.amtLabel, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
            Remaining
          </Text>
          <Text style={[styles.amtValue, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
            ₹{Math.max(0, goal.targetAmount - goal.savedAmount).toLocaleString('en-IN')}
          </Text>
        </View>
      </View>

      <View style={[styles.trackBg, { backgroundColor: colors.muted }]}>
        <View
          style={[
            styles.trackFill,
            {
              width: `${progress}%` as `${number}%`,
              backgroundColor: isComplete ? '#22C55E' : colors.primary,
            },
          ]}
        />
      </View>
      <View style={styles.progressRow}>
        <Text style={[styles.progressPct, { color: colors.mutedForeground, fontFamily: 'Inter_500Medium' }]}>
          {progress.toFixed(0)}% complete
        </Text>
        <TouchableOpacity
          style={[styles.updateBtn, { backgroundColor: colors.secondary }]}
          onPress={handleAddSaved}
          activeOpacity={0.8}
        >
          <Feather name="edit-2" size={13} color={colors.primary} />
          <Text style={[styles.updateBtnText, { color: colors.primary, fontFamily: 'Inter_600SemiBold' }]}>
            Update
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

export default function GoalsScreen() {
  const insets = useSafeAreaInsets();
  const colors = useColors();
  const { goals, addGoal, updateGoalSaved, deleteGoal } = useFinance();
  const [modalVisible, setModalVisible] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newTarget, setNewTarget] = useState('');

  const isWeb = Platform.OS === 'web';
  const paddingTop = isWeb ? insets.top + 67 : insets.top + 20;

  const handleAddGoal = async () => {
    const title = newTitle.trim();
    const target = parseFloat(newTarget);
    if (!title) {
      Alert.alert('Missing Title', 'Please enter a goal name.');
      return;
    }
    if (isNaN(target) || target <= 0) {
      Alert.alert('Invalid Amount', 'Please enter a valid target amount.');
      return;
    }
    await addGoal(title, target);
    await Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    setNewTitle('');
    setNewTarget('');
    setModalVisible(false);
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      <FlatList
        data={goals}
        keyExtractor={(item) => item.id}
        contentContainerStyle={{
          paddingTop,
          paddingBottom: isWeb ? 34 + 90 : insets.bottom + 90,
          paddingHorizontal: 20,
        }}
        showsVerticalScrollIndicator={false}
        ListHeaderComponent={
          <View style={styles.header}>
            <Text style={[styles.pageTitle, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
              Savings Goals
            </Text>
            <Text style={[styles.pageSub, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
              Track your financial milestones
            </Text>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <View style={[styles.emptyIconWrap, { backgroundColor: colors.secondary }]}>
              <Feather name="flag" size={40} color={colors.mutedForeground} />
            </View>
            <Text style={[styles.emptyTitle, { color: colors.foreground, fontFamily: 'Inter_600SemiBold' }]}>
              No savings goals yet
            </Text>
            <Text style={[styles.emptySub, { color: colors.mutedForeground, fontFamily: 'Inter_400Regular' }]}>
              Create your first savings goal to start working toward it.
            </Text>
          </View>
        }
        renderItem={({ item }) => (
          <GoalCard
            goal={item}
            onDelete={deleteGoal}
            onUpdateSaved={updateGoalSaved}
          />
        )}
        scrollEnabled={goals.length > 0}
      />

      {/* FAB */}
      <TouchableOpacity
        style={[
          styles.fab,
          {
            backgroundColor: colors.primary,
            bottom: isWeb ? 34 + 90 : insets.bottom + 90,
          },
        ]}
        onPress={() => setModalVisible(true)}
        activeOpacity={0.85}
      >
        <Feather name="plus" size={26} color={colors.primaryForeground} />
      </TouchableOpacity>

      {/* Add Goal Modal */}
      <Modal
        visible={modalVisible}
        animationType="slide"
        transparent
        onRequestClose={() => setModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <View style={[styles.modalSheet, { backgroundColor: colors.card }]}>
            <View style={[styles.modalHandle, { backgroundColor: colors.border }]} />
            <Text style={[styles.modalTitle, { color: colors.foreground, fontFamily: 'Inter_700Bold' }]}>
              New Savings Goal
            </Text>

            <Text style={[styles.modalLabel, { color: colors.mutedForeground, fontFamily: 'Inter_500Medium' }]}>
              Goal Name
            </Text>
            <TextInput
              style={[
                styles.modalInput,
                { backgroundColor: colors.muted, color: colors.foreground, borderColor: colors.border, fontFamily: 'Inter_400Regular' },
              ]}
              value={newTitle}
              onChangeText={setNewTitle}
              placeholder="e.g. Buy a Laptop, Emergency Fund..."
              placeholderTextColor={colors.mutedForeground}
              autoFocus
              returnKeyType="next"
            />

            <Text style={[styles.modalLabel, { color: colors.mutedForeground, fontFamily: 'Inter_500Medium' }]}>
              Target Amount (₹)
            </Text>
            <TextInput
              style={[
                styles.modalInput,
                { backgroundColor: colors.muted, color: colors.foreground, borderColor: colors.border, fontFamily: 'Inter_400Regular' },
              ]}
              value={newTarget}
              onChangeText={setNewTarget}
              placeholder="0"
              placeholderTextColor={colors.mutedForeground}
              keyboardType="numeric"
              returnKeyType="done"
              onSubmitEditing={handleAddGoal}
            />

            <View style={styles.modalActions}>
              <TouchableOpacity
                style={[styles.modalCancel, { backgroundColor: colors.muted }]}
                onPress={() => { setModalVisible(false); setNewTitle(''); setNewTarget(''); }}
                activeOpacity={0.8}
              >
                <Text style={[styles.modalCancelText, { color: colors.foreground, fontFamily: 'Inter_500Medium' }]}>
                  Cancel
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalConfirm, { backgroundColor: colors.primary }]}
                onPress={handleAddGoal}
                activeOpacity={0.85}
              >
                <Text style={[styles.modalConfirmText, { color: colors.primaryForeground, fontFamily: 'Inter_600SemiBold' }]}>
                  Create Goal
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  header: { marginBottom: 20 },
  pageTitle: { fontSize: 26, marginBottom: 4 },
  pageSub: { fontSize: 14 },
  goalCard: {
    borderRadius: 18,
    padding: 18,
    marginBottom: 14,
    borderWidth: 1,
  },
  goalCardTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 14 },
  goalTitleRow: { flex: 1, gap: 6 },
  completeBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    borderRadius: 20,
    paddingHorizontal: 8,
    paddingVertical: 3,
    alignSelf: 'flex-start',
  },
  completeBadgeText: { fontSize: 11, color: '#15803D' },
  goalTitle: { fontSize: 17 },
  goalAmounts: { flexDirection: 'row', alignItems: 'center', marginBottom: 14, gap: 0 },
  amtLabel: { fontSize: 11, marginBottom: 2 },
  amtValue: { fontSize: 16 },
  amtDivider: { width: 1, height: 32, marginHorizontal: 16 },
  trackBg: { height: 8, borderRadius: 4, overflow: 'hidden', marginBottom: 10 },
  trackFill: { height: '100%', borderRadius: 4 },
  progressRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  progressPct: { fontSize: 12 },
  updateBtn: { flexDirection: 'row', alignItems: 'center', gap: 5, borderRadius: 10, paddingHorizontal: 12, paddingVertical: 6 },
  updateBtnText: { fontSize: 12 },
  empty: { alignItems: 'center', paddingTop: 60, gap: 14 },
  emptyIconWrap: { width: 80, height: 80, borderRadius: 24, alignItems: 'center', justifyContent: 'center' },
  emptyTitle: { fontSize: 20, textAlign: 'center' },
  emptySub: { fontSize: 14, textAlign: 'center', lineHeight: 22, paddingHorizontal: 30 },
  fab: {
    position: 'absolute',
    right: 24,
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 6,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'flex-end',
  },
  modalSheet: {
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    padding: 24,
    paddingTop: 16,
    paddingBottom: 40,
  },
  modalHandle: {
    width: 36,
    height: 4,
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 20,
  },
  modalTitle: { fontSize: 20, marginBottom: 20 },
  modalLabel: { fontSize: 13, marginBottom: 8 },
  modalInput: {
    borderRadius: 12,
    borderWidth: 1,
    paddingHorizontal: 14,
    paddingVertical: 13,
    fontSize: 15,
    marginBottom: 16,
  },
  modalActions: { flexDirection: 'row', gap: 12, marginTop: 8 },
  modalCancel: { flex: 1, borderRadius: 14, paddingVertical: 15, alignItems: 'center' },
  modalCancelText: { fontSize: 15 },
  modalConfirm: { flex: 1, borderRadius: 14, paddingVertical: 15, alignItems: 'center' },
  modalConfirmText: { fontSize: 15 },
});
