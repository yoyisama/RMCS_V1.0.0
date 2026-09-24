import React, { useState } from 'react';
import { Lock, User, ShieldCheck, AlertCircle, ArrowRight, Eye, EyeOff } from 'lucide-react';
import { AuthUser, ThemeMode } from '../types';

interface LoginModalProps {
  isOpen: boolean;
  onLoginSuccess: (user: AuthUser) => void;
  theme?: ThemeMode;
}

export const LoginModal: React.FC<LoginModalProps> = ({
  isOpen,
  onLoginSuccess,
  theme = 'dark',
}) => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('123456');
  const [showPassword, setShowPassword] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  if (!isOpen) return null;

  const isDark = theme === 'dark';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (username.trim() === 'admin' && password === '123456') {
      setErrorMsg('');
      onLoginSuccess({
        username: 'admin',
        name: '管理员',
        role: '超级管理员',
        isLoggedIn: true,
      });
    } else {
      setErrorMsg('用户名或密码错误 (默认账号: admin / 密码: 123456)');
    }
  };

  const fillDefault = () => {
    setUsername('admin');
    setPassword('123456');
    setErrorMsg('');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-xs p-3 sm:p-4 overflow-y-auto">
      <div
        className={`rounded-xl border w-full max-w-md overflow-hidden transition-all shadow-2xl my-auto ${
          isDark
            ? 'bg-[#1a1e27] border-[#2f3747] text-[#e2e8f0]'
            : 'bg-white border-slate-200 text-slate-800'
        }`}
      >
        {/* Top Branding Banner */}
        <div className="bg-gradient-to-r from-[#005a9e] via-[#0078d4] to-[#0098f4] px-5 py-4 text-white flex items-center justify-between shadow-xs">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-[#d9383a] flex items-center justify-center font-black text-xl text-white shadow-md border border-red-300/40">
              R
            </div>
            <div>
              <h2 className="text-base sm:text-lg font-bold tracking-tight">
                自动测阻机控制系统
              </h2>
              <p className="text-[11px] text-blue-100 font-mono">
                RMCS Industrial System v0.0.0.2
              </p>
            </div>
          </div>
          <div className="p-2 rounded-full bg-white/10 text-white">
            <ShieldCheck className="w-5 h-5" />
          </div>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-5 sm:p-6 space-y-3.5 sm:space-y-4">
          <div className="text-center mb-1">
            <h3
              className={`text-sm sm:text-base font-bold ${
                isDark ? 'text-white' : 'text-slate-800'
              }`}
            >
              操作员权限登录
            </h3>
            <p
              className={`text-xs mt-0.5 ${
                isDark ? 'text-slate-400' : 'text-slate-500'
              }`}
            >
              请验证权限以访问自动测阻机调度与参数控制面板
            </p>
          </div>

          {errorMsg && (
            <div
              className={`p-2.5 rounded-lg text-xs flex items-center gap-2 ${
                isDark
                  ? 'bg-rose-950/60 border border-rose-600/50 text-rose-300'
                  : 'bg-red-50 border border-red-200 text-red-600'
              }`}
            >
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* Username */}
          <div>
            <label
              className={`block text-xs font-semibold mb-1 ${
                isDark ? 'text-slate-300' : 'text-slate-700'
              }`}
            >
              用户账号 (Username)
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <User className="w-4 h-4" />
              </div>
              <input
                id="input-username"
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="请输入用户名 (默认: admin)"
                className={`w-full pl-9 pr-3 py-2 text-sm rounded-lg focus:outline-none focus:ring-2 focus:ring-[#0078d4] font-medium border ${
                  isDark
                    ? 'bg-[#12141a] border-[#2e3748] text-white placeholder:text-slate-500'
                    : 'bg-white border-slate-300 text-slate-900'
                }`}
              />
            </div>
          </div>

          {/* Password */}
          <div>
            <label
              className={`block text-xs font-semibold mb-1 ${
                isDark ? 'text-slate-300' : 'text-slate-700'
              }`}
            >
              操作密码 (Password)
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <Lock className="w-4 h-4" />
              </div>
              <input
                id="input-password"
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="请输入密码 (默认: 123456)"
                className={`w-full pl-9 pr-10 py-2 text-sm rounded-lg focus:outline-none focus:ring-2 focus:ring-[#0078d4] font-medium border ${
                  isDark
                    ? 'bg-[#12141a] border-[#2e3748] text-white placeholder:text-slate-500'
                    : 'bg-white border-slate-300 text-slate-900'
                }`}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-slate-200 cursor-pointer"
              >
                {showPassword ? (
                  <EyeOff className="w-4 h-4" />
                ) : (
                  <Eye className="w-4 h-4" />
                )}
              </button>
            </div>
          </div>

          {/* Quick Fill Tooltip / Button */}
          <div className="flex items-center justify-between pt-0.5 text-xs">
            <span className={isDark ? 'text-slate-400' : 'text-slate-500'}>
              默认凭证:{' '}
              <code
                className={`px-1 py-0.5 rounded font-mono font-bold ${
                  isDark
                    ? 'bg-[#12141a] text-amber-300'
                    : 'bg-slate-100 text-slate-700'
                }`}
              >
                admin
              </code>{' '}
              /{' '}
              <code
                className={`px-1 py-0.5 rounded font-mono font-bold ${
                  isDark
                    ? 'bg-[#12141a] text-amber-300'
                    : 'bg-slate-100 text-slate-700'
                }`}
              >
                123456
              </code>
            </span>
            <button
              type="button"
              id="btn-quick-fill"
              onClick={fillDefault}
              className="text-[#38bdf8] hover:text-[#7dd3fc] font-semibold cursor-pointer underline underline-offset-2"
            >
              一键填入
            </button>
          </div>

          {/* Submit button */}
          <button
            id="btn-submit-login"
            type="submit"
            className="w-full mt-2 py-2.5 px-4 bg-[#0078d4] hover:bg-[#0063b1] active:scale-[0.99] text-white font-bold text-sm rounded-lg shadow-md transition-all flex items-center justify-center gap-2 cursor-pointer"
          >
            <span>立即登入控制系统</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div
          className={`px-5 py-2.5 border-t text-center text-[11px] font-mono ${
            isDark
              ? 'bg-[#14171f] border-[#252c3b] text-slate-500'
              : 'bg-slate-50 border-slate-200 text-slate-500'
          }`}
        >
          RMCS · 工业自动化控制与阻抗采集系统
        </div>
      </div>
    </div>
  );
};
