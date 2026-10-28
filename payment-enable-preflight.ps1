#requires -Version 7.0
<# 只读支付启用结构预检；退出2仍缺外部部署证明，不能用于自动启用或清理。 #>
param()
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    if (-not (Test-Path -LiteralPath 'game/target/classes/cn/game/games/diagnostics/PaymentEnablementCli.class') -or
        -not (Test-Path -LiteralPath 'game/target/lib' -PathType Container)) {
        @{ status='BLOCKED_RUNTIME'; localSchemaReady=$false; canEnable=$false; canDelete=$false; canResend=$false;
           localBlockers=@('CURRENT_CLASSES_AND_RUNTIME_DEPENDENCIES_REQUIRED');
           nextAction='COMPILE_CURRENT_REACTOR_AND_PREPARE_GAME_TARGET_LIB_AS_DOCUMENTED' } | ConvertTo-Json -Compress
        exit 3
    }
    $logConfig = ([uri](Join-Path $PSScriptRoot 'game/target/classes/payment-evidence-log4j2.xml')).AbsoluteUri
    & java "-Dlog4j.configurationFile=$logConfig" -cp 'game/target/classes;core/target/classes;protocol/target/classes;util/target/classes;game/target/lib/*' cn.game.games.diagnostics.PaymentEnablementCli @args
    exit $LASTEXITCODE
} finally { Pop-Location }
