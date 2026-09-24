import React, { useState } from 'react';
import { X, Search, Download, CheckCircle, XCircle } from 'lucide-react';
import { InspectionRecord, ThemeMode } from '../types';

interface HistoryQueryModalProps {
  isOpen: boolean;
  onClose: () => void;
  records: InspectionRecord[];
  onExport: () => void;
  theme?: ThemeMode;
}

export const HistoryQueryModal: React.FC<HistoryQueryModalProps> = ({
  isOpen,
  onClose,
  records,
  onExport,
  theme = 'dark',
}) => {
  const [filterPos, setFilterPos] = useState<'ALL' | 'L' | 'M' | 'R'>('ALL');
  const [filterResult, setFilterResult] = useState<'ALL' | 'TRUE' | 'FALSE'>('ALL');
  const [filterRow, setFilterRow] = useState<string>('ALL');

  if (!isOpen) return null;

  const isDark = theme === 'dark';

  const filtered = records.filter((r) => {
    if (filterPos !== 'ALL' && r.position !== filterPos) return false;
    if (filterResult === 'TRUE' && !r.result) return false;
    if (filterResult === 'FALSE' && r.result) return false;
    if (filterRow !== 'ALL' && r.row.toString() !== filterRow) return false;
    return true;
  });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-xs p-3 sm:p-4 overflow-y-auto">
      <div
        className={`rounded-xl border w-full max-w-3xl overflow-hidden flex flex-col max-h-[88vh] shadow-2xl my-auto ${
          isDark
            ? 'bg-[#181c24] border-[#2e3748] text-[#e2e8f0]'
            : 'bg-white border-slate-200 text-slate-800'
        }`}
      >
        {/* Title Bar */}
        <div className="bg-gradient-to-r from-[#005a9e] via-[#0078d4] to-[#0098f4] px-4 py-3 text-white flex items-center justify-between shrink-0 shadow-xs">
          <div className="flex items-center gap-2">
            <Search className="w-4 h-4" />
            <h3 className="text-sm font-bold tracking-wide">
              测试数据履历查询中心
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded hover:bg-white/20 text-white cursor-pointer transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Filters */}
        <div
          className={`p-3 border-b flex flex-wrap items-center justify-between gap-2 text-xs shrink-0 ${
            isDark
              ? 'bg-[#13161c] border-[#262c3a]'
              : 'bg-slate-50 border-slate-200'
          }`}
        >
          <div className="flex flex-wrap items-center gap-2">
            {/* Position Filter */}
            <div className="flex items-center gap-1">
              <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
                位置:
              </span>
              <select
                value={filterPos}
                onChange={(e) => setFilterPos(e.target.value as any)}
                className={`rounded px-2 py-1 border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                  isDark
                    ? 'bg-[#1b202c] border-[#313b4e] text-slate-200'
                    : 'bg-white border-slate-300 text-slate-700'
                }`}
              >
                <option value="ALL">全部 (L/M/R)</option>
                <option value="L">左区 (L)</option>
                <option value="M">中区 (M)</option>
                <option value="R">右区 (R)</option>
              </select>
            </div>

            {/* Row Filter */}
            <div className="flex items-center gap-1">
              <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
                行数:
              </span>
              <select
                value={filterRow}
                onChange={(e) => setFilterRow(e.target.value)}
                className={`rounded px-2 py-1 border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                  isDark
                    ? 'bg-[#1b202c] border-[#313b4e] text-slate-200'
                    : 'bg-white border-slate-300 text-slate-700'
                }`}
              >
                <option value="ALL">全部行 (1~7)</option>
                <option value="1">第1行</option>
                <option value="2">第2行</option>
                <option value="3">第3行</option>
                <option value="4">第4行</option>
                <option value="5">第5行</option>
                <option value="6">第6行</option>
                <option value="7">第7行</option>
              </select>
            </div>

            {/* Result Filter */}
            <div className="flex items-center gap-1">
              <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
                判定:
              </span>
              <select
                value={filterResult}
                onChange={(e) => setFilterResult(e.target.value as any)}
                className={`rounded px-2 py-1 border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                  isDark
                    ? 'bg-[#1b202c] border-[#313b4e] text-slate-200'
                    : 'bg-white border-slate-300 text-slate-700'
                }`}
              >
                <option value="ALL">全部结果</option>
                <option value="TRUE">True (合格)</option>
                <option value="FALSE">False (超差)</option>
              </select>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <span
              className={`font-mono text-[11px] ${
                isDark ? 'text-slate-400' : 'text-slate-500'
              }`}
            >
              符合条件: {filtered.length} 条
            </span>
            <button
              onClick={onExport}
              className="py-1 px-2.5 bg-[#8e24aa] hover:bg-[#7b1fa2] text-white rounded font-semibold text-xs flex items-center gap-1 shadow-xs transition-colors cursor-pointer"
            >
              <Download className="w-3.5 h-3.5" />
              <span>导出CSV</span>
            </button>
          </div>
        </div>

        {/* Results Table */}
        <div className="flex-1 min-h-0 overflow-y-auto">
          <table
            className={`w-full text-left text-xs font-mono border-collapse ${
              isDark ? 'text-slate-300' : 'text-slate-700'
            }`}
          >
            <thead
              className={`sticky top-0 font-bold ${
                isDark
                  ? 'bg-[#151922] text-slate-300 border-b border-[#282f3d]'
                  : 'bg-slate-100 text-slate-700 border-b border-slate-200'
              }`}
            >
              <tr>
                <th className="py-2 px-3 text-center">序号</th>
                <th className="py-2 px-3 text-center">行号</th>
                <th className="py-2 px-3 text-center">位置</th>
                <th className="py-2 px-3 text-center">测量阻值 (Ω)</th>
                <th className="py-2 px-3 text-center">标准阻值 (Ω)</th>
                <th className="py-2 px-3 text-center">公差判定</th>
                <th className="py-2 px-3 text-center">采集时间</th>
              </tr>
            </thead>
            <tbody
              className={`divide-y ${
                isDark ? 'divide-[#202532]' : 'divide-slate-100'
              }`}
            >
              {filtered.length === 0 ? (
                <tr>
                  <td
                    colSpan={7}
                    className="py-8 text-center text-slate-400 italic font-sans"
                  >
                    未找到匹配的检测数据
                  </td>
                </tr>
              ) : (
                filtered.map((item, idx) => (
                  <tr
                    key={item.id}
                    className={`transition-colors ${
                      isDark ? 'hover:bg-[#1f2638]' : 'hover:bg-slate-50'
                    }`}
                  >
                    <td
                      className={`py-1.5 px-3 text-center ${
                        isDark ? 'text-slate-500' : 'text-slate-400'
                      }`}
                    >
                      {idx + 1}
                    </td>
                    <td className="py-1.5 px-3 text-center font-bold">
                      第{item.row}行
                    </td>
                    <td className="py-1.5 px-3 text-center">
                      <span
                        className={`px-2 py-0.5 rounded font-bold text-[11px] ${
                          isDark
                            ? 'bg-[#1e2638] text-[#38bdf8] border border-[#313f5c]'
                            : 'bg-slate-100 text-slate-800'
                        }`}
                      >
                        {item.position}区
                      </span>
                    </td>
                    <td className="py-1.5 px-3 text-center font-bold text-[#38bdf8]">
                      {item.measuredValue.toFixed(3)}
                    </td>
                    <td
                      className={`py-1.5 px-3 text-center ${
                        isDark ? 'text-slate-400' : 'text-slate-500'
                      }`}
                    >
                      {item.standardValue.toFixed(3)}
                    </td>
                    <td className="py-1.5 px-3 text-center">
                      <span
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-bold ${
                          item.result
                            ? isDark
                              ? 'text-emerald-400 bg-emerald-950/60 border border-emerald-600/50'
                              : 'text-emerald-700 bg-emerald-50 border border-emerald-200'
                            : isDark
                            ? 'text-rose-400 bg-rose-950/60 border border-rose-500/60'
                            : 'text-rose-600 bg-rose-50 border border-rose-200'
                        }`}
                      >
                        {item.result ? (
                          <>
                            <CheckCircle className="w-3 h-3" />
                            <span>True (合格)</span>
                          </>
                        ) : (
                          <>
                            <XCircle className="w-3 h-3" />
                            <span>False (超差)</span>
                          </>
                        )}
                      </span>
                    </td>
                    <td
                      className={`py-1.5 px-3 text-center text-[11px] ${
                        isDark ? 'text-slate-400' : 'text-slate-500'
                      }`}
                    >
                      {item.time}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Footer */}
        <div
          className={`px-4 py-2.5 border-t flex items-center justify-between text-xs shrink-0 ${
            isDark
              ? 'bg-[#14171f] border-[#252c3b]'
              : 'bg-slate-50 border-slate-200'
          }`}
        >
          <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
            按老项目标准记录：7行 × 3区 (L/M/R)
          </span>
          <button
            onClick={onClose}
            className={`py-1 px-3 rounded font-semibold border transition-colors cursor-pointer ${
              isDark
                ? 'border-[#333d4e] text-slate-300 hover:bg-[#202735]'
                : 'border-slate-300 text-slate-700 hover:bg-slate-200'
            }`}
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  );
};
