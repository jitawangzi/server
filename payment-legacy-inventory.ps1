#requires -Version 7.0
<# 显式清单只读盘点；不发现账号，不写数据库。0仅表示本页完成，1参数错误，2读取不完整。 #>
param(
    [Parameter(Mandatory)][string]$Pairs,
    [Parameter(Mandatory)][ValidateRange(1,10)][int]$PageSize,
    [Parameter(Mandatory)][string]$Cursor
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $logUri = ([uri](Join-Path $PSScriptRoot 'game/target/classes/payment-evidence-log4j2.xml')).AbsoluteUri
    & java "-Dlog4j.configurationFile=$logUri" -cp 'game/target/classes;core/target/classes;protocol/target/classes;util/target/classes;game/target/lib/*' cn.game.games.diagnostics.PaymentLegacyInventoryCli $Pairs $PageSize $Cursor
    exit $LASTEXITCODE
} finally { Pop-Location }
