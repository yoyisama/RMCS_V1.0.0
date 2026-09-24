import React, { useState } from 'react';
import { X, Sliders, Save, Check, RotateCcw } from 'lucide-react';
import { TestStandard, SystemConfig, ThemeMode } from '../types';

interface StandardConfigModalProps {
  isOpen: boolean;
  onClose: () => void;
  config: SystemConfig;
  onSave: (newConfig: SystemConfig) => void;
  theme?: ThemeMode;
}

export const StandardConfigModal: React.FC<StandardConfigModalProps> = ({
  isOpen,
  onClose,
  config,
  onSave,
  theme = 'dark',
}) => {
  const [standardVal, setStandardVal] = useState<number>(config.standard.standardValue);
  const [upperDev, setUpperDev] = useState<number>(config.standard.upperDev);
  const [lowerDev, setLowerDev] = useState<number>(config.standard.lowerDev);
  const [comPort, setComPort] = useState<string>(config.comPort);
  const [baudRate, setBaudRate] = useState<number>(config.baudRate);
  const [plcIp, setPlcIp] = useState<string>(config.plcIp);
  const [simInterval, setSimInterval] = useState<number>(config.simIntervalMs);
  const [isAutoFetch, setIsAutoFetch] = useState<boolean>(config.isAutoFetch ?? true);

  if (!isOpen) return null;

  const isDark = theme === 'dark';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave({
      standard: {
        standardValue: standardVal,
        upperDev: upperDev,
        lowerDev: lowerDev,
      },
      comPort,
      baudRate,
      plcIp,
      simIntervalMs: simInterval,
      isAutoFetch,
    });
    onClose();
  };

  const handleResetDefaults = () => {
    setStandardVal(6.532);
    setUpperDev(0.01);
    setLowerDev(0.01);
    setComPort('COM4');
    setBaudRate(9600);
    setPlcIp('192.168.1.10');
    setSimInterval(1400);
    setIsAutoFetch(true);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-xs p-3 sm:p-4 overflow-y-auto">
      <div
        className={`rounded-xl border w-full max-w-lg overflow-hidden flex flex-col shadow-2xl my-auto max-h-[92vh] ${
          isDark
            ? 'bg-[#181c24] border-[#2e3748] text-[#e2e8f0]'
            : 'bg-white border-slate-200 text-slate-800'
        }`}
      >
        {/* Header */}
        <div className="bg-gradient-to-r from-[#005a9e] via-[#0078d4] to-[#0098f4] px-4 py-3 text-white flex items-center justify-between shrink-0 shadow-xs">
          <div className="flex items-center gap-2">
            <Sliders className="w-4 h-4" />
            <h3 className="text-sm font-bold tracking-wide">
              系统与测定标准参数设定
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded hover:bg-white/20 text-white cursor-pointer transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body with scrolling */}
        <form onSubmit={handleSubmit} className="p-4 sm:p-5 space-y-4 overflow-y-auto flex-1">
          {/* Section 1: Resistance Criteria */}
          <div>
            <div className="flex items-center gap-2 mb-2 pb-1 border-b border-[#2c3342]">
              <div className="w-1.5 h-3.5 bg-[#22c55e] rounded-full" />
              <h4
                className={`text-xs font-bold ${
                  isDark ? 'text-slate-200' : 'text-slate-800'
                }`}
              >
                测阻判定标准参数 (Ω)
              </h4>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label
                  className={`block text-xs font-semibold mb-1 ${
                    isDark ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  目标标准值 (Ω)
                </label>
                <input
                  type="number"
                  step="0.001"
                  required
                  value={standardVal}
                  onChange={(e) => setStandardVal(parseFloat(e.target.value) || 0)}
                  className={`w-full px-2.5 py-1.5 text-xs sm:text-sm font-mono rounded border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                    isDark
                      ? 'bg-[#12141a] border-[#2e3748] text-white'
                      : 'bg-white border-slate-300 text-slate-800'
                  }`}
                />
              </div>

              <div>
                <label
                  className={`block text-xs font-semibold mb-1 ${
                    isDark ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  正向公差上限 (+Ω)
                </label>
                <input
                  type="number"
                  step="0.001"
                  required
                  value={upperDev}
                  onChange={(e) => setUpperDev(parseFloat(e.target.value) || 0)}
                  className={`w-full px-2.5 py-1.5 text-xs sm:text-sm font-mono rounded border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                    isDark
                      ? 'bg-[#12141a] border-[#2e3748] text-white'
                      : 'bg-white border-slate-300 text-slate-800'
                  }`}
                />
              </div>

              <div>
                <label
                  className={`block text-xs font-semibold mb-1 ${
                    isDark ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  负向公差下限 (-Ω)
                </label>
                <input
                  type="number"
                  step="0.001"
                  required
                  value={lowerDev}
                  onChange={(e) => setLowerDev(parseFloat(e.target.value) || 0)}
                  className={`w-full px-2.5 py-1.5 text-xs sm:text-sm font-mono rounded border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                    isDark
                      ? 'bg-[#12141a] border-[#2e3748] text-white'
                      : 'bg-white border-slate-300 text-slate-800'
                  }`}
                />
              </div>
            </div>

            {/* Tolerance preview badge */}
            <div
              className={`mt-2 p-2 rounded text-xs flex items-center justify-between border ${
                isDark
                  ? 'bg-[#12141b] border-[#252e3e] text-slate-300'
                  : 'bg-slate-50 border-slate-200 text-slate-700'
              }`}
            >
              <span>合格判定区间:</span>
              <span className="font-mono font-bold text-emerald-400">
                {(standardVal - lowerDev).toFixed(3)} Ω ~{' '}
                {(standardVal + upperDev).toFixed(3)} Ω
              </span>
            </div>
          </div>

          {/* Section 2: Hardware Communication */}
          <div>
            <div className="flex items-center gap-2 mb-2 pb-1 border-b border-[#2c3342]">
              <div className="w-1.5 h-3.5 bg-[#0078d4] rounded-full" />
              <h4
                className={`text-xs font-bold ${
                  isDark ? 'text-slate-200' : 'text-slate-800'
                }`}
              >
                硬件通信接口配置
              </h4>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label
                  className={`block text-xs font-semibold mb-1 ${
                    isDark ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  电阻计串口号 (COM Port)
                </label>
                <input
                  type="text"
                  required
                  value={comPort}
                  onChange={(e) => setComPort(e.target.value)}
                  placeholder="例如 COM4"
                  className={`w-full px-2.5 py-1.5 text-xs sm:text-sm font-mono rounded border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                    isDark
                      ? 'bg-[#12141a] border-[#2e3748] text-white'
                      : 'bg-white border-slate-300 text-slate-800'
                  }`}
                />
              </div>

              <div>
                <label
                  className={`block text-xs font-semibold mb-1 ${
                    isDark ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  PLC 以太网 IP 地址
                </label>
                <input
                  type="text"
                  required
                  value={plcIp}
                  onChange={(e) => setPlcIp(e.target.value)}
                  placeholder="192.168.1.10"
                  className={`w-full px-2.5 py-1.5 text-xs sm:text-sm font-mono rounded border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                    isDark
                      ? 'bg-[#12141a] border-[#2e3748] text-white'
                      : 'bg-white border-slate-300 text-slate-800'
                  }`}
                />
              </div>

              <div>
                <label
                  className={`block text-xs font-semibold mb-1 ${
                    isDark ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  波特率 (Baud Rate)
                </label>
                <select
                  value={baudRate}
                  onChange={(e) => setBaudRate(parseInt(e.target.value, 10))}
                  className={`w-full px-2.5 py-1.5 text-xs sm:text-sm font-mono rounded border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                    isDark
                      ? 'bg-[#12141a] border-[#2e3748] text-white'
                      : 'bg-white border-slate-300 text-slate-800'
                  }`}
                >
                  <option value={4800}>4800</option>
                  <option value={9600}>9600 (默认)</option>
                  <option value={19200}>19200</option>
                  <option value={38400}>38400</option>
                  <option value={115200}>115200</option>
                </select>
              </div>

              <div>
                <label
                  className={`block text-xs font-semibold mb-1 ${
                    isDark ? 'text-slate-300' : 'text-slate-600'
                  }`}
                >
                  模拟采样节拍间隔 (毫秒)
                </label>
                <input
                  type="number"
                  min={500}
                  max={5000}
                  step={100}
                  value={simInterval}
                  onChange={(e) => setSimInterval(parseInt(e.target.value, 10) || 1400)}
                  className={`w-full px-2.5 py-1.5 text-xs sm:text-sm font-mono rounded border focus:outline-none focus:ring-1 focus:ring-[#0078d4] ${
                    isDark
                      ? 'bg-[#12141a] border-[#2e3748] text-white'
                      : 'bg-white border-slate-300 text-slate-800'
                  }`}
                />
              </div>
            </div>
          </div>

          {/* Footer Actions */}
          <div
            className={`pt-3 border-t flex items-center justify-between gap-2 shrink-0 ${
              isDark ? 'border-[#262c3a]' : 'border-slate-200'
            }`}
          >
            <button
              type="button"
              onClick={handleResetDefaults}
              className={`py-1.5 px-3 rounded text-xs font-semibold flex items-center gap-1.5 border transition-colors cursor-pointer ${
                isDark
                  ? 'border-[#333d4e] text-slate-400 hover:text-white hover:bg-[#202735]'
                  : 'border-slate-300 text-slate-600 hover:bg-slate-100'
              }`}
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>恢复标准预设</span>
            </button>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onClose}
                className={`py-1.5 px-3 rounded text-xs font-semibold border transition-colors cursor-pointer ${
                  isDark
                    ? 'border-[#333d4e] text-slate-300 hover:bg-[#222a38]'
                    : 'border-slate-300 text-slate-700 hover:bg-slate-100'
                }`}
              >
                取消
              </button>

              <button
                type="submit"
                className="py-1.5 px-4 bg-[#0078d4] hover:bg-[#0063b1] text-white font-bold text-xs rounded shadow-sm flex items-center gap-1.5 cursor-pointer transition-colors"
              >
                <Save className="w-3.5 h-3.5" />
                <span>保存生效</span>
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};
