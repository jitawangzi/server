#requires -Version 7.0
<# 显式逐单只读预检；退出1非法输入、2局部一致但外部阻断、3局部证据阻断。没有删除/补发入口。 #>
param(
    [Parameter(Mandatory)][string]$PlayerId,
    [Parameter(Mandatory)][string]$OrderId,
    [Parameter(Mandatory)][string]$ArchiveRoot,
    [Parameter(Mandatory)][string]$KeyFile
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & java '-Dlog4j.configurationFile=game/target/classes/payment-evidence-log4j2.xml' -cp 'game/target/classes;core/target/classes;protocol/target/classes;util/target/classes;game/target/lib/*' cn.game.games.diagnostics.PaymentCleanupCli $PlayerId $OrderId $ArchiveRoot $KeyFile
    exit $LASTEXITCODE
} finally { Pop-Location }
