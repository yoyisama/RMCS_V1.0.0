<#
    RMCS 测阻机控制系统 —— 一键打包 Windows 可执行程序（免装 JDK 运行）

    用法：
        powershell -ExecutionPolicy Bypass -File .\build-exe.ps1

    产物：
        target\package\RMCS\RMCS.exe          绿色版（整个 RMCS 文件夹可拷贝到任意电脑直接运行）
        target\package\RMCS-1.0.0.exe         安装包（需本机已安装 WiX Toolset，脚本会自动尝试）

    前置条件：
        JDK 17 或更高版本（jpackage 需要 JDK 14+）
        Maven
#>

param(
    [string]$JdkHome = "",
    [string]$MvnCmd  = "",
    [switch]$SkipInstaller
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$AppName     = "RMCS"

# ---------- 1. 查找 JDK ----------
function Resolve-Jdk {
    param([string]$Hint)
    $candidates = @()
    if ($Hint) { $candidates += $Hint }
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
    $candidates += @(
        "D:\tools\jdk-17\Java\jdk-17.0.4",
        "C:\Program Files\Java\jdk-17*",
        "C:\Program Files\Java\jdk-21*",
        "C:\Program Files\Eclipse Adoptium\jdk-17*",
        "C:\Program Files\Microsoft\jdk-17*",
        "C:\Program Files\Zulu\zulu-17*"
    )
    $paths = @()
    foreach ($dir in $candidates) {
        if (Test-Path $dir) {
            $item = Get-Item $dir -ErrorAction SilentlyContinue
            if ($item -and -not $item.PSIsContainer) { continue }
            $paths += $item.FullName
        }
        Get-ChildItem $dir -Directory -ErrorAction SilentlyContinue | ForEach-Object { $paths += $_.FullName }
    }
    foreach ($path in ($paths | Select-Object -Unique)) {
        $exe = Join-Path $path "bin\jpackage.exe"
        if (Test-Path $exe) {
            $ver = "unknown"
            $releaseFile = Join-Path $path "release"
            if (Test-Path $releaseFile) {
                $line = Select-String -Path $releaseFile -Pattern '^JAVA_VERSION=' -ErrorAction SilentlyContinue |
                        Select-Object -First 1
                if ($line) { $ver = ($line.Line -split '=', 2)[1].Trim('"') }
            }
            Write-Host "[JDK] 使用 $path (Java $ver)" -ForegroundColor Cyan
            return $path
        }
    }
    throw "未找到 JDK 17+（jpackage.exe）。请安装 JDK 17 或通过 -JdkHome 指定路径。"
}

# ---------- 2. 查找 Maven ----------
function Resolve-Maven {
    param([string]$Hint)
    if ($Hint) { return $Hint }
    $sys = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($sys) { return $sys.Source }
    $local = "D:\tools\apache-maven-3.9.9\bin\mvn.cmd"
    if (Test-Path $local) { return $local }
    throw "未找到 Maven。请安装 Maven 或通过 -MvnCmd 指定 mvn.cmd 路径。"
}

$Jdk     = Resolve-Jdk -Hint $JdkHome
$Maven   = Resolve-Maven -Hint $MvnCmd
$env:JAVA_HOME = $Jdk
$env:Path = (Join-Path $Jdk "bin") + ";" + (Split-Path $Maven) + ";" + $env:Path

# ---------- 3. 编译并打成单个 jar ----------
Write-Host "`n[1/3] 编译打包 (mvn clean package) ..." -ForegroundColor Yellow
$ErrorActionPreference = "Continue"   # 外部命令写 stderr 时不中断，改由 $LASTEXITCODE 判断
Push-Location $ProjectRoot
& $Maven clean package -q
if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Maven 构建失败" }
$fatJar = Join-Path $ProjectRoot "target\dist\$AppName.jar"
if (-not (Test-Path $fatJar)) { Pop-Location; throw "未生成 $fatJar" }
Write-Host "      -> $fatJar" -ForegroundColor Green

# ---------- 4. 生成绿色版 app-image ----------
Write-Host "`n[2/3] 生成绿色版目录 (jpackage --type app-image) ..." -ForegroundColor Yellow
$destDir = Join-Path $ProjectRoot "target\package"
if (Test-Path $destDir) { Remove-Item $destDir -Recurse -Force }

& (Join-Path $Jdk "bin\jpackage.exe") `
    --type app-image `
    --name $AppName `
    --app-version 1.0.0 `
    --input (Join-Path $ProjectRoot "target\dist") `
    --main-jar "$AppName.jar" `
    --main-class com.rmcs.Launcher `
    --add-modules java.base,java.datatransfer,java.scripting,java.desktop,java.logging,java.xml,java.prefs,jdk.unsupported `
    --java-options "-Dfile.encoding=UTF-8" `
    --dest $destDir

if ($LASTEXITCODE -ne 0) { Pop-Location; throw "jpackage 生成 app-image 失败" }
Write-Host "      -> $(Join-Path $destDir "$AppName\$AppName.exe")" -ForegroundColor Green

# ---------- 5. 生成安装包（可选，需要 WiX） ----------
if (-not $SkipInstaller) {
    Write-Host "`n[3/3] 生成安装包 (jpackage --type exe，需要 WiX Toolset) ..." -ForegroundColor Yellow
    & (Join-Path $Jdk "bin\jpackage.exe") `
        --type exe `
        --name $AppName `
        --app-version 1.0.0 `
        --input (Join-Path $ProjectRoot "target\dist") `
        --main-jar "$AppName.jar" `
        --main-class com.rmcs.Launcher `
        --add-modules java.base,java.datatransfer,java.scripting,java.desktop,java.logging,java.xml,java.prefs,jdk.unsupported `
        --java-options "-Dfile.encoding=UTF-8" `
        --win-shortcut --win-menu --win-dir-chooser `
        --dest $destDir
    if ($LASTEXITCODE -ne 0) {
        Write-Host "      ! 安装包生成失败（多半是未安装 WiX Toolset），绿色版目录依然可用。" -ForegroundColor DarkYellow
    } else {
        Write-Host "      -> $(Join-Path $destDir "$AppName-1.0.0.exe")" -ForegroundColor Green
    }
}

Pop-Location
Write-Host "`n打包完成。" -ForegroundColor Green
