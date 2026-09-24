import React from 'react';
import { RotateCcw } from 'lucide-react';
import { ProductionStatus, ThemeMode } from '../types';

interface StatusCardProps {
  status: ProductionStatus;
  onReset: () => void;
  theme?: ThemeMode;
}

export const StatusCard: React.FC<StatusCardProps> = ({
  status,
  onReset,
  theme = 'dark',
}) => {
  const isDark = theme === 'dark';

  return (
    <div
      className={`rounded-lg border p-2 sm:p-2.5 lg:p-3 flex flex-col select-none transition-colors shrink-0 ${
        isDark
          ? 'bg-[#181c24] border-[#2c3342] shadow-md shadow-black/20'
          : 'bg-white border-sky-200 shadow-xs'
      }`}
    >
      {/* Title */}
      <div
        className={`flex items-center gap-2 mb-1.5 pb-1 border-b ${
          isDark ? 'border-[#262c38]' : 'border-slate-100'
        }`}
      >
        <div className="w-1.5 h-3.5 sm:h-4 bg-[#22c55e] rounded-full shadow-[0_0_6px_#22c55e]" />
        <h2
          className={`text-xs sm:text-sm lg:text-base font-bold tracking-tight ${
            isDark ? 'text-white' : 'text-slate-800'
          }`}
        >
          运行状况
        </h2>
      </div>

      {/* Values */}
      <div className="space-y-1 sm:space-y-1.5 text-xs sm:text-sm py-1">
        <div className="flex justify-between items-center">
          <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
            完成数量:
          </span>
          <span
            className={`font-mono font-black text-sm sm:text-base ${
              isDark ? 'text-[#38bdf8]' : 'text-slate-900'
            }`}
          >
            {status.completedCount.toLocaleString()} 件
          </span>
        </div>

        <div className="flex justify-between items-center">
          <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
            设备运行状况:
          </span>
          <span
            className={`font-bold text-xs sm:text-sm px-2 py-0.5 rounded ${
              status.status === '运行中'
                ? isDark
                  ? 'text-emerald-400 bg-emerald-950/60 border border-emerald-600/50 shadow-[0_0_8px_rgba(16,185,129,0.2)]'
                  : 'text-[#2e7d32] bg-emerald-50 border border-emerald-200'
                : status.status === '待机'
                ? isDark
                  ? 'text-sky-400 bg-sky-950/60 border border-sky-600/50'
                  : 'text-[#1976d2] bg-sky-50 border border-sky-200'
                : status.status === '已停止'
                ? isDark
                  ? 'text-rose-400 bg-rose-950/60 border border-rose-600/50'
                  : 'text-[#d32f2f] bg-rose-50 border border-rose-200'
                : isDark
                ? 'text-amber-400 bg-amber-950/60 border border-amber-600/50'
                : 'text-amber-600 bg-amber-50 border border-amber-200'
            }`}
          >
            {status.status}
          </span>
        </div>

        <div className="flex justify-between items-center">
          <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
            运行时长:
          </span>
          <span
            className={`font-mono font-bold text-xs sm:text-sm ${
              isDark ? 'text-slate-200' : 'text-slate-900'
            }`}
          >
            {status.runDurationSeconds} 秒
          </span>
        </div>
      </div>

      {/* Reset Button */}
      <button
        id="btn-reset-counter"
        onClick={onReset}
        className="mt-1.5 sm:mt-2 w-full py-1.5 px-3 bg-[#0078d4] hover:bg-[#0063b1] active:scale-[0.99] text-white font-bold text-xs sm:text-sm rounded shadow-sm transition-all flex items-center justify-center gap-1.5 cursor-pointer"
      >
        <RotateCcw className="w-3.5 h-3.5" />
        <span>产量清零</span>
      </button>
    </div>
  );
};
