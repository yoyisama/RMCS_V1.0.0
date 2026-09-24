import React, { useState, useEffect } from 'react';
import { ThemeMode } from '../types';

interface FooterBarProps {
  plcConnected: boolean;
  comConnected: boolean;
  comPort: string;
  onTogglePlc: () => void;
  onToggleCom: () => void;
  theme?: ThemeMode;
}

export const FooterBar: React.FC<FooterBarProps> = ({
  plcConnected,
  comConnected,
  comPort,
  onTogglePlc,
  onToggleCom,
  theme = 'dark',
}) => {
  const [currentTime, setCurrentTime] = useState<string>('');
  const isDark = theme === 'dark';

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      setCurrentTime(`${year}/${month}/${day} ${hours}:${minutes}:${seconds}`);
    };

    updateTime();
    const timer = setInterval(updateTime, 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <footer
      className={`shrink-0 px-3 sm:px-4 py-1 sm:py-1.5 flex flex-wrap items-center justify-between text-[11px] sm:text-xs select-none border-t transition-colors ${
        isDark
          ? 'bg-[#181c24] border-[#2a3140] text-slate-300'
          : 'bg-white border-slate-200 text-slate-600 shadow-xs'
      }`}
    >
      {/* Left: Communication Statuses */}
      <div className="flex items-center gap-2.5 sm:gap-4">
        {/* PLC Status */}
        <button
          onClick={onTogglePlc}
          title="点击切换 PLC 连接状态"
          className="flex items-center gap-1.5 hover:opacity-80 transition-opacity cursor-pointer font-medium"
        >
          <div
            className={`w-2 h-2 rounded-full ${
              plcConnected
                ? 'bg-[#22c55e] animate-pulse shadow-[0_0_6px_#22c55e]'
                : 'bg-[#ef4444]'
            }`}
          />
          <span
            className={
              plcConnected
                ? isDark
                  ? 'text-slate-200'
                  : 'text-slate-800'
                : 'text-rose-500 font-bold'
            }
          >
            PLC通信: {plcConnected ? '已连接' : '中断'}
          </span>
        </button>

        <span className={isDark ? 'text-slate-700' : 'text-slate-300'}>|</span>

        {/* COM Port Resistance Meter Status */}
        <button
          onClick={onToggleCom}
          title="点击切换电阻计串口连接状态"
          className="flex items-center gap-1.5 hover:opacity-80 transition-opacity cursor-pointer font-medium"
        >
          <div
            className={`w-2 h-2 rounded-full ${
              comConnected
                ? 'bg-[#22c55e] animate-pulse shadow-[0_0_6px_#22c55e]'
                : 'bg-[#ef4444]'
            }`}
          />
          <span
            className={
              comConnected
                ? isDark
                  ? 'text-slate-200'
                  : 'text-slate-800'
                : 'text-rose-500 font-bold'
            }
          >
            电阻计({comPort}): {comConnected ? '就绪' : '脱机'}
          </span>
        </button>

        <span className="hidden md:inline text-slate-700">|</span>

        {/* Baud rate info */}
        <span className="hidden md:inline text-slate-500 font-mono">
          9600bps 8-N-1
        </span>
      </div>

      {/* Right: Version & Real-time Clock */}
      <div className="flex items-center gap-2 sm:gap-4 ml-auto font-mono text-[10px] sm:text-xs">
        <span className="hidden sm:inline text-slate-500">
          RMCS工业控制系统平台
        </span>
        <span
          className={`px-2 py-0.5 rounded font-bold tracking-wider ${
            isDark
              ? 'bg-[#12151b] text-[#38bdf8] border border-[#252d3d]'
              : 'bg-slate-100 text-slate-700'
          }`}
        >
          {currentTime}
        </span>
      </div>
    </footer>
  );
};
