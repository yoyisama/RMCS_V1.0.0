import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
  AuthUser,
  InspectionRecord,
  MatrixState,
  ProductionStatus,
  SystemConfig,
  LogEntry,
  ThemeMode,
} from './types';
import { Header } from './components/Header';
import { WorkMatrixPanel } from './components/WorkMatrixPanel';
import { DataHistoryPanel } from './components/DataHistoryPanel';
import { RightControlPanel } from './components/RightControlPanel';
import { ActionLogPanel } from './components/ActionLogPanel';
import { FooterBar } from './components/FooterBar';
import { LoginPage } from './components/LoginPage';
import { LogQueryModal } from './components/LogQueryModal';
import { HistoryQueryModal } from './components/HistoryQueryModal';
import { StandardConfigModal } from './components/StandardConfigModal';

// Initial initial sample history data matching the legacy screenshot
const INITIAL_RECORDS: InspectionRecord[] = [
  {
    id: 'rec-1',
    row: 1,
    position: 'L',
    measuredValue: 3.167,
    standardValue: 3.200,
    result: false,
    time: '2024/4/25 19:49',
  },
  {
    id: 'rec-2',
    row: 1,
    position: 'M',
    measuredValue: 2.241,
    standardValue: 2.241,
    result: true,
    time: '2024/4/25 19:49',
  },
  {
    id: 'rec-3',
    row: 2,
    position: 'L',
    measuredValue: 3.412,
    standardValue: 3.412,
    result: true,
    time: '2024/4/25 19:50',
  },
];

const INITIAL_MATRIX: MatrixState = {
  '2-L': { value: 3.412, result: true },
};

export function App() {
  // Authentication State
  const [currentUser, setCurrentUser] = useState<AuthUser>(() => {
    return {
      username: '',
      name: '未登录',
      role: '访客',
      isLoggedIn: false,
    };
  });

  // System Configuration
  const [config, setConfig] = useState<SystemConfig>({
    standard: {
      standardValue: 6.532,
      upperDev: 0.01,
      lowerDev: 0.01,
    },
    comPort: 'COM4',
    baudRate: 9600,
    plcIp: '192.168.1.10',
    simIntervalMs: 1400,
    isAutoFetch: true,
  });

  // Operating Modes & Status
  const [theme, setTheme] = useState<ThemeMode>(() => {
    const savedTheme = localStorage.getItem('rmcs_theme');
    return savedTheme === 'light' ? 'light' : 'dark';
  });
  const [isAutoMode, setIsAutoMode] = useState<boolean>(true);
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);

  useEffect(() => {
    localStorage.setItem('rmcs_theme', theme);
  }, [theme]);

  // Connectivity
  const [plcConnected, setPlcConnected] = useState<boolean>(true);
  const [comConnected, setComConnected] = useState<boolean>(true);

  // Running Status
  const [status, setStatus] = useState<ProductionStatus>({
    completedCount: 8921,
    status: '待机',
    runDurationSeconds: 10000,
  });

  // Inspection Matrix & Historical Data
  const [matrixState, setMatrixState] = useState<MatrixState>(INITIAL_MATRIX);
  const [records, setRecords] = useState<InspectionRecord[]>(INITIAL_RECORDS);
  const [activeCell, setActiveCell] = useState<{ row: number; col: 'L' | 'M' | 'R' } | null>(null);

  // Action Logs (matching screenshot)
  const [logs, setLogs] = useState<LogEntry[]>([
    {
      id: 'log-1',
      time: '16:38:44',
      type: 'success',
      message: 'PLC通讯串口打开成功!',
    },
    {
      id: 'log-2',
      time: '16:38:44',
      type: 'info',
      message: '系统初始化完成，等待启动指令',
    },
    {
      id: 'log-3',
      time: '16:38:45',
      type: 'error',
      message: '电阻计连接失败: 无法打开串口 COM4，请确认端口未被其它软件占用且线缆已连接',
    },
  ]);

  // Modal Visibility States
  const [isLogModalOpen, setIsLogModalOpen] = useState(false);
  const [isHistoryModalOpen, setIsHistoryModalOpen] = useState(false);
  const [isConfigModalOpen, setIsConfigModalOpen] = useState(false);

  // Add Log Helper
  const addLog = useCallback(
    (message: string, type: LogEntry['type'] = 'info') => {
      const now = new Date();
      const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(
        now.getMinutes()
      ).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;

      setLogs((prev) => [
        ...prev,
        {
          id: `log-${Date.now()}-${Math.random().toString(36).substr(2, 4)}`,
          time: timeStr,
          type,
          message,
        },
      ]);
    },
    []
  );

  // Handle Login
  const handleLoginSuccess = (user: AuthUser) => {
    setCurrentUser(user);
    addLog(`用户 ${user.name} (${user.username}) 登录成功，系统控制台已进入就绪状态`, 'success');
  };

  const handleLogout = () => {
    setCurrentUser({
      username: '',
      name: '未登录',
      role: '访客',
      isLoggedIn: false,
    });
    setIsRunning(false);
    setStatus((prev) => ({ ...prev, status: '已停止' }));
    addLog('用户注销退出，系统已锁定并返回登录页', 'warning');
  };

  // Fullscreen Handler
  const handleToggleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().catch(() => {});
      setIsFullscreen(true);
    } else {
      document.exitFullscreen().catch(() => {});
      setIsFullscreen(false);
    }
  };

  // Timer for duration when running
  useEffect(() => {
    if (!isRunning) return;
    const timer = setInterval(() => {
      setStatus((prev) => ({
        ...prev,
        runDurationSeconds: prev.runDurationSeconds + 1,
      }));
    }, 1000);
    return () => clearInterval(timer);
  }, [isRunning]);

  // Comm communication pulse & auto fetch state
  const [isFetchingPulse, setIsFetchingPulse] = useState<boolean>(false);

  // Automated Testing Cycle Simulation
  const currentRowRef = useRef<number>(1);
  const currentPosIdxRef = useRef<number>(0);
  const positions: Array<'L' | 'M' | 'R'> = ['L', 'M', 'R'];

  // Single or periodic measurement execution logic
  const triggerMeasurement = useCallback(() => {
    const targetRow = currentRowRef.current;
    const targetPos = positions[currentPosIdxRef.current];

    // Mark cell as actively testing
    setActiveCell({ row: targetRow, col: targetPos });
    setIsFetchingPulse(true);

    // Simulate measurement reading after brief test probe
    setTimeout(() => {
      setIsFetchingPulse(false);

      // Generate measured resistance near standard
      const isPassChance = Math.random() > 0.12; // 88% pass
      let measuredVal: number;
      if (isPassChance) {
        // within ± 0.007
        measuredVal = Number(
          (config.standard.standardValue + (Math.random() * 0.014 - 0.007)).toFixed(3)
        );
      } else {
        // slight deviation, e.g. 0.02
        const dev = (Math.random() > 0.5 ? 1 : -1) * (0.012 + Math.random() * 0.008);
        measuredVal = Number((config.standard.standardValue + dev).toFixed(3));
      }

      const isResultTrue =
        measuredVal >= config.standard.standardValue - config.standard.lowerDev &&
        measuredVal <= config.standard.standardValue + config.standard.upperDev;

      // Update Matrix State
      const cellKey = `${targetRow}-${targetPos}`;
      setMatrixState((prev) => ({
        ...prev,
        [cellKey]: {
          value: measuredVal,
          result: isResultTrue,
        },
      }));

      // Now Record
      const now = new Date();
      const timeStr = `${now.getFullYear()}/${now.getMonth() + 1}/${now.getDate()} ${String(
        now.getHours()
      ).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

      const newRec: InspectionRecord = {
        id: `rec-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
        row: targetRow,
        position: targetPos,
        measuredValue: measuredVal,
        standardValue: config.standard.standardValue,
        result: isResultTrue,
        time: timeStr,
      };

      setRecords((prev) => [newRec, ...prev.slice(0, 99)]); // keep latest 100

      // Increment Completed Count
      setStatus((prev) => ({
        ...prev,
        completedCount: prev.completedCount + 1,
      }));

      // Log result
      addLog(
        `[数据采集] 第${targetRow}行 [${targetPos}区] 阻值: ${measuredVal.toFixed(
          3
        )}Ω (${isResultTrue ? '合格' : '超差'})`,
        isResultTrue ? 'info' : 'warning'
      );

      // Advance to next position & row
      currentPosIdxRef.current += 1;
      if (currentPosIdxRef.current >= positions.length) {
        currentPosIdxRef.current = 0;
        currentRowRef.current += 1;
        if (currentRowRef.current > 7) {
          currentRowRef.current = 1;
          addLog('工件 7 行矩阵测试全部完成，PLC 发送出料与进料步进信号', 'success');
        }
      }
    }, Math.min(Math.floor(config.simIntervalMs / 2), 400));
  }, [config.standard, config.simIntervalMs, addLog]);

  // Periodic automatic acquisition loop
  useEffect(() => {
    if (!isRunning || !config.isAutoFetch) return;

    const testStepInterval = setInterval(() => {
      triggerMeasurement();
    }, config.simIntervalMs);

    return () => {
      clearInterval(testStepInterval);
    };
  }, [isRunning, config.isAutoFetch, config.simIntervalMs, triggerMeasurement]);

  // Toggle Auto Fetch Mode
  const handleToggleAutoFetch = useCallback(() => {
    setConfig((prev) => {
      const nextAuto = !prev.isAutoFetch;
      addLog(
        `采集模式变更: ${nextAuto ? '开启【自动获取数据】(周期轮询已就绪)' : '切换为【手动触发模式】(周期轮询暂停)'}`,
        nextAuto ? 'info' : 'warning'
      );
      return {
        ...prev,
        isAutoFetch: nextAuto,
      };
    });
  }, [addLog]);

  // Change Query Interval
  const handleChangeQueryInterval = useCallback((newMs: number) => {
    setConfig((prev) => {
      addLog(
        `测阻查询间隔已切换为: ${newMs}ms (${(newMs / 1000).toFixed(1)}秒/次)`,
        'info'
      );
      return {
        ...prev,
        simIntervalMs: newMs,
      };
    });
  }, [addLog]);

  // Manual Single Fetch Trigger
  const handleManualFetch = useCallback(() => {
    addLog('操作员手动触发【单次获取】工位阻值数据...', 'info');
    triggerMeasurement();
  }, [addLog, triggerMeasurement]);

  // Personnel Operations
  const handleStart = () => {
    setIsRunning(true);
    setStatus((prev) => ({ ...prev, status: '运行中' }));
    addLog('操作员点击【设备启动】，测阻机自动循环模式已进入运行状态', 'success');
  };

  const handleStop = () => {
    setIsRunning(false);
    setActiveCell(null);
    setStatus((prev) => ({ ...prev, status: '已停止' }));
    addLog('操作员点击【设备停止】，测试流程紧急暂停', 'warning');
  };

  const handleReset = () => {
    setIsRunning(false);
    setActiveCell(null);
    currentRowRef.current = 1;
    currentPosIdxRef.current = 0;
    setMatrixState({});
    setStatus((prev) => ({ ...prev, status: '待机' }));
    addLog('操作员点击【设备复位】，测试矩阵及工序指针已复位', 'info');
  };

  const handleResetCounter = () => {
    setStatus((prev) => ({
      ...prev,
      completedCount: 0,
      runDurationSeconds: 0,
    }));
    addLog('操作员清零完成数量与运行时长计数器', 'info');
  };

  const handleExportData = () => {
    if (records.length === 0) {
      addLog('导出失败: 当前无检测履历记录', 'warning');
      return;
    }

    const headers = ['行号', '工件位置', '测量值(Ω)', '标准值(Ω)', '判定结果', '检测时间'];
    const rows = records.map((r) => [
      `第${r.row}行`,
      r.position,
      r.measuredValue.toFixed(3),
      r.standardValue.toFixed(3),
      r.result ? 'True(合格)' : 'False(超差)',
      r.time,
    ]);

    const csvContent =
      '\uFEFF' +
      [headers.join(','), ...rows.map((e) => e.join(','))].join('\r\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute(
      'download',
      `RMCS_测阻履历_${new Date().toISOString().slice(0, 10)}.csv`
    );
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    addLog(`检测数据已成功导出为 CSV 表格 (共 ${records.length} 条记录)`, 'success');
  };

  // Toggle Auto/Manual Mode
  const handleToggleAutoMode = () => {
    setIsAutoMode((prev) => {
      const next = !prev;
      addLog(`设备工作模式切换为: ${next ? '自动模式' : '手动模式'}`, 'info');
      return next;
    });
  };

  // If not logged in, render full-screen System Login Page directly
  if (!currentUser.isLoggedIn) {
    return (
      <LoginPage
        onLoginSuccess={handleLoginSuccess}
        theme={theme}
        onToggleTheme={() =>
          setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'))
        }
      />
    );
  }

  return (
    <div
      className={`flex flex-col h-screen w-screen overflow-hidden font-sans select-none transition-colors duration-200 ${
        theme === 'dark'
          ? 'bg-[#121418] text-[#e0e6ed]'
          : 'bg-[#e8edf2] text-slate-800'
      }`}
    >
      {/* Top Header */}
      <Header
        user={currentUser}
        isAutoMode={isAutoMode}
        onToggleAutoMode={handleToggleAutoMode}
        onLogout={handleLogout}
        isFullscreen={isFullscreen}
        onToggleFullscreen={handleToggleFullscreen}
        theme={theme}
        onToggleTheme={() =>
          setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'))
        }
      />

      {/* Main Workspace Layout (adaptive grid that fits all screen resolutions) */}
      <main className="flex-1 min-h-0 p-1.5 sm:p-2 lg:p-2.5 xl:p-3 overflow-y-auto lg:overflow-hidden grid grid-cols-1 lg:grid-cols-12 gap-1.5 sm:gap-2 lg:gap-2.5 xl:gap-3">
        {/* Left & Center Section (8 of 12 columns on large screen) */}
        <div className="lg:col-span-8 flex flex-col gap-1.5 sm:gap-2 lg:gap-2.5 h-full min-h-0 overflow-hidden">
          {/* Top Half: Work Matrix & Data History in two columns */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-1.5 sm:gap-2 lg:gap-2.5 flex-1 min-h-[280px] lg:min-h-0">
            {/* Panel 1: 执行作业 (Work Matrix) */}
            <div className="h-full min-h-0">
              <WorkMatrixPanel
                matrixState={matrixState}
                activeCell={activeCell}
                theme={theme}
                onCellClick={(row, col) => {
                  if (!isAutoMode) {
                    setActiveCell({ row, col });
                    addLog(`手动单步选取: 第${row}行 ${col}区`, 'info');
                  }
                }}
              />
            </div>

            {/* Panel 2: 数据履历 (Data History) */}
            <div className="h-full min-h-0">
              <DataHistoryPanel
                records={records}
                theme={theme}
                isAutoFetch={config.isAutoFetch}
                queryIntervalMs={config.simIntervalMs}
                onManualFetch={handleManualFetch}
                onSelectRecord={(rec) => {
                  addLog(
                    `选中履历记录: 第${rec.row}行 ${rec.position}区, 阻值: ${rec.measuredValue}Ω`,
                    'info'
                  );
                }}
              />
            </div>
          </div>

          {/* Bottom Half: 动作履历 (Action History Console) */}
          <div className="h-28 sm:h-32 lg:h-36 xl:h-44 shrink-0 min-h-0">
            <ActionLogPanel
              logs={logs}
              theme={theme}
              onOpenLogModal={() => setIsLogModalOpen(true)}
            />
          </div>
        </div>

        {/* Right Section (4 of 12 columns on large screen) - Redesigned sleek SCADA layout */}
        <div className="lg:col-span-4 h-full min-h-0 overflow-y-auto pr-0.5">
          <RightControlPanel
            standard={config.standard}
            status={status}
            isRunning={isRunning}
            isAutoFetch={config.isAutoFetch}
            onToggleAutoFetch={handleToggleAutoFetch}
            queryIntervalMs={config.simIntervalMs}
            onChangeQueryInterval={handleChangeQueryInterval}
            onManualFetch={handleManualFetch}
            isFetchingPulse={isFetchingPulse}
            theme={theme}
            onOpenConfig={() => setIsConfigModalOpen(true)}
            onResetCounter={handleResetCounter}
            onStart={handleStart}
            onStop={handleStop}
            onReset={handleReset}
            onOpenHistory={() => setIsHistoryModalOpen(true)}
            onOpenLogs={() => setIsLogModalOpen(true)}
            onExportData={handleExportData}
          />
        </div>
      </main>

      {/* Bottom Footer Bar */}
      <FooterBar
        plcConnected={plcConnected}
        comConnected={comConnected}
        comPort={config.comPort}
        theme={theme}
        onTogglePlc={() => {
          setPlcConnected((prev) => {
            const next = !prev;
            addLog(
              `PLC通讯链路状态切换为: ${next ? '正常连接' : '中断异常'}`,
              next ? 'success' : 'error'
            );
            return next;
          });
        }}
        onToggleCom={() => {
          setComConnected((prev) => {
            const next = !prev;
            addLog(
              next
                ? `电阻计通讯连接成功 (${config.comPort}, ${config.baudRate} 8-N-1)`
                : `电阻计连接失败: 无法打开串口 ${config.comPort}，请确认端口未被占用`,
              next ? 'success' : 'error'
            );
            return next;
          });
        }}
      />

      {/* Modals */}
      <LogQueryModal
        isOpen={isLogModalOpen}
        onClose={() => setIsLogModalOpen(false)}
        logs={logs}
        theme={theme}
      />

      <HistoryQueryModal
        isOpen={isHistoryModalOpen}
        onClose={() => setIsHistoryModalOpen(false)}
        records={records}
        onExport={handleExportData}
        theme={theme}
      />

      <StandardConfigModal
        isOpen={isConfigModalOpen}
        onClose={() => setIsConfigModalOpen(false)}
        config={config}
        theme={theme}
        onSave={(newCfg) => {
          setConfig(newCfg);
          addLog(
            `测定标准已更新: 标准值 ${newCfg.standard.standardValue.toFixed(
              3
            )}Ω, 容差 [${newCfg.standard.lowerDev} / +${newCfg.standard.upperDev}]`,
            'success'
          );
        }}
      />
    </div>
  );
}

export default App;
