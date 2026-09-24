import React from 'react';
import { 
  Square, 
  RotateCcw, 
  Play, 
  Search, 
  FileText, 
  Download 
} from 'lucide-react';
import { ThemeMode } from '../types';

interface OperationPanelProps {
  isRunning: boolean;
  onStart: () => void;
  onStop: () => void;
  onReset: () => void;
  onOpenHistory: () => void;
  onOpenLogs: () => void;
  onExportData: () => void;
  theme?: ThemeMode;
}

export const OperationPanel: React.FC<OperationPanelProps> = ({
  isRunning,
  onStart,
  onStop,
  onReset,
  onOpenHistory,
  onOpenLogs,
  onExportData,
  theme = 'dark',
}) => {
  const isDark = theme === 'dark';

  return (
    <div
      className={`rounded-lg border p-2 sm:p-2.5 lg:p-3 flex flex-col select-none flex-1 min-h-0 justify-between transition-colors ${
        isDark
          ? 'bg-[#181c24] border-[#2c3342] shadow-md shadow-black/20'
          : 'bg-white border-sky-200 shadow-xs'
      }`}
    >
      {/* Title */}
      <div
        className={`flex items-center gap-2 mb-1.5 pb-1 border-b shrink-0 ${
          isDark ? 'border-[#262c38]' : 'border-slate-100'
        }`}
      >
        <div className="w-1.5 h-3.5 sm:h-4 bg-[#22c55e] rounded-full shadow-[0_0_6px_#22c55e]" />
        <h2
          className={`text-xs sm:text-sm lg:text-base font-bold tracking-tight ${
            isDark ? 'text-white' : 'text-slate-800'
          }`}
        >
          人员操作
        </h2>
      </div>

      {/* 2x3 Button Matrix - flexible height that adapts cleanly */}
      <div className="grid grid-cols-2 gap-1.5 sm:gap-2 flex-1 min-h-0">
        {/* Row 1 - Left: 设备停止 (Red) */}
        <button
          id="btn-op-stop"
          onClick={onStop}
          className="min-h-[38px] h-full rounded-md bg-[#d32f2f] hover:bg-[#b71c1c] active:scale-[0.98] text-white font-bold text-xs sm:text-sm lg:text-base shadow-sm flex items-center justify-center gap-1.5 sm:gap-2 transition-all cursor-pointer border border-red-400/30"
        >
          <Square className="w-3.5 h-3.5 sm:w-4 sm:h-4 fill-white" />
          <span>设备停止</span>
        </button>

        {/* Row 1 - Right: 设备复位 (Slate) */}
        <button
          id="btn-op-reset"
          onClick={onReset}
          className="min-h-[38px] h-full rounded-md bg-[#475569] hover:bg-[#334155] active:scale-[0.98] text-white font-bold text-xs sm:text-sm lg:text-base shadow-sm flex items-center justify-center gap-1.5 sm:gap-2 transition-all cursor-pointer border border-slate-500/30"
        >
          <RotateCcw className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          <span>设备复位</span>
        </button>

        {/* Row 2 - Left: 设备启动 (Green) */}
        <button
          id="btn-op-start"
          onClick={onStart}
          className={`min-h-[38px] h-full rounded-md text-white font-bold text-xs sm:text-sm lg:text-base shadow-sm flex items-center justify-center gap-1.5 sm:gap-2 transition-all cursor-pointer active:scale-[0.98] border border-emerald-400/30 ${
            isRunning
              ? 'bg-[#15803d] ring-2 ring-emerald-400 ring-offset-1 ring-offset-[#181c24] animate-pulse'
              : 'bg-[#16a34a] hover:bg-[#15803d]'
          }`}
        >
          <Play className="w-3.5 h-3.5 sm:w-4 sm:h-4 fill-white" />
          <span>{isRunning ? '正在测阻...' : '设备启动'}</span>
        </button>

        {/* Row 2 - Right: 履历查询 (Blue) */}
        <button
          id="btn-op-history"
          onClick={onOpenHistory}
          className="min-h-[38px] h-full rounded-md bg-[#0078d4] hover:bg-[#0063b1] active:scale-[0.98] text-white font-bold text-xs sm:text-sm lg:text-base shadow-sm flex items-center justify-center gap-1.5 sm:gap-2 transition-all cursor-pointer border border-blue-400/30"
        >
          <Search className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          <span>履历查询</span>
        </button>

        {/* Row 3 - Left: 日志查询 (Orange) */}
        <button
          id="btn-op-logs"
          onClick={onOpenLogs}
          className="min-h-[38px] h-full rounded-md bg-[#ea580c] hover:bg-[#c2410c] active:scale-[0.98] text-white font-bold text-xs sm:text-sm lg:text-base shadow-sm flex items-center justify-center gap-1.5 sm:gap-2 transition-all cursor-pointer border border-orange-400/30"
        >
          <FileText className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          <span>日志查询</span>
        </button>

        {/* Row 3 - Right: 导出数据 (Purple) */}
        <button
          id="btn-op-export"
          onClick={onExportData}
          className="min-h-[38px] h-full rounded-md bg-[#8e24aa] hover:bg-[#7b1fa2] active:scale-[0.98] text-white font-bold text-xs sm:text-sm lg:text-base shadow-sm flex items-center justify-center gap-1.5 sm:gap-2 transition-all cursor-pointer border border-purple-400/30"
        >
          <Download className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          <span>导出数据</span>
        </button>
      </div>
    </div>
  );
};
