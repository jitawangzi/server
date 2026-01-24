<#
.SYNOPSIS
    Compiles and Runs a specific simulation client test case.
    Example: .\run_client.ps1 cn.game.simulation.test.EquipForgeTest
#>

# 1. Param 块必须放在第一位（注释除外）
param (
    [Parameter(Mandatory=$true, Position=0)]
    [string]$TestClass
)

# 2. 设置编码移到 param 之后
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$ErrorActionPreference = "Stop"
$ClientModule = "simulationclient"

# ================= 1. 自动编译 (Auto Compile) =================
Write-Host ">>> [Step 1] Compiling Client Module..." -ForegroundColor Cyan

# -pl: 只构建 simulationclient 模块
# -am: 同时构建它依赖的模块 (如果 core 改了也能生效)
# -DskipTests: 跳过单元测试，只编译代码
$compileCmd = "mvn compile process-resources -pl $ClientModule -am -DskipTests"
$copyDepsCmd = "mvn dependency:copy-dependencies -pl $ClientModule -DskipTests"

# 执行编译，如果有错误直接停止
cmd.exe /c $compileCmd

if ($LASTEXITCODE -ne 0) {
    Write-Error ">>> COMPILATION FAILED! The AI generated code has syntax errors."
}

cmd.exe /c $copyDepsCmd

if ($LASTEXITCODE -ne 0) {
    Write-Error ">>> DEPENDENCY COPY FAILED! Please inspect the Maven logs above."
}

# ================= 2. 运行测试 (Run Java) =================
Write-Host ">>> [Step 2] Running Test: $TestClass" -ForegroundColor Cyan

# 构造 Classpath
# 包含当前模块的 classes，以及 lib 下的所有 jar
$TargetDir = "$ClientModule\target"
$ClassPath = ".;$TargetDir\classes;$TargetDir\lib\*;$TargetDir\dependency\*"

# 如果你的 simulationclient 依赖了其他兄弟模块(core, game)的源码，
# 且不想打成jar包，可以把它们的 classes 目录也加进来，例如：
$ClassPath += ";core\target\classes;game\target\classes;protocol\target\classes"

$javaArgs = @(
    "-cp", $ClassPath,
    $TestClass
)

# 启动 Java 进程
# Wait: 脚本必须等待测试跑完
# NoNewWindow: 直接在当前控制台输出日志，方便 AI 读取
$process = Start-Process -FilePath "java" `
    -ArgumentList $javaArgs `
    -NoNewWindow `
    -PassThru `
    -Wait

# ================= 3. 结果判断 (Check Exit Code) =================
if ($process.ExitCode -eq 0) {
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "   TEST PASSED: $TestClass" -ForegroundColor Green
    Write-Host "========================================"
    exit 0
} else {
    Write-Host "`n========================================" -ForegroundColor Red
    Write-Host "   TEST FAILED: $TestClass" -ForegroundColor Red
    Write-Host "   Exit Code: $($process.ExitCode)" -ForegroundColor Red
    Write-Host "========================================"
    # 告诉 AI 去检查上面的日志
    Write-Error "Test execution failed. Please analyze the logs above."
}