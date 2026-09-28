import React, { useState, useRef, useEffect } from 'react';
import {
  Sparkles,
  Send,
  X,
  Bot,
  User,
  CheckCircle2,
  PenTool,
  RotateCcw,
  Check,
  ArrowRight,
} from 'lucide-react';
import { DailyHourlyReport, HourlySlot } from '../types/attendance';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
  writtenDown?: {
    slotId: string;
    slotTimeRange: string;
    activity: string;
  };
}

interface AIChatHelpProps {
  isOpen: boolean;
  onClose: () => void;
  currentReport: DailyHourlyReport;
  onWriteDown: (slotId: string, activityText: string) => void;
  currentSlotId?: string | null;
}

export const AIChatHelp: React.FC<AIChatHelpProps> = ({
  isOpen,
  onClose,
  currentReport,
  onWriteDown,
  currentSlotId,
}) => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'init_1',
      role: 'assistant',
      content:
        'Hello! I am your AI assistant. Tell me what you did or what you\'re doing, and say **"I\'m just saying, write it down."**—I will immediately record it into your 12-hour status report.',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 150);
    }
  }, [isOpen]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  const activeSlot = currentReport.slots.find((s) => s.id === currentSlotId) || currentReport.slots[0];

  const handleSendMessage = async (textToSend?: string) => {
    const text = (textToSend || inputValue).trim();
    if (!text || isLoading) return;

    const userMsg: ChatMessage = {
      id: `msg_${Date.now()}`,
      role: 'user',
      content: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputValue('');
    setIsLoading(true);

    try {
      const res = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: text,
          currentSlotId: activeSlot.id,
          currentReport,
        }),
      });

      const data = await res.json();
      const reply = data.reply || 'Processed.';

      let writtenDownInfo: ChatMessage['writtenDown'];
      if (data.action && data.action.type === 'write_down') {
        const slot = currentReport.slots.find((s) => s.id === data.action.slotId) || activeSlot;
        const activityText = data.action.activity || text;
        onWriteDown(slot.id, activityText);

        writtenDownInfo = {
          slotId: slot.id,
          slotTimeRange: slot.timeRange,
          activity: activityText,
        };
      }

      const botMsg: ChatMessage = {
        id: `msg_${Date.now() + 1}`,
        role: 'assistant',
        content: reply,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        writtenDown: writtenDownInfo,
      };

      setMessages((prev) => [...prev, botMsg]);
    } catch (e) {
      // Fallback: write down directly on frontend
      const activityText = text.replace(/^(?:write (?:it )?down:?|i'm just saying:?)\s*/i, '');
      onWriteDown(activeSlot.id, activityText);

      const botMsg: ChatMessage = {
        id: `msg_${Date.now() + 1}`,
        role: 'assistant',
        content: `Got it! I wrote down: "${activityText}" into your ${activeSlot.timeRange} slot.`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        writtenDown: {
          slotId: activeSlot.id,
          slotTimeRange: activeSlot.timeRange,
          activity: activityText,
        },
      };
      setMessages((prev) => [...prev, botMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickWriteDown = (phrase: string) => {
    handleSendMessage(`Write it down: ${phrase}`);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 sm:inset-auto sm:bottom-6 sm:right-6 sm:w-96 sm:max-h-[600px] h-full sm:h-[580px] z-50 flex flex-col bg-white sm:rounded-2xl sm:border border-slate-200 shadow-2xl overflow-hidden font-sans animate-fade-in">
      {/* Header */}
      <div className="bg-slate-900 text-white px-4 py-3.5 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-indigo-600 flex items-center justify-center text-white">
            <PenTool className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-xs font-semibold leading-tight text-white flex items-center gap-1.5">
              <span>AI Chat: Write It Down</span>
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
            </h3>
            <span className="text-[10px] text-slate-400 block">
              Active Slot: {activeSlot.timeRange}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-1">
          <button
            onClick={() =>
              setMessages([
                {
                  id: 'init_reset',
                  role: 'assistant',
                  content:
                    'Chat cleared. What should I write down into your 12-hour report?',
                  timestamp: new Date().toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                  }),
                },
              ])
            }
            className="p-1.5 text-slate-400 hover:text-white rounded transition-colors cursor-pointer"
            title="Clear Chat"
          >
            <RotateCcw className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded transition-colors cursor-pointer"
            title="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Quick "Write It Down" Suggestions */}
      <div className="bg-slate-50 border-b border-slate-200 px-3 py-2 overflow-x-auto flex gap-1.5 shrink-0 no-scrollbar">
        <button
          onClick={() => handleQuickWriteDown("I'm just saying, write it down.")}
          className="whitespace-nowrap px-2.5 py-1 text-[11px] font-semibold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 border border-indigo-200 rounded-md transition-all shrink-0 cursor-pointer flex items-center gap-1"
        >
          <PenTool className="w-3 h-3 text-indigo-600" />
          <span>Write down: "I'm just saying, write it down."</span>
        </button>
        <button
          onClick={() => handleQuickWriteDown("It's a machine")}
          className="whitespace-nowrap px-2.5 py-1 text-[11px] font-medium text-slate-700 bg-white hover:bg-slate-100 border border-slate-200 rounded-md transition-all shrink-0 cursor-pointer flex items-center gap-1"
        >
          <PenTool className="w-3 h-3 text-slate-500" />
          <span>Write down: "It's a machine"</span>
        </button>
        <button
          onClick={() => handleQuickWriteDown('Morning standup & sprint ticket triage')}
          className="whitespace-nowrap px-2.5 py-1 text-[11px] font-medium text-slate-700 bg-white hover:bg-slate-100 border border-slate-200 rounded-md transition-all shrink-0 cursor-pointer"
        >
          Morning standup
        </button>
      </div>

      {/* Message Feed */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3.5 text-xs">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex gap-2.5 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
          >
            {msg.role === 'assistant' && (
              <div className="w-6 h-6 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center shrink-0 mt-0.5">
                <Bot className="w-3.5 h-3.5" />
              </div>
            )}

            <div
              className={`max-w-[85%] rounded-xl px-3.5 py-2.5 space-y-2 ${
                msg.role === 'user'
                  ? 'bg-slate-900 text-white rounded-br-none'
                  : 'bg-slate-100 text-slate-900 border border-slate-200 rounded-bl-none'
              }`}
            >
              <div className="whitespace-pre-wrap leading-relaxed break-words font-normal">
                {msg.content}
              </div>

              {/* Card showing that item was written down to report */}
              {msg.writtenDown && (
                <div className="bg-emerald-50 border border-emerald-200 rounded-lg p-2 text-emerald-900 text-[11px] flex items-start gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                  <div className="min-w-0">
                    <span className="font-semibold block">Written down to report:</span>
                    <span className="text-slate-600 block font-mono text-[10px]">
                      {msg.writtenDown.slotTimeRange}
                    </span>
                    <span className="font-medium text-emerald-800 italic block truncate">
                      "{msg.writtenDown.activity}"
                    </span>
                  </div>
                </div>
              )}

              <span
                className={`text-[9px] block text-right font-mono ${
                  msg.role === 'user' ? 'text-slate-400' : 'text-slate-400'
                }`}
              >
                {msg.timestamp}
              </span>
            </div>

            {msg.role === 'user' && (
              <div className="w-6 h-6 rounded-full bg-slate-900 text-white flex items-center justify-center shrink-0 mt-0.5">
                <User className="w-3.5 h-3.5" />
              </div>
            )}
          </div>
        ))}

        {isLoading && (
          <div className="flex gap-2.5 justify-start items-center">
            <div className="w-6 h-6 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center shrink-0">
              <Bot className="w-3.5 h-3.5 animate-spin" />
            </div>
            <div className="bg-slate-100 text-slate-600 rounded-xl px-3.5 py-2 text-xs flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-bounce" />
              <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-bounce delay-150" />
              <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-bounce delay-300" />
              <span className="ml-1 text-[11px]">Writing it down...</span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Form with direct "Write It Down" button */}
      <div className="p-3 bg-white border-t border-slate-200 shrink-0">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSendMessage();
          }}
          className="flex items-center gap-2 bg-slate-50 border border-slate-300 rounded-xl p-1.5 focus-within:ring-1 focus-within:ring-slate-900 focus-within:border-slate-900"
        >
          <input
            ref={inputRef}
            type="text"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={'Say anything (e.g. "I\'m just saying, write it down")...'}
            className="w-full bg-transparent px-2 py-1 text-xs text-slate-900 placeholder-slate-400 focus:outline-none"
          />
          <button
            type="submit"
            disabled={!inputValue.trim() || isLoading}
            className="px-3 py-1.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 text-white font-semibold text-xs transition-colors shrink-0 flex items-center gap-1 cursor-pointer"
          >
            <PenTool className="w-3.5 h-3.5" />
            <span>Write Down</span>
          </button>
        </form>
        <div className="flex items-center justify-between text-[10px] text-slate-400 mt-1.5 px-1 font-mono">
          <span>Target Slot: {activeSlot.timeRange}</span>
          <span>12-Hour Schedule</span>
        </div>
      </div>
    </div>
  );
};
