[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
<#
.SYNOPSIS
    Automated Build & Launch Script for Game Server (Windows/AI Agent Optimized)
    1. Kills old processes based on unique arguments.
    2. Builds using Maven.
    3. Starts Game, Cross, and Login servers in parallel.
    4. Monitors stderr logs for "Server startup complete".
#>

$ErrorActionPreference = "Stop"

# ================= Configuration =================

# 1. Global JVM Args
$GlobalJvmArgs = @(
    "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=oom.dump",
    "-Xmx4g", "-Xms256m", "-Xss256k",
    "-XX:MaxDirectMemorySize=256m",
    "-XX:+UseG1GC", "-XX:+UseCompressedOops", "-XX:+UseCompressedClassPointers",
    "-XX:+SegmentedCodeCache",
    "-verbose:gc",
    "-XX:+PrintCommandLineFlags",
    "-XX:+ExplicitGCInvokesConcurrent",
    "-Xlog:gc*,safepoint:gc.log:time,uptime:filecount=100,filesize=50M",
    "-Djdk.attach.allowAttachSelf=true",
    "-Dio.netty.tryReflectionSetAccessible=true",
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED"
)

# 2. Common Apollo & Module Args
$ApolloArgs = "-Dapp.id={0} -Denv=dev -Dapollo.cluster=default -Dapollo.meta=http://test:8080"
$RunModuleArg = "-Dserver.run.mode=test"

# 3. Service Definitions (paths are now relative to module directory)
$Services = @(
    @{
        Name = "GameServer"
        ModuleDir = "game"
        MainClass = "cn.game.games.net.game.GameServer"
        UniqueId = "game_test"
        DebugPort = "8011"
        LogDir = "logs"
        ErrLogFile = "logs/out.log"
        AppId = "game"
    },
    @{
        Name = "CrossServer"
        ModuleDir = "game"
        MainClass = "cn.game.games.net.cross.CrossServer"
        UniqueId = "cross_test"
        DebugPort = "8012"
        LogDir = "cross_logs"
        ErrLogFile = "cross_logs/out.log"
        AppId = "cross"
    },
    @{
        Name = "LoginServer"
        ModuleDir = "login"
        MainClass = "cn.game.login.LoginServer"
        UniqueId = "login_test"
        DebugPort = "9490"
        LogDir = "logs"
        ErrLogFile = "logs/out.log"
        AppId = "login"
    }
)

# Save project root directory
$ProjectRoot = Get-Location

# ================= Phase 1: Cleanup Old Processes =================
Write-Host ">>> [Phase 1] Cleaning up old processes..." -ForegroundColor Cyan

foreach ($svc in $Services) {
    # Find process by CommandLine containing the UniqueId (e.g., "game_test")
    $procs = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like "*$($svc.UniqueId)*" }
    
    if ($procs) {
        foreach ($p in $procs) {
            Write-Host "    Killing $($svc.Name) (PID: $($p.ProcessId))..." -ForegroundColor Yellow
            Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue
        }
    }
}
Start-Sleep -Seconds 2 # Wait for locks to release

# ================= Phase 2: Maven Build =================
Write-Host ">>> [Phase 2] Building Project..." -ForegroundColor Cyan

# Check if we are in the root
if (!(Test-Path "pom.xml")) {
    Write-Error "pom.xml not found! Please run this script from the project root."
}

# Run Maven (Compile + Copy Dependencies if needed)
# Added dependency:copy-dependencies to ensure target/lib exists as per your request
$mvnCmd = "mvn clean package -DskipTests"
cmd /c $mvnCmd

if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven Build Failed! Exiting."
}
Write-Host "Build Success." -ForegroundColor Green

# ================= Phase 3: Launch Services =================
Write-Host ">>> [Phase 3] Starting Services Parallelly..." -ForegroundColor Cyan

$StartedProcesses = @()

foreach ($svc in $Services) {
    # 1. Prepare Paths (relative to module directory)
    $modulePath = Resolve-Path (Join-Path $ProjectRoot $svc.ModuleDir)
    $logDir = Join-Path $modulePath ($svc.LogDir -replace '/', '\')
    $errLog = Join-Path $modulePath ($svc.ErrLogFile -replace '/', '\')
    
    # Create Log Directory if missing
    if (!(Test-Path $logDir)) { 
        New-Item -ItemType Directory -Force -Path $logDir | Out-Null 
    }
    
    # Clear old error log
    if (Test-Path $errLog) { 
        Clear-Content $errLog 
    }

    # 2. Construct Classpath (relative to module directory)
    $classPath = ".;.\target\classes;.\target\lib\*"

    # 3. Construct Arguments
    $debugArg = "-agentlib:jdwp=transport=dt_socket,address=*:$($svc.DebugPort),server=y,suspend=n"
    $apolloActual = $ApolloArgs -f $svc.AppId
    $logPathArg = "-DSEVER_PATH=./$($svc.LogDir)"
    
    # Assemble full argument list
    $javaArgs = @()
    $javaArgs += $debugArg
    $javaArgs += $GlobalJvmArgs
    $javaArgs += $RunModuleArg
    $javaArgs += $logPathArg
    $javaArgs += $apolloActual.Split(" ")
    $javaArgs += "-cp", $classPath
    $javaArgs += $svc.MainClass
    $javaArgs += $svc.UniqueId

    Write-Host "    Starting $($svc.Name) in directory: $modulePath"
    
    # 4. Start Process with WorkingDirectory set to module path
    $p = Start-Process -FilePath "java" `
        -ArgumentList $javaArgs `
        -WorkingDirectory $modulePath `
        -RedirectStandardError $errLog `
        -WindowStyle Hidden `
        -PassThru

    $StartedProcesses += @{ 
        Process = $p
        Config = $svc
        ErrLogPath = $errLog
        Finished = $false 
    }
}

# ================= Phase 4: Monitor (The Watchdog) =================
Write-Host ">>> [Phase 4] Monitoring Startup (Timeout: 60s)..." -ForegroundColor Cyan

$TimeoutSeconds = 60
$StartTime = Get-Date
$AllReady = $false

while ((Get-Date) -lt $StartTime.AddSeconds($TimeoutSeconds)) {
    $pending = 0
    
    foreach ($item in $StartedProcesses) {
        if ($item.Finished) { continue }

        # Check if process died
        if ($item.Process.HasExited) {
            Write-Error "Service $($item.Config.Name) crashed unexpectedly! Check $($item.ErrLogPath)"
        }

        # Check log file for success message
        if (Test-Path $item.ErrLogPath) {
            $logContent = Get-Content $item.ErrLogPath -Tail 20 -Encoding UTF8 -ErrorAction SilentlyContinue
            if ($logContent -match "Server startup complete") {
                Write-Host "    [SUCCESS] $($item.Config.Name) is ready." -ForegroundColor Green
                $item.Finished = $true
            } else {
                $pending++
            }
        } else {
            $pending++
        }
    }

    if ($pending -eq 0) {
        $AllReady = $true
        break
    }

    Start-Sleep -Seconds 2
    Write-Host "." -NoNewline -ForegroundColor Gray
}

Write-Host "" # Newline

if ($AllReady) {
    Write-Host ">>> ALL SERVICES STARTED SUCCESSFULLY!" -ForegroundColor Green
    Write-Host ">>> You can now run tests."
    exit 0
} else {
    Write-Host ">>> STARTUP TIMEOUT! Cleaning up..." -ForegroundColor Red
    # Kill the processes we just started
    foreach ($item in $StartedProcesses) {
        Stop-Process -Id $item.Process.Id -Force -ErrorAction SilentlyContinue
    }
    exit 1
}