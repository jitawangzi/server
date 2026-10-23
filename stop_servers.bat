@echo off
chcp 65001 > nul
title 停止游戏服务器

echo 正在停止 GameServer 和 LoginServer...

powershell -Command "$procs = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*game_test*' -or $_.CommandLine -like '*login_test*' }; if ($procs) { foreach ($p in $procs) { Stop-Process -Id $p.ProcessId -Force -ErrorAction SilentlyContinue; Write-Host 'Stopped process' $p.ProcessId } }"

echo 服务已停止。
pause
