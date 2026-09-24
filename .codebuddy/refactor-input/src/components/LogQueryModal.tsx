import React from 'react';
import { X, Minus, Square, Info, FileText } from 'lucide-react';
import { LogEntry, ThemeMode } from '../types';

interface LogQueryModalProps {
  isOpen: boolean;
  onClose: () => void;
  logs: LogEntry[];
  theme?: ThemeMode;
}

export const LogQueryModal: React.FC<LogQueryModalProps> = ({
  isOpen,
  onClose,
  logs,
  theme = 'dark',
}) => {
  if (!isOpen) return null;

  const isDark = theme === 'dark';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-xs p-3 sm:p-4 overflow-y-auto">
      {/* Window Frame mimicking image 2 */}
      <div
        className={`rounded-xl border w-full max-w-2xl overflow-hidden flex flex-col shadow-2xl my-auto max-h-[85vh] ${
          isDark
            ? 'bg-[#181c24] border-[#2e3748] text-[#e2e8f0]'
            : 'bg-[#f0f4f8] border-slate-300 text-slate-800'
        }`}
      >
        {/* Title Bar with classic OS controls */}
        <div
          className={`px-3.5 py-2 border-b flex items-center justify-between select-none shrink-0 ${
            isDark
              ? 'bg-[#151922] border-[#282f3d]'
              : 'bg-gradient-to-r from-slate-100 to-slate-200 border-slate-300'
          }`}
        >
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded-xs bg-[#0078d4] flex items-center justify-center text-[10px] text-white font-bold">
              <FileText className="w-2.5 h-2.5" />
            </div>
            <span
              className={`text-xs font-bold ${
                isDark ? 'text-white' : 'text-slate-800'
              }`}
            >
              日志查询
            </span>
          </div>

          <div className="flex items-center gap-1 text-slate-400">
            <button
              onClick={onClose}
              className={`w-5 h-5 flex items-center justify-center rounded cursor-pointer ${
                isDark ? 'hover:bg-[#252c3b]' : 'hover:bg-slate-300'
              }`}
            >
              <Minus className="w-3 h-3" />
            </button>
            <button
              onClick={onClose}
              className={`w-5 h-5 flex items-center justify-center rounded cursor-pointer ${
                isDark ? 'hover:bg-[#252c3b]' : 'hover:bg-slate-300'
              }`}
            >
              <Square className="w-2.5 h-2.5" />
            </button>
            <button
              onClick={onClose}
              className="w-5 h-5 flex items-center justify-center hover:bg-red-600 hover:text-white rounded cursor-pointer"
            >
              <X className="w-3 h-3" />
            </button>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-4 flex flex-col gap-3 flex-1 min-h-0 overflow-hidden">
          <div className="flex items-center justify-between shrink-0">
            <h3
              className={`text-sm font-semibold ${
                isDark ? 'text-white' : 'text-slate-800'
              }`}
            >
              动作履历记录 (共 {logs.length} 条)
            </h3>
            <div className="w-5 h-5 rounded-full bg-[#0078d4] text-white flex items-center justify-center font-serif font-bold text-xs shadow-xs">
              i
            </div>
          </div>

          {/* Log Window Display */}
          <div
            className={`border rounded p-3 overflow-y-auto flex-1 font-mono text-xs leading-relaxed select-text min-h-[220px] ${
              isDark
                ? 'bg-[#101318] border-[#252c38] text-slate-300'
                : 'bg-white border-slate-300 text-slate-700'
            }`}
          >
            {logs.length === 0 ? (
              <div className="text-slate-500 italic">暂无系统动作日志</div>
            ) : (
              logs.map((log) => (
                <div key={log.id} className="py-1 flex items-start gap-2">
                  <span className="text-slate-500 select-none">
                    [{log.time}]
                  </span>
                  <span
                    className={
                      log.type === 'error'
                        ? 'text-rose-400 font-semibold'
                        : log.type === 'warning'
                        ? 'text-amber-300'
                        : log.type === 'success'
                        ? 'text-emerald-400'
                        : isDark
                        ? 'text-slate-300'
                        : 'text-slate-800'
                    }
                  >
                    {log.message}
                  </span>
                </div>
              ))
            )}
          </div>

          {/* Dialog Action Buttons */}
          <div className="flex justify-end gap-2 pt-1 shrink-0">
            <button
              onClick={onClose}
              className="px-4 py-1.5 bg-[#0078d4] hover:bg-[#0063b1] text-white text-xs font-semibold rounded shadow-xs cursor-pointer transition-colors"
            >
              确定 (OK)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
