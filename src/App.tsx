import React, { useState } from 'react';
import { TopNavbar } from './components/TopNavbar';
import { HourlyStatusReport } from './components/HourlyStatusReport';
import { AIChatHelp } from './components/AIChatHelp';
import { useHourlyReport } from './hooks/useAttendance';
import { Sparkles, PenTool } from 'lucide-react';

export function App() {
  const [selectedDate, setSelectedDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const [isAIChatOpen, setIsAIChatOpen] = useState(false);

  const { report, updateSlot } = useHourlyReport(selectedDate);

  const handleWriteDown = (slotId: string, activityText: string) => {
    updateSlot(slotId, {
      activity: activityText,
      status: 'completed',
    });
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-sans relative">
      <TopNavbar onOpenAIChat={() => setIsAIChatOpen(true)} />

      <main className="flex-1 max-w-6xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8 space-y-6">
        <HourlyStatusReport
          onOpenAIChat={() => setIsAIChatOpen(true)}
          selectedDate={selectedDate}
          onDateChange={setSelectedDate}
        />
      </main>

      <footer className="border-t border-slate-200 bg-white py-6">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <div>
            <span className="font-semibold text-slate-800">Hourly Status Report</span>
            <span aria-hidden="true" className="mx-2">·</span>
            <span>12-Hour Daily Log (9:00 AM – 9:00 PM)</span>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsAIChatOpen(true)}
              className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center gap-1 cursor-pointer"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>AI Chat: Write It Down</span>
            </button>
            <span aria-hidden="true" className="text-slate-300">·</span>
            <span className="font-mono text-slate-400">
              {report.totalLoggedHours}/12 Hours Tracked
            </span>
          </div>
        </div>
      </footer>

      {/* Floating Action Button for AI Chat: Write It Down */}
      {!isAIChatOpen && (
        <button
          onClick={() => setIsAIChatOpen(true)}
          className="fixed bottom-6 right-6 z-40 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-3 rounded-full shadow-lg hover:shadow-xl transition-all flex items-center gap-2 font-medium text-xs cursor-pointer group"
          title="Open AI Chat: Write It Down"
        >
          <PenTool className="w-4 h-4 text-amber-300 group-hover:scale-110 transition-transform" />
          <span className="font-semibold">Write It Down</span>
        </button>
      )}

      {/* AI Chat Help Window with Write Down Action */}
      <AIChatHelp
        isOpen={isAIChatOpen}
        onClose={() => setIsAIChatOpen(false)}
        currentReport={report}
        onWriteDown={handleWriteDown}
      />
    </div>
  );
}

export default App;
