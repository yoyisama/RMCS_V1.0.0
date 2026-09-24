export type FlowStep = 0 | 1 | 2 | 3 | 4 | 5 | 6;

export interface LogEntry {
  id: string;
  time: string;
  type: 'info' | 'plc' | 'com' | 'success' | 'warning' | 'error';
  message: string;
}

export interface TelemetryData {
  flag: number; // DB36.258.0 (0-1000)
  position: number; // DB36.260.0 (mm)
  resistance: number; // COM4 (Ω)
  timestamp: number;
}

export interface TestStandard {
  standardValue: number; // 标准值 e.g. 6.532
  upperDev: number;      // 上偏差 e.g. 0.01
  lowerDev: number;      // 下偏差 e.g. 0.01
}

export interface InspectionRecord {
  id: string;
  row: number; // 行号: 1 ~ 7
  position: 'L' | 'M' | 'R'; // 位置: 左区(L), 中区(M), 右区(R)
  measuredValue: number; // 测量值 e.g. 3.412
  standardValue: number; // 标准值 e.g. 6.532
  result: boolean; // True (合格) / False (超差)
  time: string; // 检测时间 e.g. 2024/4/25 19:49
}

export type MatrixState = Record<string, { value: number | null; result: boolean | null; isTesting?: boolean }>;

export interface ProductionStatus {
  completedCount: number; // 完成数量 e.g. 8,921
  status: '运行中' | '待机' | '已停止' | '复位中';
  runDurationSeconds: number; // 运行时长 (秒)
}

export interface SystemConfig {
  standard: TestStandard;
  comPort: string;
  baudRate: number;
  plcIp: string;
  simIntervalMs: number; // 查询间隔 (毫秒)
  isAutoFetch: boolean;  // 自动获取数据开关
}

export interface AuthUser {
  username: string;
  name: string;
  role: string;
  isLoggedIn: boolean;
}

export type ThemeMode = 'dark' | 'light';
