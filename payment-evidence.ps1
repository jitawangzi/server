#requires -Version 7.0
<# 本地只读核对：先编译当前 game 及依赖，并提供受限 S2_DB_CONFIG；不启动服务。 #>
param([Parameter(Mandatory)][string]$PlayerId, [Parameter(Mandatory)][string]$OrderId)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    # 使用当前编译类；lib 仅提供第三方依赖，不使用历史 game.jar。
    & java '-Dlog4j.configurationFile=game/target/classes/payment-evidence-log4j2.xml' -cp 'game/target/classes;core/target/classes;protocol/target/classes;util/target/classes;game/target/lib/*' cn.game.games.diagnostics.PaymentEvidenceCli $PlayerId $OrderId
    exit $LASTEXITCODE
} finally { Pop-Location }
