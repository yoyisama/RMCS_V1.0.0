import React from 'react';
import { InspectionRecord, ThemeMode } from '../types';
import { Zap, CheckCircle2, XCircle } from 'lucide-react';

interface DataHistoryPanelProps {
  records: InspectionRecord[];
  onSelectRecord?: (record: InspectionRecord) => void;
  theme?: ThemeMode;
  isAutoFetch?: boolean;
  queryIntervalMs?: number;
  onManualFetch?: () => void;
}

export const DataHistoryPanel: React.FC<DataHistoryPanelProps> = ({
  records,
  onSelectRecord,
  theme = 'dark',
  isAutoFetch = true,
  queryIntervalMs = 1400,
  onManualFetch,
}) => {
  const isDark = theme === 'dark';

  const passCount = records.filter((r) => r.result).length;
  const failCount = records.filter((r) => !r.result).length;
  const yieldRate = records.length > 0 ? ((passCount / records.length) * 100).toFixed(1) : '100.0';

  return (
    <div
      className={`rounded-lg border p-2 sm:p-2.5 flex flex-col h-full select-none overflow-hidden transition-colors ${
        isDark
          ? 'bg-[#181c24] border-[#2c3342] shadow-md shadow-black/20'
          : 'bg-white border-sky-200 shadow-xs'
      }`}
    >
      {/* Header */}
      <div
        className={`flex items-center justify-between mb-1.5 pb-1 border-b ${
          isDark ? 'border-[#262c38]' : 'border-slate-100'
        }`}
      >
        <div className="flex items-center gap-2">
          <div className="w-1.5 h-3.5 bg-[#22c55e] rounded-full shadow-[0_0_6px_#22c55e]" />
          <h2
            className={`text-xs sm:text-sm font-bold tracking-tight ${
              isDark ? 'text-white' : 'text-slate-800'
            }`}
          >
            数据履历
          </h2>
          <span
            className={`text-[10px] font-mono px-1.5 py-0.2 rounded border flex items-center gap-1 ${
              isAutoFetch
                ? isDark
                  ? 'bg-emerald-950/60 border-emerald-600/40 text-emerald-400'
                  : 'bg-emerald-50 border-emerald-200 text-emerald-700'
                : isDark
                ? 'bg-amber-950/60 border-amber-600/40 text-amber-400'
                : 'bg-amber-50 border-amber-200 text-amber-700'
            }`}
          >
            <span className={`w-1.5 h-1.5 rounded-full ${isAutoFetch ? 'bg-emerald-400 animate-pulse' : 'bg-amber-400'}`} />
            <span>
              {isAutoFetch ? `自动获取 (${(queryIntervalMs / 1000).toFixed(1)}s)` : '手动获取模式'}
            </span>
          </span>
        </div>

        <div className="flex items-center gap-2">
          {onManualFetch && !isAutoFetch && (
            <button
              onClick={onManualFetch}
              className="text-[10px] px-2 py-0.5 rounded bg-sky-600 hover:bg-sky-500 text-white font-bold flex items-center gap-1 cursor-pointer transition-colors"
            >
              <Zap className="w-2.5 h-2.5" />
              <span>单次采样</span>
            </button>
          )}
          <span
            className={`text-[10px] sm:text-xs font-mono ${
              isDark ? 'text-slate-400' : 'text-slate-500'
            }`}
          >
            实时记录 ({records.length}条)
          </span>
        </div>
      </div>

      {/* Table Container with scrollable body */}
      <div
        className={`flex-1 min-h-0 rounded border overflow-hidden flex flex-col ${
          isDark ? 'border-[#282f3d] bg-[#12141a]' : 'border-slate-200 bg-white'
        }`}
      >
        {/* Table Header */}
        <div
          className={`text-[10px] sm:text-xs font-bold grid grid-cols-6 text-center py-1 sm:py-1.5 px-1 divide-x shrink-0 ${
            isDark
              ? 'bg-[#151922] text-slate-300 border-b border-[#282f3d] divide-[#282f3d]'
              : 'bg-slate-50 text-slate-700 border-b border-slate-200 divide-slate-200'
          }`}
        >
          <div>行号</div>
          <div>位置</div>
          <div>测量值</div>
          <div>标准值</div>
          <div>判定</div>
          <div>检测时间</div>
        </div>

        {/* Table Body */}
        <div
          className={`flex-1 min-h-0 overflow-y-auto divide-y font-mono text-[11px] sm:text-xs ${
            isDark ? 'divide-[#202532] text-slate-300' : 'divide-slate-100 text-slate-700'
          }`}
        >
          {records.length === 0 ? (
            <div className="py-8 text-center text-slate-500 italic text-xs">
              暂无检测数据，点击【设备启动】或【单次获取】开始作业
            </div>
          ) : (
            records.map((item) => (
              <div
                key={item.id}
                onClick={() => onSelectRecord && onSelectRecord(item)}
                className={`grid grid-cols-6 text-center py-1 px-1 transition-colors cursor-pointer divide-x items-center ${
                  isDark
                    ? 'hover:bg-[#1c2333] divide-[#202532]'
                    : 'hover:bg-sky-50/70 divide-slate-100'
                }`}
              >
                <div
                  className={`font-semibold ${
                    isDark ? 'text-slate-200' : 'text-slate-800'
                  }`}
                >
                  第{item.row}行
                </div>
                <div>
                  <span
                    className={`px-1 py-0.2 rounded font-bold text-[10px] sm:text-xs ${
                      isDark
                        ? 'bg-[#1e2638] text-[#38bdf8] border border-[#313f5c]'
                        : 'bg-slate-100 text-slate-800'
                    }`}
                  >
                    {item.position}区
                  </span>
                </div>
                <div className="font-bold text-[#38bdf8]">
                  {item.measuredValue.toFixed(3)}
                </div>
                <div className={isDark ? 'text-slate-400' : 'text-slate-500'}>
                  {item.standardValue.toFixed(3)}
                </div>
                <div className="flex items-center justify-center">
                  <span
                    className={`inline-flex items-center gap-0.5 px-1.5 py-0.2 rounded text-[10px] font-bold ${
                      item.result
                        ? isDark
                          ? 'text-emerald-400 bg-emerald-950/50 border border-emerald-600/50'
                          : 'text-emerald-700 bg-emerald-50 border border-emerald-200'
                        : isDark
                        ? 'text-rose-400 bg-rose-950/50 border border-rose-500/60'
                        : 'text-rose-600 bg-rose-50 border border-rose-200'
                    }`}
                  >
                    {item.result ? (
                      <CheckCircle2 className="w-2.5 h-2.5 text-emerald-400" />
                    ) : (
                      <XCircle className="w-2.5 h-2.5 text-rose-400" />
                    )}
                    <span>{item.result ? 'OK' : 'NG'}</span>
                  </span>
                </div>
                <div
                  className={`text-[10px] truncate px-1 ${
                    isDark ? 'text-slate-400' : 'text-slate-500'
                  }`}
                >
                  {item.time.split(' ')[1] || item.time}
                </div>
              </div>
            ))
          )}
        </div>

        {/* Statistical Summary Footer */}
        <div
          className={`shrink-0 px-2 py-1 text-[10px] font-mono flex items-center justify-between border-t ${
            isDark
              ? 'bg-[#151922] border-[#282f3d] text-slate-400'
              : 'bg-slate-50 border-slate-200 text-slate-600'
          }`}
        >
          <div className="flex items-center gap-3">
            <span>总计: <strong className="text-inherit">{records.length}</strong></span>
            <span>合格: <strong className="text-emerald-500">{passCount}</strong></span>
            <span>超差: <strong className="text-rose-500">{failCount}</strong></span>
          </div>
          <div>
            <span>良率: </span>
            <span className={`font-bold ${parseFloat(yieldRate) >= 95 ? 'text-emerald-400' : 'text-amber-400'}`}>
              {yieldRate}%
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
