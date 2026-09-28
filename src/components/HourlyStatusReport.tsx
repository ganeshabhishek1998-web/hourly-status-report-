import React, { useState, useEffect, useMemo, useRef } from 'react';
import { useHourlyReport } from '../hooks/useAttendance';
import { HourlySlot } from '../types/attendance';
import {
  Clock,
  Calendar,
  CheckCircle2,
  Copy,
  Download,
  Upload,
  RotateCcw,
  Sparkles,
  ChevronLeft,
  ChevronRight,
  Check,
} from 'lucide-react';

interface HourlyStatusReportProps {
  onOpenAIChat?: () => void;
  selectedDate?: string;
  onDateChange?: (date: string) => void;
}

export const HourlyStatusReport: React.FC<HourlyStatusReportProps> = ({
  onOpenAIChat,
  selectedDate: propDate,
  onDateChange,
}) => {
  const [internalDate, setInternalDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const selectedDate = propDate || internalDate;
  const setSelectedDate = (d: string) => {
    setInternalDate(d);
    if (onDateChange) onDateChange(d);
  };
  const [copied, setCopied] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [currentTime, setCurrentTime] = useState<string>(() => new Date().toLocaleTimeString());

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date().toLocaleTimeString());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const { report, updateSlot, importReport, resetReport } = useHourlyReport(selectedDate);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);

  // Determine current active hour slot based on real-time clock
  const currentSlotId = useMemo(() => {
    const now = new Date();
    const curH = now.getHours();
    const curM = now.getMinutes();

    const todayStr = new Date().toISOString().split('T')[0];
    if (selectedDate !== todayStr) return null;

    const matched = report.slots.find((s) => {
      const startTotal = s.startHour * 60 + s.startMinute;
      const endTotal = s.endHour * 60 + s.endMinute;
      const nowTotal = curH * 60 + curM;
      return nowTotal >= startTotal && nowTotal < endTotal;
    });

    return matched ? matched.id : null;
  }, [report.slots, selectedDate]);

  const loggedHours = report.totalLoggedHours;

  const showToast = (text: string) => {
    setToastMessage(text);
    setTimeout(() => setToastMessage(null), 3000);
  };

  const handleActivityChange = (slotId: string, activityText: string) => {
    updateSlot(slotId, { activity: activityText });
  };

  const handleStatusToggle = (slot: HourlySlot) => {
    if (slot.isBreak) return;
    const nextStatus =
      slot.status === 'completed'
        ? 'in_progress'
        : slot.status === 'in_progress'
        ? 'blocked'
        : 'completed';

    updateSlot(slot.id, { status: nextStatus });
  };

  const handleReset = () => {
    if (window.confirm('Reset all activities for this report?')) {
      resetReport();
      showToast('Hourly report reset to empty.');
    }
  };

  const handleCopyText = () => {
    const lines = [
      `Hourly Status Report (${loggedHours}/12 hours) - ${selectedDate}`,
      `--------------------------------------------------`,
      `Time                    Activity`,
      `--------------------------------------------------`,
    ];

    report.slots.forEach((s) => {
      const padTime = s.timeRange.padEnd(23, ' ');
      const act = s.activity.trim() || '(Pending)';
      lines.push(`${padTime} ${act}`);
    });

    lines.push(`--------------------------------------------------`);
    lines.push(`Total Logged Hours: ${loggedHours} / 12 hours`);

    navigator.clipboard.writeText(lines.join('\n')).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
      showToast('Report copied to clipboard.');
    });
  };

  const handleExportCSV = () => {
    const headers = ['Time', 'Activity', 'Status', 'Duration (Hours)', 'Date'];
    const rows = report.slots.map((s) => [
      `"${s.timeRange}"`,
      `"${s.activity.replace(/"/g, '""')}"`,
      s.status,
      s.durationHours,
      selectedDate,
    ]);

    const csv = [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `hourly_status_report_${selectedDate}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const processReportText = (text: string, fileName: string) => {
    if (!text || typeof text !== 'string') {
      showToast('Uploaded file is empty.');
      return;
    }

    let parsedSlots: Array<{ timeRange?: string; slotId?: string; activity: string; status?: string }> = [];

    // 1. Try parsing as JSON
    if (fileName.endsWith('.json') || text.trim().startsWith('{') || text.trim().startsWith('[')) {
      try {
        const data = JSON.parse(text);
        const list = Array.isArray(data) ? data : data.slots && Array.isArray(data.slots) ? data.slots : null;
        if (list) {
          parsedSlots = list.map((item: any) => ({
            timeRange: item.timeRange || item.time,
            slotId: item.slotId || item.id,
            activity: item.activity || item.task || '',
            status: item.status || 'completed',
          }));
        }
      } catch {
        // Fall through to CSV/plain text parsing
      }
    }

    // 2. If not JSON, try parsing as CSV or standard plain text
    if (parsedSlots.length === 0) {
      const rawLines = text.split(/\r?\n/).map((l) => l.trim()).filter(Boolean);
      const isCSV = rawLines.some((l) => l.includes(',') && !l.startsWith('---'));

      if (isCSV) {
        const parseCSVRow = (line: string) => {
          const res: string[] = [];
          let cur = '';
          let inQuotes = false;
          for (let i = 0; i < line.length; i++) {
            const char = line[i];
            if (char === '"') {
              if (inQuotes && line[i + 1] === '"') {
                cur += '"';
                i++;
              } else {
                inQuotes = !inQuotes;
              }
            } else if (char === ',' && !inQuotes) {
              res.push(cur.trim());
              cur = '';
            } else {
              cur += char;
            }
          }
          res.push(cur.trim());
          return res;
        };

        const rows = rawLines.map(parseCSVRow);
        const headerRow = rows[0] || [];
        const timeCol = headerRow.findIndex((h) => /time/i.test(h));
        const actCol = headerRow.findIndex((h) => /activity|task|description|log/i.test(h));
        const statusCol = headerRow.findIndex((h) => /status/i.test(h));

        const dataRows = timeCol !== -1 || actCol !== -1 ? rows.slice(1) : rows;

        parsedSlots = dataRows
          .map((cols) => {
            const timeRange = (timeCol !== -1 ? cols[timeCol] : cols[0]) || '';
            const activity = (actCol !== -1 ? cols[actCol] : cols[1]) || '';
            const status = (statusCol !== -1 ? cols[statusCol] : cols[2]) || 'completed';
            return { timeRange, activity, status };
          })
          .filter((s) => s.activity || s.timeRange);
      } else {
        // Human-readable / copied report lines
        for (const line of rawLines) {
          if (
            line.startsWith('Hourly Status Report') ||
            line.startsWith('---') ||
            line.startsWith('Time ') ||
            line.startsWith('Total Logged')
          ) {
            continue;
          }
          const match = line.match(
            /^(\d{1,2}:\d{2}\s*(?:AM|PM)?\s*[\u2013\u2014-]\s*\d{1,2}:\d{2}\s*(?:AM|PM))\s+(.*)$/i
          );
          if (match) {
            parsedSlots.push({
              timeRange: match[1].trim(),
              activity: match[2].trim(),
              status: 'completed',
            });
          } else if (line.trim()) {
            parsedSlots.push({
              activity: line.trim(),
              status: 'completed',
            });
          }
        }
      }
    }

    if (parsedSlots.length === 0) {
      showToast('Could not find hourly status activities in the uploaded file.');
      return;
    }

    importReport(parsedSlots);
    const count = parsedSlots.filter((p) => p.activity.trim()).length;
    showToast(`Hourly status report uploaded successfully (${count} activities updated).`);
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      processReportText(text, file.name);
      if (e.target) e.target.value = '';
    };
    reader.readAsText(file);
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const text = event.target?.result as string;
      processReportText(text, file.name);
    };
    reader.readAsText(file);
  };

  const handlePrevDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() - 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  const handleNextDay = () => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + 1);
    setSelectedDate(d.toISOString().split('T')[0]);
  };

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="p-3 bg-slate-900 text-white text-xs rounded-lg flex items-center justify-between shadow-sm animate-fade-in">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <span>{toastMessage}</span>
          </div>
          <button
            onClick={() => setToastMessage(null)}
            className="text-slate-400 hover:text-white cursor-pointer"
          >
            ✕
          </button>
        </div>
      )}

      {/* Main Header Card: Hourly Status Report (0/12 hours) */}
      <div className="bg-white border border-slate-200 rounded-xl p-6 sm:p-8 space-y-5">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">
              <Clock className="w-4 h-4 text-slate-400" />
              <span>Daily Work Activity Log</span>
              <span aria-hidden="true">·</span>
              <span>12-Hour Schedule Window</span>
            </div>

            {/* Exact Header: Hourly Status Report (0/12 hours) */}
            <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
              Hourly Status Report{' '}
              <span className="font-mono text-indigo-600 font-semibold tabular-nums">
                ({loggedHours}/12 hours)
              </span>
            </h1>

            <div className="text-xs text-slate-500 mt-1">
              <span>9:00 AM – 9:00 PM operational schedule</span>
            </div>
          </div>

          {/* Action Toolbar */}
          <div className="flex flex-wrap items-center gap-2">
            {onOpenAIChat && (
              <button
                onClick={onOpenAIChat}
                className="px-3 py-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors flex items-center gap-1.5 cursor-pointer shadow-xs"
                title="AI Chat: Write it down"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>AI Chat Help</span>
              </button>
            )}

            <button
              onClick={handleCopyText}
              className="px-3 py-1.5 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors flex items-center gap-1.5 cursor-pointer"
              title="Copy formatted text for standup"
            >
              {copied ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-600" />
                  <span className="text-emerald-700 font-semibold">Copied!</span>
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5 text-slate-500" />
                  <span>Copy Report</span>
                </>
              )}
            </button>

            <button
              onClick={handleExportCSV}
              className="px-3 py-1.5 text-xs font-medium text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <Download className="w-3.5 h-3.5 text-slate-500" />
              <span>Export CSV</span>
            </button>

            {/* Hidden file input for uploading report */}
            <input
              ref={fileInputRef}
              type="file"
              accept=".csv,.json,.txt"
              onChange={handleFileUpload}
              className="hidden"
            />

            <button
              onClick={() => fileInputRef.current?.click()}
              className="px-3 py-1.5 text-xs font-semibold text-indigo-700 bg-indigo-50 border border-indigo-200 rounded-lg hover:bg-indigo-100 transition-colors flex items-center gap-1.5 cursor-pointer shadow-2xs"
              title="Upload / Import Hourly Status Report (CSV, JSON, or TXT)"
            >
              <Upload className="w-3.5 h-3.5 text-indigo-600" />
              <span>Upload Report</span>
            </button>

            <button
              onClick={handleReset}
              className="p-1.5 text-slate-400 hover:text-slate-700 rounded-lg border border-transparent hover:border-slate-200 transition-colors cursor-pointer"
              title="Reset hourly activities"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Live Current Time Show & Shift Info */}
        <div className="flex flex-wrap items-center justify-between bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-xs gap-3">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-indigo-50 border border-indigo-200 text-indigo-700 flex items-center justify-center font-bold">
              <Clock className="w-4 h-4 text-indigo-600" />
            </div>
            <div>
              <span className="text-[10px] uppercase font-semibold text-slate-500 block tracking-wider">
                Current Time
              </span>
              <span className="font-mono text-slate-900 font-bold text-sm tracking-tight tabular-nums">
                {currentTime}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3 text-slate-600 font-mono text-xs">
            <div className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
              <span className="text-slate-700 font-medium">Shift Time:</span>
              <span className="text-slate-900 font-semibold">9:00 AM – 9:00 PM</span>
            </div>
            <span aria-hidden="true" className="text-slate-300">·</span>
            <div className="text-slate-500">
              12 Hours Workday Schedule
            </div>
          </div>
        </div>

        {/* Date Selector Sub-bar */}
        <div className="pt-2 flex items-center justify-between border-t border-slate-100">
          <div className="flex items-center gap-2">
            <button
              onClick={handlePrevDay}
              className="p-1 text-slate-500 hover:text-slate-900 rounded border border-slate-200 hover:bg-slate-50 cursor-pointer"
              title="Previous Day"
            >
              <ChevronLeft className="w-3.5 h-3.5" />
            </button>

            <div className="flex items-center gap-1.5 bg-slate-50 border border-slate-300 rounded-lg px-2.5 py-1">
              <Calendar className="w-3.5 h-3.5 text-slate-500" />
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                className="bg-transparent text-xs font-mono font-medium text-slate-900 focus:outline-none cursor-pointer"
              />
            </div>

            <button
              onClick={handleNextDay}
              className="p-1 text-slate-500 hover:text-slate-900 rounded border border-slate-200 hover:bg-slate-50 cursor-pointer"
              title="Next Day"
            >
              <ChevronRight className="w-3.5 h-3.5" />
            </button>

            <button
              onClick={() => setSelectedDate(new Date().toISOString().split('T')[0])}
              className="text-[11px] font-medium text-slate-600 hover:text-slate-900 hover:underline px-1 cursor-pointer"
            >
              Today
            </button>
          </div>

          <div className="text-xs font-mono text-slate-500 tabular-nums">
            {report.slots.length} Hourly Slots
          </div>
        </div>
      </div>

      {/* Hourly Status Table: Time and Activity */}
      <div
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={`relative bg-white border rounded-xl overflow-hidden shadow-xs transition-all ${
          isDragging ? 'border-indigo-500 ring-2 ring-indigo-200' : 'border-slate-200'
        }`}
      >
        {isDragging && (
          <div className="absolute inset-0 z-30 bg-indigo-50/95 backdrop-blur-xs flex flex-col items-center justify-center p-6 text-center animate-fade-in pointer-events-none">
            <Upload className="w-10 h-10 text-indigo-600 animate-bounce mb-2" />
            <p className="text-sm font-bold text-indigo-900">Drop your report file to upload</p>
            <p className="text-xs text-indigo-600 mt-1">Accepts CSV, JSON, or text report files</p>
          </div>
        )}

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 font-semibold uppercase tracking-wider">
              <tr>
                <th className="py-3.5 px-4 w-48 sm:w-60">Time</th>
                <th className="py-3.5 px-4">Activity</th>
                <th className="py-3.5 px-4 w-28 text-center">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {report.slots.map((slot) => {
                const isCurrent = currentSlotId === slot.id;
                const isBreak = slot.isBreak;
                const hasContent = slot.activity.trim().length > 0;

                return (
                  <tr
                    key={slot.id}
                    className={`transition-colors ${
                      isCurrent
                        ? 'bg-indigo-50/60 hover:bg-indigo-50/80'
                        : isBreak
                        ? 'bg-amber-50/30 hover:bg-amber-50/50'
                        : 'hover:bg-slate-50/80'
                    }`}
                  >
                    {/* Time Column */}
                    <td className="py-3.5 px-4 font-mono font-medium text-slate-900 align-top">
                      <div className="flex items-center gap-2">
                        <span className="tabular-nums whitespace-nowrap text-slate-900 font-semibold">
                          {slot.timeRange}
                        </span>
                        {isCurrent && (
                          <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold uppercase tracking-wider bg-indigo-100 text-indigo-700 animate-pulse">
                            Now
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-slate-400 mt-0.5 font-sans">
                        {slot.durationHours === 0.5 ? '0.5 hr' : '1.0 hr'}
                      </div>
                    </td>

                    {/* Activity Column */}
                    <td className="py-2.5 px-4 align-middle">
                      {isBreak ? (
                        <div className="flex items-center gap-2 text-slate-600 font-medium py-1">
                          <span className="text-amber-800 font-semibold">Lunch break</span>
                          <span className="text-xs text-slate-400">·</span>
                          <span className="text-xs text-slate-500">
                            Scheduled meal & recharge interval
                          </span>
                        </div>
                      ) : (
                        <div className="relative flex items-center gap-1.5">
                          <input
                            type="text"
                            value={slot.activity}
                            onChange={(e) => handleActivityChange(slot.id, e.target.value)}
                            placeholder="Enter hourly activity / task / milestone..."
                            className={`w-full px-3 py-2 text-xs rounded-lg border transition-all ${
                              hasContent
                                ? 'bg-white border-slate-300 text-slate-900 focus:border-slate-900'
                                : 'bg-slate-50/80 border-slate-200 text-slate-600 placeholder-slate-400 focus:bg-white focus:border-slate-900'
                            } focus:outline-none focus:ring-1 focus:ring-slate-900`}
                          />
                        </div>
                      )}
                    </td>

                    {/* Status Column */}
                    <td className="py-2.5 px-4 text-center align-middle">
                      {isBreak ? (
                        <span className="text-xs font-semibold text-amber-700">
                          Break
                        </span>
                      ) : (
                        <button
                          type="button"
                          onClick={() => handleStatusToggle(slot)}
                          className={`px-2.5 py-1 text-[11px] font-semibold rounded-lg transition-colors cursor-pointer capitalize border ${
                            slot.status === 'completed'
                              ? 'bg-emerald-50 text-emerald-800 border-emerald-200 hover:bg-emerald-100'
                              : slot.status === 'in_progress'
                              ? 'bg-indigo-50 text-indigo-800 border-indigo-200 hover:bg-indigo-100'
                              : slot.status === 'blocked'
                              ? 'bg-rose-50 text-rose-800 border-rose-200 hover:bg-rose-100'
                              : 'bg-slate-100 text-slate-600 border-slate-200 hover:bg-slate-200'
                          }`}
                          title="Click to toggle status"
                        >
                          {slot.status.replace('_', ' ')}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Summary Footer */}
      <div className="bg-slate-50 border border-slate-200 rounded-xl p-4 text-xs text-slate-600 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Clock className="w-4 h-4 text-slate-500 shrink-0" />
          <span>
            12-Hour Daily Work Schedule (9:00 AM – 9:00 PM). Use "Write It Down" or "Copy Report" to log and export.
          </span>
        </div>
        <span className="font-mono text-slate-500 tabular-nums shrink-0">
          Target: 12.0 hours
        </span>
      </div>
    </div>
  );
};
