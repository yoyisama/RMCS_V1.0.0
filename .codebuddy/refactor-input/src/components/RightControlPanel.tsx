import React, { useState } from 'react';
import { 
  Sliders, 
  RotateCcw, 
  Play, 
  Square, 
  Search, 
  FileText, 
  Download, 
  Activity, 
  Clock, 
  Gauge, 
  RefreshCw,
  Zap,
  Layers,
  ChevronDown,
  Check
} from 'lucide-react';
import { TestStandard, ProductionStatus, ThemeMode } from '../types';

interface RightControlPanelProps {
  standard: TestStandard;
  status: ProductionStatus;
  isRunning: boolean;
  isAutoFetch: boolean;
  onToggleAutoFetch: () => void;
  queryIntervalMs: number;
  onChangeQueryInterval: (ms: number) => void;
  onManualFetch: () => void;
  isFetchingPulse?: boolean;
  theme?: ThemeMode;
  onOpenConfig: () => void;
  onResetCounter: () => void;
  onStart: () => void;
  onStop: () => void;
  onReset: () => void;
  onOpenHistory: () => void;
  onOpenLogs: () => void;
  onExportData: () => void;
}

const INTERVAL_PRESETS = [
  { label: '0.5s', value: 500, hz: '2.0Hz' },
  { label: '1.0s', value: 1000, hz: '1.0Hz' },
  { label: '1.4s', value: 1400, hz: '0.7Hz' },
  { label: '2.0s', value: 2000, hz: '0.5Hz' },
  { label: '3.0s', value: 3000, hz: '0.3Hz' },
];

export const RightControlPanel: React.FC<RightControlPanelProps> = ({
  standard,
  status,
  isRunning,
  isAutoFetch,
  onToggleAutoFetch,
  queryIntervalMs,
  onChangeQueryInterval,
  onManualFetch,
  isFetchingPulse = false,
  theme = 'dark',
  onOpenConfig,
  onResetCounter,
  onStart,
  onStop,
  onReset,
  onOpenHistory,
  onOpenLogs,
  onExportData,
}) => {
  const isDark = theme === 'dark';
  const [showResetConfirm, setShowResetConfirm] = useState(false);
  const [isCustomIntervalOpen, setIsCustomIntervalOpen] = useState(false);
  const [customMsInput, setCustomMsInput] = useState<string>(String(queryIntervalMs));

  // Format seconds to hh:mm:ss
  const formatTime = (totalSeconds: number) => {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  };

  const lowerLimit = standard.standardValue - standard.lowerDev;
  const upperLimit = standard.standardValue + standard.upperDev;

  const handleApplyCustomInterval = (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseInt(customMsInput, 10);
    if (!isNaN(val) && val >= 200 && val <= 10000) {
      onChangeQueryInterval(val);
      setIsCustomIntervalOpen(false);
    }
  };

  return (
    <div className="flex flex-col gap-2 sm:gap-2.5 select-none pb-1">
      
      {/* ========================================================= */}
      {/* 1. 测定标准与公差 (Precision Standard & Tolerances) */}
      {/* ========================================================= */}
      <section
        className={`rounded-xl border p-2.5 sm:p-3 flex flex-col transition-all shrink-0 ${
          isDark
            ? 'bg-[#151922] border-[#252e3e] shadow-md shadow-black/25'
            : 'bg-white border-slate-200 shadow-xs'
        }`}
      >
        {/* Header with Title and Integrated Config Button */}
        <div className="flex items-center justify-between pb-1.5 mb-1.5 border-b border-inherit">
          <div className="flex items-center gap-1.5">
            <div className="w-5 h-5 rounded-md bg-sky-500/15 border border-sky-500/30 flex items-center justify-center text-sky-400">
              <Gauge className="w-3 h-3" />
            </div>
            <div>
              <h2 className="text-xs sm:text-sm font-bold tracking-tight text-inherit">
                测定标准与公差
              </h2>
            </div>
          </div>

          <button
            id="btn-param-config"
            onClick={onOpenConfig}
            title="修改测定标准与通讯参数"
            className={`px-2 py-0.5 rounded text-[11px] font-semibold flex items-center gap-1 transition-all cursor-pointer border ${
              isDark
                ? 'bg-[#1e2638] hover:bg-[#28334b] border-[#313f5c] text-sky-400 shadow-xs'
                : 'bg-sky-50 hover:bg-sky-100 border-sky-200 text-sky-700 shadow-xs'
            }`}
          >
            <Sliders className="w-3 h-3" />
            <span>参数设定</span>
          </button>
        </div>

        {/* Standard Value Display & Range Pill */}
        <div className="grid grid-cols-12 gap-1.5 items-center">
          {/* Main Standard Readout */}
          <div
            className={`col-span-7 rounded-lg p-2 border flex flex-col justify-center ${
              isDark
                ? 'bg-[#11131a] border-[#222836]'
                : 'bg-slate-50 border-slate-200'
            }`}
          >
            <span className="text-[10px] text-slate-400 font-medium">
              基准标准值 (Target)
            </span>
            <div className="flex items-baseline gap-1 mt-0.5">
              <span className="font-mono font-black text-xl sm:text-2xl text-sky-500 tracking-tight">
                {standard.standardValue.toFixed(3)}
              </span>
              <span className="text-xs font-bold text-slate-400">Ω</span>
            </div>
          </div>

          {/* Tolerance Bounds Dual Tag */}
          <div className="col-span-5 flex flex-col gap-1.5">
            <div
              className={`px-2 py-1 rounded border text-[11px] font-mono flex items-center justify-between ${
                isDark
                  ? 'bg-[#1b202c] border-[#2a3449] text-amber-300'
                  : 'bg-amber-50/80 border-amber-200 text-amber-700'
              }`}
            >
              <span className="text-[10px] text-slate-400">上偏差</span>
              <span className="font-bold">+{standard.upperDev.toFixed(2)}</span>
            </div>

            <div
              className={`px-2 py-1 rounded border text-[11px] font-mono flex items-center justify-between ${
                isDark
                  ? 'bg-[#1b202c] border-[#2a3449] text-amber-300'
                  : 'bg-amber-50/80 border-amber-200 text-amber-700'
              }`}
            >
              <span className="text-[10px] text-slate-400">下偏差</span>
              <span className="font-bold">-{standard.lowerDev.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Pass Band Indicator Bar */}
        <div
          className={`mt-2 pt-1.5 border-t border-inherit flex items-center justify-between text-[11px] font-mono ${
            isDark ? 'text-slate-400' : 'text-slate-500'
          }`}
        >
          <span>合格窗口:</span>
          <span className="font-bold text-emerald-600 dark:text-emerald-400">
            [{lowerLimit.toFixed(3)} Ω ~ {upperLimit.toFixed(3)} Ω]
          </span>
        </div>
      </section>

      {/* ========================================================= */}
      {/* 2. 数据采集与查询控制 (Data Acquisition & Query Interval) */}
      {/* ========================================================= */}
      <section
        className={`rounded-xl border p-2.5 sm:p-3 flex flex-col transition-all shrink-0 ${
          isDark
            ? 'bg-[#151922] border-[#252e3e] shadow-md shadow-black/25'
            : 'bg-white border-slate-200 shadow-xs'
        }`}
      >
        {/* Section Header with Activity Pulsing Indicator */}
        <div className="flex items-center justify-between pb-1.5 mb-2 border-b border-inherit">
          <div className="flex items-center gap-1.5">
            <div className="w-5 h-5 rounded-md bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
              <RefreshCw className={`w-3 h-3 ${isRunning && isAutoFetch ? 'animate-spin' : ''}`} style={{ animationDuration: `${Math.max(queryIntervalMs, 1000)}ms` }} />
            </div>
            <div>
              <h2 className="text-xs sm:text-sm font-bold tracking-tight text-inherit">
                数据采集与查询控制
              </h2>
            </div>
          </div>

          {/* RX / TX Comm Indicator */}
          <div className="flex items-center gap-1 text-[10px] font-mono">
            <span
              className={`w-2 h-2 rounded-full transition-colors ${
                isFetchingPulse
                  ? 'bg-[#22c55e] shadow-[0_0_8px_#22c55e]'
                  : isRunning && isAutoFetch
                  ? 'bg-emerald-400/60'
                  : 'bg-slate-400'
              }`}
            />
            <span
              className={
                isFetchingPulse
                  ? 'text-emerald-500 font-bold'
                  : isDark
                  ? 'text-slate-400'
                  : 'text-slate-500'
              }
            >
              {isFetchingPulse ? 'RX数据采样' : isAutoFetch ? '轮询就绪' : '手动模式'}
            </span>
          </div>
        </div>

        {/* Control Row 1: 自动获取数据 Toggle + 单次获取 Trigger */}
        <div
          className={`flex items-center justify-between gap-2 p-2 rounded-lg border mb-2 transition-colors ${
            isDark
              ? 'bg-[#11131a] border-[#222836]'
              : 'bg-slate-50 border-slate-200'
          }`}
        >
          {/* Auto Fetch Switch */}
          <div className="flex items-center gap-2.5">
            <button
              id="toggle-auto-fetch-btn"
              type="button"
              onClick={onToggleAutoFetch}
              className={`relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${
                isAutoFetch
                  ? 'bg-emerald-500'
                  : isDark
                  ? 'bg-slate-700'
                  : 'bg-slate-300'
              }`}
              title={isAutoFetch ? '点击关闭自动获取数据' : '点击开启自动获取数据'}
            >
              <span
                className={`pointer-events-none inline-block h-4 w-4 transform rounded-full bg-white shadow-md ring-0 transition duration-200 ease-in-out ${
                  isAutoFetch ? 'translate-x-4' : 'translate-x-0'
                }`}
              />
            </button>
            <div className="flex flex-col">
              <span
                className={`text-xs font-bold leading-tight ${
                  isDark ? 'text-slate-100' : 'text-slate-800'
                }`}
              >
                自动获取数据
              </span>
              <span
                className={`text-[10px] font-mono font-medium ${
                  isAutoFetch
                    ? isDark
                      ? 'text-emerald-400'
                      : 'text-emerald-600 font-semibold'
                    : isDark
                    ? 'text-amber-400'
                    : 'text-amber-600 font-semibold'
                }`}
              >
                {isAutoFetch ? '周期轮询中' : '手动触发模式'}
              </span>
            </div>
          </div>

          {/* Manual Fetch Trigger Button */}
          <button
            id="btn-single-fetch"
            onClick={onManualFetch}
            title="手动触发单次数据采样"
            className={`px-3 py-1.5 rounded-md text-xs font-bold flex items-center gap-1.5 transition-all cursor-pointer active:scale-95 border ${
              isDark
                ? 'bg-[#1f2637] hover:bg-[#2b354d] border-[#34425e] text-sky-300 shadow-xs'
                : 'bg-white hover:bg-sky-50 border-sky-300 text-sky-800 shadow-xs'
            }`}
          >
            <Zap className="w-3.5 h-3.5 text-amber-500 fill-amber-500" />
            <span>单次获取</span>
          </button>
        </div>

        {/* Control Row 2: 查询间隔 (Query Interval) Presets */}
        <div className="space-y-1.5">
          <div className="flex items-center justify-between text-[11px]">
            <span className="font-semibold text-slate-400">查询间隔切换:</span>
            <span className="font-mono text-sky-500 dark:text-sky-400 font-bold">
              {queryIntervalMs} ms ({ (queryIntervalMs / 1000).toFixed(1) }s / 次)
            </span>
          </div>

          {/* Preset Buttons Grid */}
          <div className="grid grid-cols-5 gap-1.5">
            {INTERVAL_PRESETS.map((preset) => {
              const isSelected = queryIntervalMs === preset.value;
              return (
                <button
                  key={preset.value}
                  type="button"
                  onClick={() => onChangeQueryInterval(preset.value)}
                  className={`py-1.5 px-1 rounded text-center transition-all cursor-pointer border ${
                    isSelected
                      ? 'bg-[#0078d4] text-white font-bold border-blue-400 shadow-[0_0_8px_rgba(0,120,212,0.4)]'
                      : isDark
                      ? 'bg-[#181d27] hover:bg-[#222938] text-slate-300 border-[#283244]'
                      : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-200'
                  }`}
                  title={`将查询间隔设为 ${preset.value}ms (${preset.hz})`}
                >
                  <div className="text-[11px] font-mono leading-none font-bold">
                    {preset.label}
                  </div>
                  <div className="text-[8px] opacity-75 font-mono mt-0.5">
                    {preset.hz}
                  </div>
                </button>
              );
            })}
          </div>

          {/* Custom Milliseconds Toggle */}
          <div className="pt-0.5 flex justify-end">
            <button
              type="button"
              onClick={() => {
                setCustomMsInput(String(queryIntervalMs));
                setIsCustomIntervalOpen(!isCustomIntervalOpen);
              }}
              className="text-[10px] text-slate-400 hover:text-sky-500 flex items-center gap-0.5 cursor-pointer font-mono"
            >
              <span>自定义采样周期</span>
              <ChevronDown className={`w-2.5 h-2.5 transition-transform ${isCustomIntervalOpen ? 'rotate-180' : ''}`} />
            </button>
          </div>

          {isCustomIntervalOpen && (
            <form onSubmit={handleApplyCustomInterval} className="flex items-center gap-1.5 pt-1.5 mt-1 border-t border-inherit">
              <input
                type="number"
                min={200}
                max={10000}
                step={50}
                value={customMsInput}
                onChange={(e) => setCustomMsInput(e.target.value)}
                placeholder="毫秒 (200~10000)"
                className={`flex-1 px-2.5 py-1 text-xs font-mono rounded border focus:outline-none focus:ring-1 focus:ring-sky-500 ${
                  isDark
                    ? 'bg-[#11131a] border-[#2b3548] text-white'
                    : 'bg-white border-slate-300 text-slate-800'
                }`}
              />
              <button
                type="submit"
                className="px-2.5 py-1 rounded bg-sky-600 hover:bg-sky-500 text-white font-bold text-xs flex items-center gap-1 cursor-pointer"
              >
                <Check className="w-3 h-3" />
                <span>应用</span>
              </button>
            </form>
          )}
        </div>
      </section>

      {/* ========================================================= */}
      {/* 3. 生产运行遥测 (Operation Status & Telemetry) */}
      {/* ========================================================= */}
      <section
        className={`rounded-xl border p-2.5 sm:p-3 flex flex-col transition-all shrink-0 ${
          isDark
            ? 'bg-[#151922] border-[#252e3e] shadow-md shadow-black/25'
            : 'bg-white border-slate-200 shadow-xs'
        }`}
      >
        {/* Header with Title and Reset Action */}
        <div className="flex items-center justify-between pb-1.5 mb-2 border-b border-inherit">
          <div className="flex items-center gap-1.5">
            <div className="w-5 h-5 rounded-md bg-purple-500/15 border border-purple-500/30 flex items-center justify-center text-purple-400">
              <Activity className="w-3 h-3" />
            </div>
            <div>
              <h2 className="text-xs sm:text-sm font-bold tracking-tight text-inherit">
                运行状况与遥测
              </h2>
            </div>
          </div>

          {/* Clean Reset Button with confirm safeguard */}
          {showResetConfirm ? (
            <div className="flex items-center gap-1 animate-fadeIn">
              <span className="text-[10px] text-rose-500 font-bold">清零?</span>
              <button
                onClick={() => {
                  onResetCounter();
                  setShowResetConfirm(false);
                }}
                className="px-2 py-0.5 rounded bg-rose-600 hover:bg-rose-700 text-white font-bold text-[10px] cursor-pointer"
              >
                确定
              </button>
              <button
                onClick={() => setShowResetConfirm(false)}
                className="px-2 py-0.5 rounded bg-slate-700 hover:bg-slate-600 text-slate-200 text-[10px] cursor-pointer"
              >
                取消
              </button>
            </div>
          ) : (
            <button
              id="btn-reset-counter"
              onClick={() => setShowResetConfirm(true)}
              title="清零当前完成数量与运行时长"
              className={`px-2 py-0.5 rounded text-[11px] font-semibold flex items-center gap-1 transition-all cursor-pointer border ${
                isDark
                  ? 'bg-[#1b202c] hover:bg-[#252d3f] border-[#2e394f] text-slate-300'
                  : 'bg-slate-100 hover:bg-slate-200 border-slate-300 text-slate-700'
              }`}
            >
              <RotateCcw className="w-2.5 h-2.5 text-slate-400" />
              <span>产量清零</span>
            </button>
          )}
        </div>

        {/* Telemetry Metrics Grid */}
        <div className="grid grid-cols-3 gap-1.5">
          {/* Card A: 完成数量 */}
          <div
            className={`rounded-lg p-2 border flex flex-col justify-between ${
              isDark
                ? 'bg-[#11131a] border-[#222836]'
                : 'bg-slate-50 border-slate-200'
            }`}
          >
            <div className="text-[10px] text-slate-400 font-medium">完成数量</div>
            <div className="my-0.5">
              <span className="font-mono font-black text-base sm:text-lg text-sky-500">
                {status.completedCount.toLocaleString()}
              </span>
              <span className="text-[10px] text-slate-400 ml-0.5 font-bold">件</span>
            </div>
            <div className="text-[9px] text-emerald-500 font-mono flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              <span>自动累计</span>
            </div>
          </div>

          {/* Card B: 设备运行状况 */}
          <div
            className={`rounded-lg p-2 border flex flex-col justify-between ${
              isDark
                ? 'bg-[#11131a] border-[#222836]'
                : 'bg-slate-50 border-slate-200'
            }`}
          >
            <div className="text-[10px] text-slate-400 font-medium">设备状态</div>
            <div className="my-0.5">
              <span
                className={`inline-flex items-center gap-1 font-bold text-[11px] px-2 py-0.5 rounded-full ${
                  status.status === '运行中'
                    ? 'text-emerald-500 bg-emerald-50 dark:bg-emerald-950/70 border border-emerald-500/50 shadow-xs'
                    : status.status === '待机'
                    ? 'text-sky-600 bg-sky-50 dark:bg-sky-950/70 border border-sky-400/50'
                    : 'text-rose-500 bg-rose-50 dark:bg-rose-950/70 border border-rose-500/50'
                }`}
              >
                <span
                  className={`w-1.5 h-1.5 rounded-full ${
                    status.status === '运行中'
                      ? 'bg-emerald-500 animate-ping'
                      : status.status === '待机'
                      ? 'bg-sky-500'
                      : 'bg-rose-500'
                  }`}
                />
                <span>{status.status}</span>
              </span>
            </div>
            <div className="text-[9px] text-slate-400 font-mono">
              {status.status === '运行中' ? '巡检中' : '就绪'}
            </div>
          </div>

          {/* Card C: 运行时长 */}
          <div
            className={`rounded-lg p-2 border flex flex-col justify-between ${
              isDark
                ? 'bg-[#11131a] border-[#222836]'
                : 'bg-slate-50 border-slate-200'
            }`}
          >
            <div className="text-[10px] text-slate-400 font-medium flex items-center justify-between">
              <span>运行时长</span>
              <Clock className="w-3 h-3 text-slate-400" />
            </div>
            <div className="my-0.5">
              <span className="font-mono font-bold text-xs sm:text-sm text-inherit">
                {formatTime(status.runDurationSeconds)}
              </span>
            </div>
            <div className="text-[9px] text-slate-400 font-mono">
              {status.runDurationSeconds} 秒
            </div>
          </div>
        </div>
      </section>

      {/* ========================================================= */}
      {/* 4. 人员操作调度中心 (Personnel Operations & Machine Actuation) */}
      {/* ========================================================= */}
      <section
        className={`rounded-xl border p-2.5 sm:p-3 flex flex-col gap-2.5 sm:gap-3 transition-all shrink-0 ${
          isDark
            ? 'bg-[#151922] border-[#252e3e] shadow-md shadow-black/25'
            : 'bg-white border-slate-200 shadow-xs'
        }`}
      >
        {/* Section Header */}
        <div className="flex items-center justify-between pb-1.5 border-b border-inherit shrink-0">
          <div className="flex items-center gap-1.5">
            <div className="w-5 h-5 rounded-md bg-blue-500/15 border border-blue-500/30 flex items-center justify-center text-blue-400">
              <Layers className="w-3 h-3" />
            </div>
            <div>
              <h2 className="text-xs sm:text-sm font-bold tracking-tight text-inherit">
                人员操作调度中心
              </h2>
            </div>
          </div>
          <div className="flex items-center gap-1.5">
            <span
              className={`text-[9px] font-mono px-1.5 py-0.5 rounded border ${
                isDark
                  ? 'bg-emerald-950/50 text-emerald-400 border-emerald-600/40'
                  : 'bg-emerald-50 text-emerald-700 border-emerald-200'
              }`}
            >
              ● 联锁保护就绪
            </span>
            <span className="text-[9px] font-mono px-1.5 py-0.5 rounded border bg-blue-500/10 text-blue-400 border-blue-500/20">
              工位01
            </span>
          </div>
        </div>

        {/* Group A: 核心机台启停控制 */}
        <div className="space-y-1.5 shrink-0">
          <div className="text-[10px] font-mono uppercase tracking-wider text-slate-400 flex items-center justify-between">
            <span className="flex items-center gap-1 font-semibold">● 核心机台控制</span>
            <span className="text-[9px] text-slate-400">急停: 正常释放</span>
          </div>

          <div className="grid grid-cols-12 gap-1.5 sm:gap-2">
            {/* Start Button */}
            <button
              id="btn-op-start"
              onClick={onStart}
              className={`col-span-5 py-2 sm:py-2.5 px-2 rounded-lg font-bold text-xs sm:text-sm shadow-md flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-[0.98] border ${
                isRunning
                  ? 'bg-gradient-to-r from-emerald-600 to-teal-600 text-white border-emerald-400 shadow-[0_0_12px_rgba(16,185,129,0.4)] ring-1 ring-emerald-400'
                  : 'bg-emerald-600 hover:bg-emerald-500 text-white border-emerald-500/60 shadow-sm hover:shadow-md'
              }`}
            >
              <Play className={`w-4 h-4 fill-white ${isRunning ? 'animate-bounce' : ''}`} />
              <div className="text-left leading-tight">
                <div className="font-extrabold">{isRunning ? '设备运行中' : '设备启动'}</div>
                <div className="text-[8px] font-normal opacity-85 font-mono">
                  {isRunning ? 'RUNNING' : 'START'}
                </div>
              </div>
            </button>

            {/* Stop Button */}
            <button
              id="btn-op-stop"
              onClick={onStop}
              className="col-span-4 py-2 sm:py-2.5 px-2 rounded-lg bg-gradient-to-r from-rose-700 to-red-600 hover:from-rose-600 hover:to-red-500 active:scale-[0.98] text-white font-bold text-xs sm:text-sm shadow-md flex items-center justify-center gap-1.5 transition-all cursor-pointer border border-rose-400/40"
            >
              <Square className="w-3.5 h-3.5 fill-white" />
              <div className="text-left leading-tight">
                <div className="font-extrabold">设备停止</div>
                <div className="text-[8px] font-normal opacity-85 font-mono">STOP</div>
              </div>
            </button>

            {/* Reset Button */}
            <button
              id="btn-op-reset"
              onClick={onReset}
              className={`col-span-3 py-2 sm:py-2.5 px-1.5 rounded-lg font-bold text-xs sm:text-sm shadow-xs flex items-center justify-center gap-1 transition-all cursor-pointer active:scale-[0.98] border ${
                isDark
                  ? 'bg-[#1f2637] hover:bg-[#2b354c] text-slate-200 border-[#323f59]'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-300'
              }`}
            >
              <RotateCcw className="w-3.5 h-3.5 text-sky-500" />
              <div className="text-left leading-tight">
                <div className="font-extrabold">复位</div>
                <div className="text-[8px] font-normal opacity-75 font-mono">RESET</div>
              </div>
            </button>
          </div>
        </div>

        {/* Group B: 履历与诊断工具 */}
        <div className="space-y-1.5 shrink-0">
          <div className="text-[10px] font-mono uppercase tracking-wider text-slate-400 flex items-center gap-1 font-semibold">
            <span>● 数据履历与日志分析</span>
          </div>

          <div className="grid grid-cols-3 gap-1.5 sm:gap-2">
            {/* 履历查询 */}
            <button
              id="btn-op-history"
              onClick={onOpenHistory}
              className={`py-2 px-1.5 rounded-lg text-xs font-semibold flex flex-col items-center justify-center gap-1 transition-all cursor-pointer active:scale-[0.98] border ${
                isDark
                  ? 'bg-[#181e2b] hover:bg-[#222b3d] border-[#29364d] text-slate-200 hover:border-sky-500/50'
                  : 'bg-slate-50 hover:bg-sky-50/70 border-slate-200 text-slate-800 hover:border-sky-300'
              }`}
            >
              <div className="w-6 h-6 rounded-md bg-sky-500/15 text-sky-500 flex items-center justify-center">
                <Search className="w-3.5 h-3.5" />
              </div>
              <span className="font-bold text-xs">履历查询</span>
            </button>

            {/* 日志查询 */}
            <button
              id="btn-op-logs"
              onClick={onOpenLogs}
              className={`py-2 px-1.5 rounded-lg text-xs font-semibold flex flex-col items-center justify-center gap-1 transition-all cursor-pointer active:scale-[0.98] border ${
                isDark
                  ? 'bg-[#181e2b] hover:bg-[#222b3d] border-[#29364d] text-slate-200 hover:border-amber-500/50'
                  : 'bg-slate-50 hover:bg-amber-50/70 border-slate-200 text-slate-800 hover:border-amber-300'
              }`}
            >
              <div className="w-6 h-6 rounded-md bg-amber-500/15 text-amber-500 flex items-center justify-center">
                <FileText className="w-3.5 h-3.5" />
              </div>
              <span className="font-bold text-xs">日志查询</span>
            </button>

            {/* 导出数据 */}
            <button
              id="btn-op-export"
              onClick={onExportData}
              className={`py-2 px-1.5 rounded-lg text-xs font-semibold flex flex-col items-center justify-center gap-1 transition-all cursor-pointer active:scale-[0.98] border ${
                isDark
                  ? 'bg-[#181e2b] hover:bg-[#222b3d] border-[#29364d] text-slate-200 hover:border-purple-500/50'
                  : 'bg-slate-50 hover:bg-purple-50/70 border-slate-200 text-slate-800 hover:border-purple-300'
              }`}
            >
              <div className="w-6 h-6 rounded-md bg-purple-500/15 text-purple-500 flex items-center justify-center">
                <Download className="w-3.5 h-3.5" />
              </div>
              <span className="font-bold text-xs">导出数据</span>
            </button>
          </div>
        </div>

      </section>
    </div>
  );
};
