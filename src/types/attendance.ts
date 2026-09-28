export interface HourlySlot {
  id: string;
  timeRange: string;
  startHour: number;
  startMinute: number;
  endHour: number;
  endMinute: number;
  isBreak?: boolean;
  defaultTitle?: string;
  activity: string;
  status: 'completed' | 'in_progress' | 'pending' | 'blocked';
  durationHours: number;
}

export interface DailyHourlyReport {
  id: string;
  date: string; // YYYY-MM-DD
  slots: HourlySlot[];
  totalLoggedHours: number;
  targetHours: number; // 12
  updatedAt: string;
}
