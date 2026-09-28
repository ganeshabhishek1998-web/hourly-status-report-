import React from 'react';
import { Clock, PenTool } from 'lucide-react';

interface TopNavbarProps {
  onOpenAIChat?: () => void;
}

export const TopNavbar: React.FC<TopNavbarProps> = ({ onOpenAIChat }) => {
  return (
    <header className="sticky top-0 z-40 bg-white border-b border-slate-200">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Zone 1: Wordmark */}
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-slate-900 flex items-center justify-center text-white font-mono font-bold text-sm">
              <Clock className="w-4 h-4" />
            </div>
            <span className="text-lg font-bold tracking-tight text-slate-900">
              Hourly Status Report
            </span>
          </div>

          {/* Zone 2 & 3: Clean timing indicator + Action Button */}
          <div className="flex items-center gap-3">
            <div className="hidden sm:flex items-center gap-2 text-xs font-mono text-slate-600 mr-1">
              <span>12 Hours Workday</span>
              <span aria-hidden="true" className="text-slate-300">·</span>
              <span className="text-slate-900 font-medium">9:00 AM – 9:00 PM</span>
            </div>

            {onOpenAIChat && (
              <button
                onClick={onOpenAIChat}
                className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 rounded-lg transition-colors cursor-pointer"
                title="Open AI Chat: Write It Down"
              >
                <PenTool className="w-3.5 h-3.5 text-indigo-600" />
                <span>Write It Down</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
