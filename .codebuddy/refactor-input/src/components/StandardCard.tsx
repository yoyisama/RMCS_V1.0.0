import React from 'react';
import { Sliders } from 'lucide-react';
import { TestStandard, ThemeMode } from '../types';

interface StandardCardProps {
  standard: TestStandard;
  onOpenConfig: () => void;
  theme?: ThemeMode;
}

export const StandardCard: React.FC<StandardCardProps> = ({
  standard,
  onOpenConfig,
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
          测定标准
        </h2>
      </div>

      {/* Values */}
      <div className="space-y-1 sm:space-y-1.5 text-xs sm:text-sm py-1">
        <div className="flex justify-between items-center">
          <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
            标准值:
          </span>
          <span
            className={`font-mono font-bold text-xs sm:text-sm lg:text-base ${
              isDark ? 'text-white' : 'text-slate-900'
            }`}
          >
            {standard.standardValue.toFixed(3)} Ω
          </span>
        </div>

        <div className="flex justify-between items-center">
          <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
            上偏差:
          </span>
          <span
            className={`font-mono font-bold text-xs sm:text-sm ${
              isDark ? 'text-amber-400' : 'text-amber-600'
            }`}
          >
            +{standard.upperDev.toFixed(2)} Ω
          </span>
        </div>

        <div className="flex justify-between items-center">
          <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
            下偏差:
          </span>
          <span
            className={`font-mono font-bold text-xs sm:text-sm ${
              isDark ? 'text-amber-400' : 'text-amber-600'
            }`}
          >
            -{standard.lowerDev.toFixed(2)} Ω
          </span>
        </div>
      </div>

      {/* Blue Action Button */}
      <button
        id="btn-param-config"
        onClick={onOpenConfig}
        className="mt-1.5 sm:mt-2 w-full py-1.5 px-3 bg-[#0078d4] hover:bg-[#0063b1] active:scale-[0.99] text-white font-bold text-xs sm:text-sm rounded shadow-sm transition-all flex items-center justify-center gap-1.5 cursor-pointer"
      >
        <Sliders className="w-3.5 h-3.5" />
        <span>参数设定</span>
      </button>
    </div>
  );
};
