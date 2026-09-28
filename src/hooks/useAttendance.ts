import { useState, useEffect } from 'react';
import { hourlyStore } from '../services/attendanceStore';
import { DailyHourlyReport, HourlySlot } from '../types/attendance';

export function useHourlyReport(date: string) {
  const [, setTick] = useState(0);

  useEffect(() => {
    const unsubscribe = hourlyStore.subscribe(() => {
      setTick((t) => t + 1);
    });
    return unsubscribe;
  }, []);

  const report = hourlyStore.getHourlyReport(date);

  return {
    report,
    updateSlot: (slotId: string, updates: Partial<HourlySlot>) =>
      hourlyStore.updateHourlySlot(date, slotId, updates),
    applyTemplate: () => hourlyStore.applyTemplate(date),
    importReport: (importedSlots: Array<{ timeRange?: string; slotId?: string; activity: string; status?: string }>) =>
      hourlyStore.importReport(date, importedSlots),
    resetReport: () => hourlyStore.resetHourlyReport(date),
    resetAll: () => hourlyStore.resetAll(),
  };
}
