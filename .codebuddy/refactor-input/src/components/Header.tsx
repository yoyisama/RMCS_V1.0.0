import React from 'react';
import { Maximize2, Minimize2, Power, Sun, Moon, Shield } from 'lucide-react';
import { AuthUser, ThemeMode } from '../types';

interface HeaderProps {
  user: AuthUser;
  isAutoMode: boolean;
  onToggleAutoMode: () => void;
  onLogout: () => void;
  isFullscreen: boolean;
  onToggleFullscreen: () => void;
  theme: ThemeMode;
  onToggleTheme: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  user,
  isAutoMode,
  onToggleAutoMode,
  onLogout,
  isFullscreen,
  onToggleFullscreen,
  theme,
  onToggleTheme,
}) => {
  const isDark = theme === 'dark';

  return (
    <header
      className={`shrink-0 px-3 sm:px-4 py-1.5 sm:py-2 flex items-center justify-between select-none relative z-10 border-b transition-colors ${
        isDark
          ? 'bg-[#181c24] border-[#2a3140] text-[#e2e8f0] shadow-sm'
          : 'bg-white border-slate-200 text-slate-800 shadow-xs'
      }`}
    >
      {/* Left: RMCS Logo & Brand */}
      <div className="flex items-center gap-2 sm:gap-2.5 shrink-0">
        <div className="w-7 h-7 sm:w-8 sm:h-8 rounded-md bg-[#d9383a] text-white flex items-center justify-center font-black text-base sm:text-lg shadow-sm border border-red-400/40">
          R
        </div>
        <div className="flex flex-col">
          <span className="font-black text-sm sm:text-base tracking-wider text-[#e63946]">
            RMCS
          </span>
          <span className="hidden sm:inline text-[9px] font-mono text-slate-400 -mt-1 tracking-tighter">
            INDUSTRIAL
          </span>
        </div>
      </div>

      {/* Center: Big Bold Title */}
      <div className="text-center mx-2 truncate pointer-events-none">
        <h1
          className={`text-base sm:text-xl lg:text-2xl font-black tracking-tight flex items-center justify-center gap-1.5 ${
            isDark ? 'text-white' : 'text-slate-800'
          }`}
        >
          <span>自动测阻机控制系统</span>
          <span
            className={`hidden md:inline-block text-[10px] sm:text-xs font-mono px-1.5 py-0.5 rounded border ${
              isDark
                ? 'bg-blue-950/60 border-blue-600/40 text-blue-400'
                : 'bg-blue-50 border-blue-200 text-blue-700'
            }`}
          >
            v0.0.0.2
          </span>
        </h1>
      </div>

      {/* Right: Controls & User info */}
      <div className="flex items-center gap-2 sm:gap-3 shrink-0">
        {/* Mode Indicator & Switch */}
        <button
          id="btn-toggle-auto-mode"
          onClick={onToggleAutoMode}
          title="点击切换自动/手动模式"
          className="flex flex-col items-end cursor-pointer group text-right"
        >
          <div className="flex items-center gap-1">
            <span
              className={`inline-block w-2 h-2 rounded-full ${
                isAutoMode
                  ? 'bg-[#22c55e] animate-pulse shadow-[0_0_8px_#22c55e]'
                  : 'bg-[#f59e0b]'
              }`}
            />
            <span
              className={`text-xs sm:text-sm font-black transition-colors ${
                isAutoMode
                  ? isDark
                    ? 'text-[#4ade80]'
                    : 'text-[#2e7d32]'
                  : isDark
                  ? 'text-[#fbbf24]'
                  : 'text-[#f57c00]'
              }`}
            >
              {isAutoMode ? '自动模式' : '手动模式'}
            </span>
          </div>
          <div
            className={`text-[10px] sm:text-[11px] flex items-center gap-1 ${
              isDark ? 'text-slate-400' : 'text-slate-500'
            }`}
          >
            <span className="hidden sm:inline">操作员:</span>
            <span
              className={`font-semibold ${
                isDark ? 'text-slate-200' : 'text-slate-700'
              }`}
            >
              {user.name}
            </span>
          </div>
        </button>

        {/* Theme Switcher Button */}
        <button
          id="btn-toggle-theme"
          onClick={onToggleTheme}
          title={isDark ? '切换至浅色模式' : '切换至暗黑工控模式'}
          className={`w-7 h-7 sm:w-8 sm:h-8 rounded-full flex items-center justify-center shadow-xs cursor-pointer transition-transform active:scale-95 border ${
            isDark
              ? 'bg-[#252c3b] hover:bg-[#31394c] border-[#384357] text-amber-300'
              : 'bg-slate-100 hover:bg-slate-200 border-slate-300 text-slate-700'
          }`}
        >
          {isDark ? <Sun className="w-3.5 h-3.5 sm:w-4 sm:h-4" /> : <Moon className="w-3.5 h-3.5 sm:w-4 sm:h-4" />}
        </button>

        {/* Fullscreen Button */}
        <button
          id="btn-header-fullscreen"
          onClick={onToggleFullscreen}
          title={isFullscreen ? '退出全屏' : '全屏显示'}
          className="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-[#0078d4] hover:bg-[#0063b1] text-white flex items-center justify-center shadow-sm cursor-pointer transition-transform active:scale-95"
        >
          {isFullscreen ? (
            <Minimize2 className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          ) : (
            <Maximize2 className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
          )}
        </button>

        {/* Logout / Exit Button */}
        <button
          id="btn-header-logout"
          onClick={onLogout}
          title="注销 / 退出登录"
          className="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-[#d32f2f] hover:bg-[#b71c1c] text-white flex items-center justify-center shadow-sm cursor-pointer transition-transform active:scale-95"
        >
          <Power className="w-3.5 h-3.5 sm:w-4 sm:h-4" />
        </button>
      </div>
    </header>
  );
};
