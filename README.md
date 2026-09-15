# RMCS 自动测阻机控制系统

> RMCS（Resistance Measuring & Control System）——面向工业现场的电阻测量与设备控制上位机软件。
> 提供作业可视化、数据履历、参数设定、设备启停 / 复位等一体化的图形化操作界面，支持 Windows 桌面单机运行、绿色免安装分发。

- **当前版本**：1.0.0
- **运行平台**：Windows 10 / 11（64 位，绿色版无需额外安装 JDK）
- **开发语言**：Java 17 + JavaFX 17
- **构建工具**：Maven 3.x
- **打包工具**：JDK 17 自带 `jpackage`

---

## 一、项目简介与功能清单

### 1.1 项目简介

RMCS 是为电阻类测量 / 控制设备打造的上位机软件。系统采用经典「登录 → 主控台」两段式流程：登录后进入主控台，主控台以工控友好的深色面板风格呈现「执行作业 / 数据履历 / 设备状态 / 人员操作」四大区域，方便操作员快速掌握生产节拍、完成参数下发、查询历史记录或导出数据。

设计原则：

- **界面即文档**：所有面板采用统一的 `panel-title` 配色与边框，按钮按用途使用 6 种固定语义色（红 / 灰 / 绿 / 蓝 / 橙 / 紫）。
- **绿色可拷贝**：所有依赖（含 JDK 运行时）一并打包，单文件夹即拷即用，无需目标机器安装任何软件。
- **可扩展**：界面与业务控制器分离（FXML + Controller），方便后续接入真实 PLC / 电阻计协议。

### 1.2 功能清单

| 模块 | 功能 | 说明 |
|---|---|---|
| **登录** | 用户登录 | 默认账号 `admin / 123456`，登录窗口按屏幕可视区域自适应缩放 |
| **顶部状态栏** | 当前模式 / 当前用户 / 全屏切换 / 电源退出 | 全屏按钮或 `F11` 切换；电源键弹出二次确认 |
| **执行作业** | 7 行 × 3 区（`L` / `M` / `R`）实时测量值显示 | 表头 30 px，行号固定在左侧，单元格 `28 px` 高度，网格按行列百分比填满面板 |
| **数据履历** | 历史测量记录表格 | 字段：行号 / 位置 / 测量值 / 标准值 / 结果 / 检测时间，奇行底色交替，选中高亮 |
| **动作履历** | 系统 / 操作日志 | 时间戳 + 文本，支持滚动查看，永不丢失关键动作记录 |
| **测定标准** | 标准值 + 上 / 下偏差 | 右侧「参数设定」入口（占位，按钮已通栏） |
| **运行状况** | 完成数量 / 设备运行状况 / 运行时长 | 「清零」按钮可重置统计；显示已连接设备状态 |
| **人员操作** | 设备停止 / 复位 / 启动 / 履历查询 / 日志查询 / 导出数据 | 6 个大按钮各占一半面板，色彩按危险等级区分 |
| **数据导出** | 数据履历 → CSV | 点击「导出数据」打开系统保存对话框，文件名带时间戳，UTF-8 + BOM，Excel 直接可读 |
| **底部状态栏** | PLC 通讯状态 / 电阻计通讯状态 / 版本号 / 当前日期 | 日期实时刷新 |

---

## 二、技术方案与架构

### 2.1 技术栈

| 类别 | 选型 | 用途 |
|---|---|---|
| JDK | Java 17 | 编译器 + 运行时 + `jpackage` 打包 |
| GUI 框架 | JavaFX 17（`javafx-controls` / `javafx-fxml`） | 桌面 UI |
| 样式 | JavaFX CSS | 主题与控件美化 |
| 构建 | Maven 3.x | 依赖管理、编译、测试 |
| 单包 | `maven-shade-plugin` 3.5.1 | 编译产物打成单一 fat-jar |
| 桌面打包 | `jpackage`（JDK 自带） | 把 fat-jar 打成 Windows `.exe`（绿色版 / 安装包） |
| 图表 / 报表 | — | 当前版本无图表组件（占位面板即可满足 1.0） |

### 2.2 模块结构

```
src/
├── main/
│   ├── java/com/rmcs/
│   │   ├── App.java                 # Application 子类，初始化首屏为登录页
│   │   ├── Launcher.java            # jpackage 入口壳（绕过 Application 模块限制）
│   │   ├── SmokeTest.java           # FXML / 控制器绑定冒烟验证工具
│   │   └── controller/
│   │       ├── LoginController.java # 登录控制器
│   │       └── MainController.java  # 主控台控制器（含数据导出、设备状态等业务）
│   └── resources/
│       ├── fxml/
│       │   ├── login.fxml           # 登录界面
│       │   └── main.fxml            # 主控台界面（执行作业 / 数据履历 / 操作按钮）
│       └── css/
│           ├── login.css
│           └── main.css             # 全局样式（面板、表格、按钮、状态栏）
└── test/
```

### 2.3 运行架构

```
                ┌────────────────────────────────────┐
                │       RMCS.exe（jpackage 启动）      │
                └──────────────┬─────────────────────┘
                               │  java → com.rmcs.Launcher
                ┌──────────────▼─────────────────────┐
                │   Application.launch(App.class)    │
                └──────────────┬─────────────────────┘
                               │
                  ┌────────────┴────────────┐
                  ▼                         ▼
           ┌─────────────┐           ┌─────────────┐
           │ login.fxml  │  → 登录    │ main.fxml   │
           │ LoginCtrl   │   成功     │ MainCtrl    │
           └─────────────┘  跳转 →    └─────────────┘
                                        │
        ┌───────────────────────────────┼───────────────────────────────┐
        ▼               ▼               ▼                ▼                ▼
   作业表格        数据履历表      状态 / 标准 /      人员操作 6按钮      实时时钟 / 通讯状态
  （GridPane）    （TableView）     运行状况         （含 CSV 导出）
```

- **视图与逻辑分离**：每个 FXML 绑定一个 `*Controller`（实现 `Initializable`）。控件注入使用 `@FXML`，业务事件用 `@FXML onAction=#xxx`。
- **数据模型**：`MainController.MeasureRecord`（内部静态类）承载履历数据，使用 `StringProperty` 方便与 `TableView` 绑定。
- **国际化 / 字符集**：全部资源为 UTF-8；导出 CSV 时显式写 `U+FEFF` BOM，Excel 打开不乱码。
- **布局策略**：主控台采用三列两行 `GridPane`，列宽 420 / 自适应 / 226，行高 62% / 38%，确保不同分辨率下面板均衡。
- **无侵入式升级**：CSS 与 FXML 与控制器完全解耦，调整外观只改 `.css` / `.fxml`，无需改 Java 代码。

### 2.4 关键依赖版本

```xml
<javafx.version>17.0.2</javafx.version>
<maven.compiler.source>17</maven.compiler.source>
<maven-compiler-plugin>3.13.0</maven-compiler-plugin>
<maven-shade-plugin>3.5.1</maven-shade-plugin>
<javafx-maven-plugin>0.0.8</javafx-maven-plugin>
```

---

## 三、打包与发布

### 3.1 前置条件

| 工具 | 版本 | 用途 |
|---|---|---|
| JDK | 17 或更高 | 编译 + `jpackage` |
| Maven | 3.6+ | 构建 / shade |
| WiX Toolset | 3.0+（可选） | 仅当需要生成 `.exe` 安装包时 |

### 3.2 一键打包（推荐）

仓库根目录提供脚本 `build-exe.ps1`，自动检测 JDK / Maven 并依次执行：

```powershell
# 完整打包：绿色版 + 安装包（需 WiX）
powershell -ExecutionPolicy Bypass -File .\build-exe.ps1

# 仅打包绿色版目录（推荐日常迭代）
powershell -ExecutionPolicy Bypass -File .\build-exe.ps1 -SkipInstaller
```

执行流程：

1. `[1/3] mvn clean package` — 编译 + shade 成 `target\dist\RMCS.jar`
2. `[2/3] jpackage --type app-image` — 生成绿色版目录 `target\package\RMCS\`
3. `[3/3] jpackage --type exe` —（可选）生成安装包 `target\package\RMCS-1.0.0.exe`

产物清单（绿色版）：

```
target\package\RMCS\
├── RMCS.exe          # 主程序（约 0.4 MB）
├── RMCS.ico          # 图标
├── app\              # fat-jar 及运行时依赖
└── runtime\          # JDK 17 精简运行时
```

### 3.3 手动打包

如需在 CI 环境精确控制：

```powershell
# 1. 编译并生成 fat-jar
mvn clean package

# 2. 生成绿色版目录
jpackage ^
    --type app-image ^
    --name RMCS ^
    --app-version 1.0.0 ^
    --input target\dist ^
    --main-jar RMCS.jar ^
    --main-class com.rmcs.Launcher ^
    --add-modules java.base,java.datatransfer,java.scripting,java.desktop,java.logging,java.xml,java.prefs,jdk.unsupported ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --dest target\package

# 3.（可选）生成安装包，需已安装 WiX
jpackage --type exe --name RMCS --app-version 1.0.0 ^
    --input target\dist --main-jar RMCS.jar --main-class com.rmcs.Launcher ^
    --add-modules java.base,java.datatransfer,java.scripting,java.desktop,java.logging,java.xml,java.prefs,jdk.unsupported ^
    --win-shortcut --win-menu --win-dir-chooser ^
    --dest target\package
```

### 3.4 发布与分发

| 分发形态 | 路径 | 适用场景 |
|---|---|---|
| **绿色版** | 整个 `target\package\RMCS\` 文件夹 | U 盘拷贝、临时产线机、内网共享 |
| **安装包** | `target\package\RMCS-1.0.0.exe` | 正式部署到工控机（含开始菜单 / 桌面快捷方式） |

发布检查清单：

1. 双击 `RMCS.exe` 能正常弹出登录窗口。
2. 默认账号 `admin / 123456` 可登录。
3. 主控台 6 个面板正常渲染，数据履历表头不换行，按钮文字居中。
4. 点击「导出数据」可保存 CSV，Excel 打开无乱码。
5. 全屏按钮 / `F11` 能正常切换。

### 3.5 开发期调试

仓库内置 `SmokeTest.java`，用于在不打开主窗口的前提下快速验证 FXML 加载与控制器绑定：

```powershell
mvn -q dependency:build-classpath -Dmdep.outputFile=target/cp.txt
$cp = "target\classes;" + (Get-Content target\cp.txt)
$jfx = (Get-Content target/cp.txt) | Where-Object { $_ -match "openjfx" } | Select-Object -Unique -join ";"
java --module-path $jfx --add-modules javafx.controls,javafx.fxml -cp $cp com.rmcs.SmokeTest
```

成功时输出 `SMOKE_TEST_OK`。

---

## 四、目录约定与命名规范

- Java 包：`com.rmcs.*`
- 视图：`*.fxml` 与对应 `*Controller.java` 同包，前缀名一致。
- 样式：所有样式集中在 `resources\css\`，按页面拆分（`login.css` / `main.css`）。
- 日志：界面层日志通过 `appendLog()` 写入「动作履历」面板；持久化日志留待后续接入业务时引入日志框架（`log4j2` / `slf4j`）。

## 五、许可与版本

当前版本 **1.0.0**。未经授权，请勿用于商业用途。

— END —