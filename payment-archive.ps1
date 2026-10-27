#requires -Version 7.0
<# 本地单订单证据封存/验证；密钥为归档根之外的私有32字节文件。先编译当前类，不启动服务器。 #>
param(
    [Parameter(Mandatory)][ValidateSet('archive','verify')][string]$Operation,
    [Parameter(Mandatory)][string]$PlayerId,
    [Parameter(Mandatory)][string]$OrderId,
    [Parameter(Mandatory)][string]$OutputRoot,
    [Parameter(Mandatory)][string]$KeyFile
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & java '-Dlog4j.configurationFile=game/target/classes/payment-evidence-log4j2.xml' -cp 'game/target/classes;core/target/classes;protocol/target/classes;util/target/classes;game/target/lib/*' cn.game.games.diagnostics.PaymentArchiveCli $Operation $PlayerId $OrderId $OutputRoot $KeyFile
    exit $LASTEXITCODE
} finally { Pop-Location }
