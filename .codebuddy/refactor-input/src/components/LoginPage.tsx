import React, { useState } from 'react';
import { 
  Lock, 
  User, 
  ShieldCheck, 
  AlertCircle, 
  ArrowRight, 
  Eye, 
  EyeOff, 
  Cpu, 
  Activity, 
  CheckCircle2, 
  Sun, 
  Moon,
  Radio,
  Layers,
  Sparkles
} from 'lucide-react';
import { AuthUser, ThemeMode } from '../types';

interface LoginPageProps {
  onLoginSuccess: (user: AuthUser) => void;
  theme: ThemeMode;
  onToggleTheme: () => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({
  onLoginSuccess,
  theme,
  onToggleTheme,
}) => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('123456');
  const [showPassword, setShowPassword] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [station, setStation] = useState('ST-01 (阻抗自动测试工位)');

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
    <div
      className={`min-h-screen w-screen flex flex-col justify-between font-sans select-none transition-colors duration-200 relative overflow-hidden ${
        isDark
          ? 'bg-[#0e1117] text-[#e0e6ed]'
          : 'bg-[#f1f5f9] text-slate-800'
      }`}
    >
      {/* Background Industrial Grid Lines */}
      <div 
        className="absolute inset-0 pointer-events-none opacity-[0.035] dark:opacity-[0.06]"
        style={{
          backgroundImage: isDark
            ? 'linear-gradient(to right, #38bdf8 1px, transparent 1px), linear-gradient(to bottom, #38bdf8 1px, transparent 1px)'
            : 'linear-gradient(to right, #0078d4 1px, transparent 1px), linear-gradient(to bottom, #0078d4 1px, transparent 1px)',
          backgroundSize: '40px 40px',
        }}
      />

      {/* Top Navbar */}
      <header
        className={`px-4 sm:px-8 py-3.5 flex items-center justify-between border-b relative z-10 ${
          isDark
            ? 'bg-[#141821]/80 border-[#232a39] backdrop-blur-md'
            : 'bg-white/80 border-slate-200 backdrop-blur-md'
        }`}
      >
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-[#d9383a] text-white flex items-center justify-center font-black text-lg shadow-md border border-red-400/40">
            R
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-black text-base tracking-wider text-[#e63946]">
                RMCS
              </span>
              <span className="text-[10px] font-mono px-1.5 py-0.2 rounded border bg-blue-500/10 text-blue-400 border-blue-500/20">
                INDUSTRIAL
              </span>
            </div>
            <p className="text-[11px] text-slate-400 font-mono -mt-0.5">
              自动测阻机控制系统 · SCADA Console
            </p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <div className="hidden sm:flex items-center gap-1.5 text-xs font-mono text-emerald-400 bg-emerald-950/40 border border-emerald-600/30 px-2.5 py-1 rounded-full">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>系统总线通讯待命</span>
          </div>

          <button
            onClick={onToggleTheme}
            title={isDark ? '切换至浅色模式' : '切换至暗黑工控模式'}
            className={`w-8 h-8 rounded-lg flex items-center justify-center border transition-all cursor-pointer ${
              isDark
                ? 'bg-[#1b202c] border-[#2e3748] text-amber-300 hover:bg-[#252c3c]'
                : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-100'
            }`}
          >
            {isDark ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
          </button>
        </div>
      </header>

      {/* Center Main Card */}
      <main className="flex-1 flex items-center justify-center p-4 sm:p-6 relative z-10">
        <div className="w-full max-w-4xl grid grid-cols-1 md:grid-cols-12 rounded-2xl border overflow-hidden shadow-2xl transition-all duration-200">
          
          {/* Left Decorative Industrial Hero (5 cols) */}
          <div
            className={`hidden md:flex md:col-span-5 p-8 flex-col justify-between relative overflow-hidden border-r ${
              isDark
                ? 'bg-gradient-to-br from-[#121620] via-[#161b26] to-[#0d1017] border-[#222938]'
                : 'bg-gradient-to-br from-[#005a9e] via-[#0078d4] to-[#004578] text-white border-blue-600'
            }`}
          >
            {/* Background glowing rings */}
            <div className="absolute -top-16 -left-16 w-56 h-56 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />
            <div className="absolute -bottom-16 -right-16 w-56 h-56 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />

            <div>
              <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full text-[11px] font-mono mb-4 border bg-white/10 text-white border-white/20">
                <Cpu className="w-3.5 h-3.5" />
                <span>RMCS-RESISTOR v0.0.0.2</span>
              </div>

              <h2 className="text-2xl font-black tracking-tight leading-snug">
                自动测阻机<br />数字控制中心
              </h2>
              <p
                className={`text-xs mt-2 leading-relaxed ${
                  isDark ? 'text-slate-400' : 'text-blue-100'
                }`}
              >
                高精度7行3区阻值自动巡检、PLC联动步进与工业公差智能判定平台
              </p>
            </div>

            {/* Industrial Diagnostics Checklist */}
            <div className="my-6 space-y-2.5">
              <div
                className={`p-2.5 rounded-lg border text-xs flex items-center gap-2.5 ${
                  isDark
                    ? 'bg-[#181e2b]/80 border-[#273244]'
                    : 'bg-white/15 border-white/20 text-white'
                }`}
              >
                <div className="w-2 h-2 rounded-full bg-emerald-400 shadow-[0_0_6px_#34d399]" />
                <div className="flex-1 min-w-0">
                  <div className="font-semibold text-[11px]">PLC 逻辑控制器</div>
                  <div className="text-[10px] text-slate-400 font-mono">192.168.1.10:502 待命</div>
                </div>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
              </div>

              <div
                className={`p-2.5 rounded-lg border text-xs flex items-center gap-2.5 ${
                  isDark
                    ? 'bg-[#181e2b]/80 border-[#273244]'
                    : 'bg-white/15 border-white/20 text-white'
                }`}
              >
                <div className="w-2 h-2 rounded-full bg-emerald-400 shadow-[0_0_6px_#34d399]" />
                <div className="flex-1 min-w-0">
                  <div className="font-semibold text-[11px]">高精度微欧计串口</div>
                  <div className="text-[10px] text-slate-400 font-mono">COM4 · 9600bps 8-N-1</div>
                </div>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
              </div>

              <div
                className={`p-2.5 rounded-lg border text-xs flex items-center gap-2.5 ${
                  isDark
                    ? 'bg-[#181e2b]/80 border-[#273244]'
                    : 'bg-white/15 border-white/20 text-white'
                }`}
              >
                <div className="w-2 h-2 rounded-full bg-emerald-400 shadow-[0_0_6px_#34d399]" />
                <div className="flex-1 min-w-0">
                  <div className="font-semibold text-[11px]">校准基准与判定库</div>
                  <div className="text-[10px] text-slate-400 font-mono">基准 6.532Ω (±0.010Ω)</div>
                </div>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
              </div>
            </div>

            <div className="text-[10px] font-mono text-slate-400">
              © 2026 RMCS Precision Electronics. All Rights Reserved.
            </div>
          </div>

          {/* Right Form Card (7 cols) */}
          <div
            className={`md:col-span-7 p-6 sm:p-8 flex flex-col justify-center ${
              isDark ? 'bg-[#181d27]' : 'bg-white'
            }`}
          >
            <div className="mb-6">
              <div className="flex items-center gap-2 text-xs font-semibold text-[#0078d4] dark:text-[#38bdf8] mb-1">
                <ShieldCheck className="w-4 h-4" />
                <span>操作员安全认证</span>
              </div>
              <h3 className="text-xl sm:text-2xl font-bold tracking-tight">
                登入系统控制台
              </h3>
              <p
                className={`text-xs mt-1 ${
                  isDark ? 'text-slate-400' : 'text-slate-500'
                }`}
              >
                请输入工号凭证以进入自动测阻机在线控制与调度界面
              </p>
            </div>

            {errorMsg && (
              <div className="mb-4 p-3 rounded-lg text-xs flex items-center gap-2 bg-rose-950/60 border border-rose-600/50 text-rose-300 animate-shake">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Workstation Selector */}
              <div>
                <label
                  className={`block text-xs font-semibold mb-1.5 ${
                    isDark ? 'text-slate-300' : 'text-slate-700'
                  }`}
                >
                  目标工位 (Workstation)
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                    <Radio className="w-4 h-4" />
                  </div>
                  <select
                    value={station}
                    onChange={(e) => setStation(e.target.value)}
                    className={`w-full pl-9 pr-3 py-2 text-xs sm:text-sm rounded-lg border font-medium focus:outline-none focus:ring-2 focus:ring-[#0078d4] ${
                      isDark
                        ? 'bg-[#12141a] border-[#2e3748] text-white'
                        : 'bg-white border-slate-300 text-slate-900'
                    }`}
                  >
                    <option value="ST-01 (阻抗自动测试工位)">
                      ST-01 (阻抗自动测试工位 - 生产线A)
                    </option>
                    <option value="ST-02 (备用抽检工作站)">
                      ST-02 (备用抽检工作站 - 质检线)
                    </option>
                    <option value="SIM-DEBUG (仿真模拟环境)">
                      SIM-DEBUG (仿真与算法测试模式)
                    </option>
                  </select>
                </div>
              </div>

              {/* Username */}
              <div>
                <label
                  className={`block text-xs font-semibold mb-1.5 ${
                    isDark ? 'text-slate-300' : 'text-slate-700'
                  }`}
                >
                  用户工号 (Username)
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
                    placeholder="请输入工号或账号 (默认: admin)"
                    className={`w-full pl-9 pr-3 py-2 text-xs sm:text-sm rounded-lg border font-medium focus:outline-none focus:ring-2 focus:ring-[#0078d4] ${
                      isDark
                        ? 'bg-[#12141a] border-[#2e3748] text-white placeholder:text-slate-600'
                        : 'bg-white border-slate-300 text-slate-900'
                    }`}
                  />
                </div>
              </div>

              {/* Password */}
              <div>
                <label
                  className={`block text-xs font-semibold mb-1.5 ${
                    isDark ? 'text-slate-300' : 'text-slate-700'
                  }`}
                >
                  授权密码 (Password)
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
                    className={`w-full pl-9 pr-10 py-2 text-xs sm:text-sm rounded-lg border font-medium focus:outline-none focus:ring-2 focus:ring-[#0078d4] ${
                      isDark
                        ? 'bg-[#12141a] border-[#2e3748] text-white placeholder:text-slate-600'
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

              {/* Quick Fill Tooltip */}
              <div
                className={`p-2.5 rounded-lg border text-xs flex items-center justify-between ${
                  isDark
                    ? 'bg-[#12151c] border-[#252c3b] text-slate-300'
                    : 'bg-slate-50 border-slate-200 text-slate-600'
                }`}
              >
                <div className="flex items-center gap-1.5">
                  <span className="text-slate-400">初始凭证:</span>
                  <code className="font-mono font-bold text-amber-400 px-1 py-0.2 rounded bg-black/20">
                    admin
                  </code>
                  <span className="text-slate-500">/</span>
                  <code className="font-mono font-bold text-amber-400 px-1 py-0.2 rounded bg-black/20">
                    123456
                  </code>
                </div>
                <button
                  type="button"
                  onClick={fillDefault}
                  className="text-xs text-[#0078d4] dark:text-[#38bdf8] font-bold hover:underline cursor-pointer flex items-center gap-1"
                >
                  <Sparkles className="w-3 h-3" />
                  <span>一键填入</span>
                </button>
              </div>

              {/* Login Button */}
              <button
                id="btn-login-submit"
                type="submit"
                className="w-full py-2.5 px-4 bg-[#0078d4] hover:bg-[#0063b1] active:scale-[0.99] text-white font-bold text-sm rounded-lg shadow-lg shadow-blue-500/20 transition-all flex items-center justify-center gap-2 cursor-pointer mt-2"
              >
                <span>进入控制系统</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer
        className={`px-4 sm:px-8 py-3 text-center text-xs font-mono border-t ${
          isDark
            ? 'bg-[#11141b] border-[#202736] text-slate-500'
            : 'bg-white border-slate-200 text-slate-500'
        }`}
      >
        <span>RMCS 自动化控制系统 · 设备就绪状态: 正常</span>
        <span className="mx-2 text-slate-700">|</span>
        <span>工业通信标准: RS232 / TCP-IP Modbus</span>
      </footer>
    </div>
  );
};
