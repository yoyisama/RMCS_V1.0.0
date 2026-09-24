import React from 'react';
import { MatrixState, ThemeMode } from '../types';

interface WorkMatrixPanelProps {
  matrixState: MatrixState;
  activeCell: { row: number; col: 'L' | 'M' | 'R' } | null;
  onCellClick?: (row: number, col: 'L' | 'M' | 'R') => void;
  theme?: ThemeMode;
}

const ROW_LABELS = [
  '第一行',
  '第二行',
  '第三行',
  '第四行',
  '第五行',
  '第六行',
  '第七行',
];

const COLUMNS: Array<{ key: 'L' | 'M' | 'R'; label: string }> = [
  { key: 'L', label: '左区 (L)' },
  { key: 'M', label: '中区 (M)' },
  { key: 'R', label: '右区 (R)' },
];

export const WorkMatrixPanel: React.FC<WorkMatrixPanelProps> = ({
  matrixState,
  activeCell,
  onCellClick,
  theme = 'dark',
}) => {
  const isDark = theme === 'dark';

  return (
    <div
      className={`rounded-lg border p-2 sm:p-2.5 lg:p-3 flex flex-col h-full select-none transition-colors overflow-hidden ${
        isDark
          ? 'bg-[#181c24] border-[#2c3342] shadow-md shadow-black/20'
          : 'bg-white border-sky-200 shadow-xs'
      }`}
    >
      {/* Panel Header */}
      <div
        className={`flex items-center justify-between mb-1.5 sm:mb-2 pb-1 sm:pb-1.5 border-b ${
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
            执行作业
          </h2>
        </div>
        <span
          className={`text-[10px] sm:text-xs font-mono ${
            isDark ? 'text-slate-400' : 'text-slate-500'
          }`}
        >
          7行 × 3区阵列
        </span>
      </div>

      {/* Grid Container - auto adapting to height */}
      <div className="flex-1 min-h-0 flex flex-col justify-between overflow-hidden">
        {/* Table Header Row */}
        <div className="grid grid-cols-4 gap-1 sm:gap-1.5 mb-1 sm:mb-1.5 text-center shrink-0">
          {/* Top-left corner blank */}
          <div className="rounded border border-transparent" />

          {/* 3 Column Headers */}
          {COLUMNS.map((col) => (
            <div
              key={col.key}
              className={`py-1 sm:py-1.5 px-0.5 sm:px-1 font-bold text-[11px] sm:text-xs lg:text-sm rounded border transition-colors flex items-center justify-center ${
                isDark
                  ? 'bg-[#1e2638] border-[#313f5c] text-[#38bdf8] shadow-xs'
                  : 'bg-gradient-to-b from-[#e3f2fd] to-[#bbdefb] text-[#1565c0] border-[#90caf9] shadow-2xs'
              }`}
            >
              {col.label}
            </div>
          ))}
        </div>

        {/* 7 Data Rows - proportionally expand using flex-1 */}
        <div className="flex-1 min-h-0 flex flex-col gap-0.5 sm:gap-1 justify-between">
          {ROW_LABELS.map((rowName, index) => {
            const rowNum = index + 1;
            return (
              <div
                key={rowNum}
                className="grid grid-cols-4 gap-1 sm:gap-1.5 flex-1 min-h-[24px] sm:min-h-[28px] items-stretch"
              >
                {/* Row Header Label */}
                <div
                  className={`py-0.5 sm:py-1 px-1 sm:px-2 font-semibold text-[10px] sm:text-xs lg:text-sm rounded border flex items-center justify-center select-none ${
                    isDark
                      ? 'bg-[#13161c] text-slate-300 border-[#262c3a]'
                      : 'bg-slate-50 text-slate-600 border-slate-200'
                  }`}
                >
                  {rowName}
                </div>

                {/* 3 Data Cells */}
                {COLUMNS.map((col) => {
                  const key = `${rowNum}-${col.key}`;
                  const cell = matrixState[key];
                  const isCurrentActive =
                    activeCell?.row === rowNum && activeCell?.col === col.key;

                  const hasValue =
                    cell && cell.value !== null && cell.value !== undefined;
                  const isPass = cell?.result === true;
                  const isFail = cell?.result === false;

                  return (
                    <div
                      key={key}
                      onClick={() => onCellClick && onCellClick(rowNum, col.key)}
                      className={`py-0.5 sm:py-1 px-1 rounded border transition-all duration-150 flex items-center justify-center font-mono text-[11px] sm:text-xs lg:text-sm cursor-default relative overflow-hidden ${
                        isCurrentActive
                          ? isDark
                            ? 'bg-amber-950/60 border-amber-400 text-amber-300 ring-2 ring-amber-400/70 font-bold shadow-[0_0_12px_rgba(251,191,36,0.3)] animate-pulse'
                            : 'bg-amber-50 border-amber-400 text-amber-900 ring-2 ring-amber-300 ring-offset-1 animate-pulse'
                          : hasValue
                          ? isPass
                            ? isDark
                              ? 'bg-emerald-950/30 border-emerald-600/60 text-emerald-400 font-semibold shadow-[0_0_6px_rgba(16,185,129,0.1)]'
                              : 'bg-sky-50/60 border-sky-200 text-slate-800 font-semibold'
                            : isDark
                            ? 'bg-rose-950/40 border-rose-500/70 text-rose-400 font-bold shadow-[0_0_8px_rgba(244,67,54,0.2)]'
                            : 'bg-rose-50 border-rose-300 text-rose-700 font-bold'
                          : isDark
                          ? 'bg-[#12151b] border-[#222834] text-slate-600 hover:border-slate-700'
                          : 'bg-white border-slate-200 text-slate-300 hover:border-slate-300'
                      }`}
                      title={`行: ${rowName}, 位置: ${col.label}${
                        hasValue ? ` => 阻值: ${cell?.value}Ω` : ''
                      }`}
                    >
                      {/* Active Measuring Scanner Light */}
                      {isCurrentActive && (
                        <div className="absolute inset-x-0 bottom-0 h-0.5 bg-amber-400 animate-ping" />
                      )}

                      {/* Display Value */}
                      <span className="truncate">
                        {hasValue
                          ? cell.value?.toFixed(3).replace('.', ',')
                          : '---'}
                      </span>
                    </div>
                  );
                })}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
