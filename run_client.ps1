<#
.SYNOPSIS
    Compiles and Runs a specific simulation client test case.
    Example: .\run_client.ps1 cn.game.simulation.test.EquipForgeTest
#>

# 1. Param 块必须放在第一位（注释除外）
param (
    [Parameter(Mandatory=$true, Position=0)]
    [string]$TestClass,
    [string]$ExpectedRuntime,
    [uri]$RuntimeIdentityUri,
    [switch]$RequireRuntimeIdentity,
    [string]$RuntimeSnapshot,
    [string]$TestAccount,
    [string]$LogicServerId,
    [string]$GameId,
    [uri]$LoginUrl
)

# 2. 设置编码移到 param 之后
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot '.ai-workspace/scripts/native-arguments.ps1')
if ([bool]$ExpectedRuntime -ne [bool]$RuntimeIdentityUri) { throw 'RUNTIME_ARGUMENTS_INCOMPLETE: ExpectedRuntime and RuntimeIdentityUri must be supplied together' }
if ($RequireRuntimeIdentity -and -not $ExpectedRuntime) { throw 'RUNTIME_IDENTITY_REQUIRED: configure the actual runtime observation endpoint and expected release manifest' }
if ($ExpectedRuntime -and -not $LogicServerId) { throw 'SCENARIO_LOGIC_SERVER_REQUIRED' }
$runtimeBefore=$null
if ($ExpectedRuntime) { . (Join-Path $PSScriptRoot '.ai-workspace/scripts/runtime-evidence.ps1') }
else { Write-Warning 'RUNTIME_UNVERIFIED: this run cannot establish the running server build or configuration' }
$ClientModule = "simulationclient"

# ================= 1. 自动编译 (Auto Compile) =================
Write-Host ">>> [Step 1] Compiling Client Module..." -ForegroundColor Cyan

# -pl: 只构建 simulationclient 模块
# -am: 同时构建它依赖的模块 (如果 core 改了也能生效)
# -DskipTests: 跳过单元测试，只编译代码
$compileCmd = "mvn compile process-resources -pl $ClientModule -am -DskipTests"
$copyDepsCmd = "mvn dependency:copy-dependencies -pl $ClientModule -DskipTests"
$LASTEXITCODE=0

# 执行编译，如果有错误直接停止
if (-not $RuntimeSnapshot) { cmd.exe /c $compileCmd }

if ($LASTEXITCODE -ne 0) {
    Write-Error ">>> COMPILATION FAILED! The AI generated code has syntax errors."
}

if (-not $RuntimeSnapshot) { cmd.exe /c $copyDepsCmd }

if ($LASTEXITCODE -ne 0) {
    Write-Error ">>> DEPENDENCY COPY FAILED! Please inspect the Maven logs above."
}

# ================= 2. 运行测试 (Run Java) =================
Write-Host ">>> [Step 2] Running Test: $TestClass" -ForegroundColor Cyan

# 构造 Classpath
# 包含当前模块的 classes，以及 lib 下的所有 jar
$TargetDir = "$ClientModule\target"
# 本轮 reactor 编译结果必须先于历史依赖 JAR，确保兄弟模块修改实际生效。
$ClassPath = ".;$TargetDir\classes;util\target\classes;core\target\classes;game\target\classes;protocol\target\classes;$TargetDir\lib\*;$TargetDir\dependency\*"
if ($RuntimeSnapshot) {
    $snapshot=(Resolve-Path -LiteralPath $RuntimeSnapshot).Path
    $ClassPath=(@('simulationclient','util','core','game','protocol')|ForEach-Object { Join-Path $snapshot "artifacts/$_/classes" }) -join ';'
    $ClassPath+=';'+(Join-Path $snapshot 'artifacts/lib/*')
}

$javaArgs = @(
    "-cp", $ClassPath,
    $TestClass
)
if ($RuntimeSnapshot) { $javaArgs=@(('-DSEVER_PATH='+(Join-Path $snapshot 'work/client')))+$javaArgs }

# 编译完成后观察目标；测试结束再次读取，防止沿用旧观察或忽略中途重启。
if ($ExpectedRuntime) {
    $runtimeBefore=Get-RuntimeIdentityObservation -ExpectedPath $ExpectedRuntime -IdentityUri $RuntimeIdentityUri
    Write-Output ('RUNTIME_BEFORE_OBSERVATION: '+($runtimeBefore|ConvertTo-Json -Depth 8 -Compress))
    $scenarioRunId=[guid]::NewGuid().ToString('N')
    $scenarioDirectory=Join-Path ([IO.Path]::GetTempPath()) ('runtime-scenario-'+$scenarioRunId)
    [IO.Directory]::CreateDirectory($scenarioDirectory)|Out-Null
    $scenarioOutput=Join-Path $scenarioDirectory 'result.json'
    $javaArgs=@(('-Dscenario.runtime.expected='+((Resolve-Path -LiteralPath $ExpectedRuntime).Path)),
        ('-Dscenario.runtime.instance='+$runtimeBefore.identity.instanceId),
        ('-Dscenario.runtime.runId='+$scenarioRunId),('-Dscenario.runtime.output='+$scenarioOutput))+$javaArgs
}
if ($TestAccount) { $javaArgs=@(('-Dscenario.account='+$TestAccount))+$javaArgs }
if ($LogicServerId) { $javaArgs=@(('-Dscenario.logicServerId='+$LogicServerId))+$javaArgs }
if ($GameId) { $javaArgs=@(('-Dscenario.gameId='+$GameId))+$javaArgs }
if ($LoginUrl) { $javaArgs=@(('-Dscenario.loginUrl='+$LoginUrl.AbsoluteUri.TrimEnd('/')))+$javaArgs }

# 启动 Java 进程
# Wait: 脚本必须等待测试跑完
# NoNewWindow: 直接在当前控制台输出日志，方便 AI 读取
$process=$null; $launchFailure=$null
try {
    $process = Start-Process -FilePath "java" -ArgumentList @($javaArgs|ForEach-Object { ConvertTo-NativeArgument $_ }) -NoNewWindow -PassThru -Wait
} catch { $launchFailure=$_ }
$processExit=if ($process) { $process.ExitCode } else { -1 }

Write-Output ("SCENARIO_PROCESS_EXIT: "+$processExit)
if ($runtimeBefore) {
    $runtimeAfter=$null; $observationStatus='FAILED'; $scenarioEvidence=$null; $scenarioRouting='FAILED'
    try {
        # 先净化保存现有场景报告，即使 HTTP 后观察失败也不能丢失前后探针。
        $scenarioEvidence=Read-ScenarioRuntimeEvidence -Path $scenarioOutput -RunId $scenarioRunId
        if ($launchFailure) { throw $launchFailure }
        $runtimeAfter=Get-RuntimeIdentityObservation -ExpectedPath $ExpectedRuntime -IdentityUri $RuntimeIdentityUri
        Assert-RuntimeIdentityStable $runtimeBefore $runtimeAfter
        $observationStatus='OBSERVED_MATCH'
        $null=Assert-ScenarioRuntimeEvidence -Path $scenarioOutput -RunId $scenarioRunId -Observation $runtimeBefore -ExitCode $processExit -LogicServerId $LogicServerId
        $scenarioRouting='AUTHENTICATED_GAME_NODE_PROBES_MATCH'
    } finally {
        # 后观测失败也保存之前观察和场景退出码；原异常继续向外传播，不能输出通过。
        $report=[ordered]@{schemaVersion='1.0';testClass=$TestClass;exitCode=$processExit;runtimeStatus=$observationStatus;before=$runtimeBefore;after=$runtimeAfter;expectedLogicServerId=$LogicServerId;scenarioRouting=$scenarioRouting;scenario=$scenarioEvidence}
        Write-Output ('RUNTIME_OBSERVATION: '+($report|ConvertTo-Json -Depth 12 -Compress))
        if (Test-Path -LiteralPath $scenarioOutput) { Remove-Item -LiteralPath $scenarioOutput -Force }
        if (Test-Path -LiteralPath $scenarioDirectory) { [IO.Directory]::Delete($scenarioDirectory) }
    }
}
if ($launchFailure) { throw $launchFailure }

# ================= 3. 结果判断 (Check Exit Code) =================
if ($processExit -eq 0) {
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "   SCENARIO_PROCESS_PASS: $TestClass (runtime identity and routing scope are reported separately)" -ForegroundColor Green
    Write-Host "========================================"
    exit 0
} else {
    Write-Host "`n========================================" -ForegroundColor Red
    Write-Host "   TEST FAILED: $TestClass" -ForegroundColor Red
    Write-Host "   Exit Code: $processExit" -ForegroundColor Red
    Write-Host "========================================"
    # 告诉 AI 去检查上面的日志
    Write-Error "Test execution failed. Please analyze the logs above."
}
