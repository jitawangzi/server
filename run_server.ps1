#requires -Version 7.0
<#
.SYNOPSIS
    本地运行入口。Prepare 构建独立制品；Start 显式确认测试数据范围；Stop 只操作本次登记的进程。
#>
[CmdletBinding()]
param(
    [ValidateSet('Prepare','Start','Stop','Status')][string]$Action='Prepare',
    [string]$Snapshot,
    [uri]$ApolloUri='http://127.0.0.1:9888',
    [string]$ApolloCluster='SYQ',
    [string]$GameId='game_test',
    [string]$LoginId='login_test',
    [int]$IdentityPort=17011,
    [switch]$ConfirmDataScope
)
& (Join-Path $PSScriptRoot '.ai-workspace/scripts/local-runtime.ps1') @PSBoundParameters
exit $LASTEXITCODE
