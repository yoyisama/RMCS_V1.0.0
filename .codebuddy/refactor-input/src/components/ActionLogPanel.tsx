import React, { useEffect, useRef } from 'react';
import { LogEntry, ThemeMode } from '../types';

interface ActionLogPanelProps {
  logs: LogEntry[];
  onOpenLogModal: () => void;
  theme?: ThemeMode;
}

export const ActionLogPanel: React.FC<ActionLogPanelProps> = ({
  logs,
  onOpenLogModal,
  theme = 'dark',
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const isDark = theme === 'dark';

  useEffect(() => {
    if (containerRef.current) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight;
    }
  }, [logs]);

  return (
    <div
      className={`rounded-lg border p-2 sm:p-2.5 lg:p-3 flex flex-col h-full select-none overflow-hidden transition-colors ${
        isDark
          ? 'bg-[#181c24] border-[#2c3342] shadow-md shadow-black/20'
          : 'bg-white border-sky-200 shadow-xs'
      }`}
    >
      {/* Title Header */}
      <div
        className={`flex items-center justify-between mb-1.5 pb-1 border-b shrink-0 ${
          isDark ? 'border-[#262c38]' : 'border-slate-100'
        }`}
      >
        <div className="flex items-center gap-2">
          <div className="w-1.5 h-3.5 sm:h-4 bg-[#22c55e] rounded-full shadow-[0_0_6px_#22c55e]" />
          <h2
            className={`text-xs sm:text-sm lg:text-base font-bold tracking-tight ${
              isDark ? 'text-white' : 'text-slate-800'
            }`}
          >
            动作履历
          </h2>
        </div>
        <button
          onClick={onOpenLogModal}
          className={`text-[11px] sm:text-xs cursor-pointer font-medium hover:underline ${
            isDark ? 'text-[#38bdf8]' : 'text-[#1976d2]'
          }`}
        >
          查看完整日志 ({logs.length}条)
        </button>
      </div>

      {/* Log Content Area */}
      <div
        ref={containerRef}
        className={`flex-1 min-h-0 rounded p-2 overflow-y-auto font-mono text-[11px] sm:text-xs leading-relaxed select-text border ${
          isDark
            ? 'bg-[#0f1217] border-[#242b38] text-slate-300'
            : 'bg-white border-slate-200 text-slate-700'
        }`}
      >
        {logs.map((log) => (
          <div key={log.id} className="py-0.5 flex items-start gap-1.5">
            <span className="text-slate-500 shrink-0 select-none">
              [{log.time}]
            </span>
            <span
              className={
                log.type === 'error'
                  ? isDark
                    ? 'text-rose-400 font-semibold'
                    : 'text-rose-600 font-semibold'
                  : log.type === 'warning'
                  ? isDark
                    ? 'text-amber-300'
                    : 'text-amber-600'
                  : log.type === 'success'
                  ? isDark
                    ? 'text-emerald-400'
                    : 'text-emerald-700'
                  : isDark
                  ? 'text-slate-300'
                  : 'text-slate-800'
              }
            >
              {log.message}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};
