import { DailyHourlyReport, HourlySlot } from '../types/attendance';
import { DEFAULT_HOURLY_SLOTS } from '../data/initialData';

const HOURLY_STORAGE_KEY = 'hourly_status_report_v2';

type Listener = () => void;

class HourlyReportStore {
  private listeners: Set<Listener> = new Set();

  public subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    return () => {
      this.listeners.delete(listener);
    };
  }

  private notify() {
    this.listeners.forEach((l) => l());
  }

  private loadAllReports(): Record<string, DailyHourlyReport> {
    try {
      const stored = localStorage.getItem(HOURLY_STORAGE_KEY);
      if (stored) {
        return JSON.parse(stored);
      }
    } catch (e) {
      console.warn('Could not read reports from localStorage', e);
    }
    return {};
  }

  private saveAllReports(reports: Record<string, DailyHourlyReport>) {
    try {
      localStorage.setItem(HOURLY_STORAGE_KEY, JSON.stringify(reports));
      this.notify();
    } catch (e) {
      console.error('Failed to save reports to localStorage', e);
    }
  }

  public getHourlyReport(date: string): DailyHourlyReport {
    const all = this.loadAllReports();
    if (all[date]) {
      return all[date];
    }

    const defaultReport: DailyHourlyReport = {
      id: `rep_${date}`,
      date,
      slots: DEFAULT_HOURLY_SLOTS.map((s) => ({
        ...s,
        activity: s.isBreak ? 'Lunch break' : '',
        status: s.isBreak ? 'completed' : 'pending',
      })),
      totalLoggedHours: 0.5,
      targetHours: 12,
      updatedAt: new Date().toISOString(),
    };

    all[date] = defaultReport;
    this.saveAllReports(all);
    return defaultReport;
  }

  public updateHourlySlot(
    date: string,
    slotId: string,
    updates: Partial<HourlySlot>
  ): DailyHourlyReport {
    const report = this.getHourlyReport(date);
    const updatedSlots = report.slots.map((s) => {
      if (s.id === slotId) {
        const newActivity = updates.activity !== undefined ? updates.activity : s.activity;
        let newStatus = updates.status !== undefined ? updates.status : s.status;
        if (updates.activity !== undefined && newActivity.trim().length > 0 && newStatus === 'pending') {
          newStatus = 'completed';
        }
        return {
          ...s,
          ...updates,
          status: newStatus,
        };
      }
      return s;
    });

    const loggedHours = updatedSlots
      .filter((s) => s.activity.trim().length > 0)
      .reduce((sum, s) => sum + s.durationHours, 0);

    const updatedReport: DailyHourlyReport = {
      ...report,
      slots: updatedSlots,
      totalLoggedHours: Math.min(12, Number(loggedHours.toFixed(1))),
      updatedAt: new Date().toISOString(),
    };

    const all = this.loadAllReports();
    all[date] = updatedReport;
    this.saveAllReports(all);
    return updatedReport;
  }

  public applyTemplate(date: string): DailyHourlyReport {
    const report = this.getHourlyReport(date);
    const sampleTasks: Record<string, string> = {
      slot_1: 'Morning standup, priority check-in, and Slack inbox triage',
      slot_2: 'Feature architecture design and component API drafting',
      slot_3: 'Core system logic engineering & test coverage',
      slot_4: 'Cross-functional sync & client feedback review',
      slot_5: 'Lunch break',
      slot_6: 'Code review, PR revisions & CI pipeline checks',
      slot_7: 'High-focus module development and database queries',
      slot_8: 'Internal team alignment & staging deployment',
      slot_9: 'Bug triage, performance tuning & latency reduction',
      slot_10: 'Documentation update & customer onboarding setup',
      slot_11: 'Production monitoring & release candidate verification',
      slot_12: 'Daily retrospective and next-day roadmap sync',
      slot_13: 'Shift handover log and desk wrap-up notes',
    };

    const updatedSlots = report.slots.map((s) => ({
      ...s,
      activity: sampleTasks[s.id] || s.activity,
      status: 'completed' as const,
    }));

    const loggedHours = updatedSlots
      .filter((s) => s.activity.trim().length > 0)
      .reduce((sum, s) => sum + s.durationHours, 0);

    const updatedReport: DailyHourlyReport = {
      ...report,
      slots: updatedSlots,
      totalLoggedHours: Math.min(12, Number(loggedHours.toFixed(1))),
      updatedAt: new Date().toISOString(),
    };

    const all = this.loadAllReports();
    all[date] = updatedReport;
    this.saveAllReports(all);
    return updatedReport;
  }

  public importReport(
    date: string,
    importedSlots: Array<{ timeRange?: string; slotId?: string; activity: string; status?: string }>
  ): DailyHourlyReport {
    const report = this.getHourlyReport(date);
    const updatedSlots = report.slots.map((s, idx) => {
      const match = importedSlots.find(
        (item) =>
          (item.slotId && item.slotId === s.id) ||
          (item.timeRange &&
            (item.timeRange.toLowerCase().trim() === s.timeRange.toLowerCase().trim() ||
              item.timeRange.replace(/[\s\u2013\u2014-]/g, '') === s.timeRange.replace(/[\s\u2013\u2014-]/g, '')))
      ) || (importedSlots[idx] && !importedSlots[idx].timeRange && !importedSlots[idx].slotId ? importedSlots[idx] : undefined);

      if (match && typeof match.activity === 'string') {
        const activity = match.activity;
        let status = (match.status as any) || s.status;
        if (s.isBreak) {
          status = 'completed';
        } else if (activity.trim().length > 0 && (!status || status === 'pending')) {
          status = 'completed';
        }
        return {
          ...s,
          activity,
          status,
        };
      }
      return s;
    });

    const loggedHours = updatedSlots
      .filter((s) => s.activity.trim().length > 0)
      .reduce((sum, s) => sum + s.durationHours, 0);

    const updatedReport: DailyHourlyReport = {
      ...report,
      slots: updatedSlots,
      totalLoggedHours: Math.min(12, Number(loggedHours.toFixed(1))),
      updatedAt: new Date().toISOString(),
    };

    const all = this.loadAllReports();
    all[date] = updatedReport;
    this.saveAllReports(all);
    return updatedReport;
  }

  public resetHourlyReport(date: string): DailyHourlyReport {
    const fresh: DailyHourlyReport = {
      id: `rep_${date}`,
      date,
      slots: DEFAULT_HOURLY_SLOTS.map((s) => ({
        ...s,
        activity: s.isBreak ? 'Lunch break' : '',
        status: s.isBreak ? 'completed' : 'pending',
      })),
      totalLoggedHours: 0.5,
      targetHours: 12,
      updatedAt: new Date().toISOString(),
    };

    const all = this.loadAllReports();
    all[date] = fresh;
    this.saveAllReports(all);
    return fresh;
  }

  public resetAll() {
    localStorage.removeItem(HOURLY_STORAGE_KEY);
    this.notify();
  }
}

export const hourlyStore = new HourlyReportStore();
