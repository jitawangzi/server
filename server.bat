:: 设置控制台字符集为UTF-8
chcp 65001 > nul

@echo off
PowerShell.exe -ExecutionPolicy Bypass -File "run_server.ps1"
