@echo off
chcp 65001 > nul
title 启动游戏服务器 (Windows Terminal 多页签模式)

echo ======================================================================
echo   正在使用 Windows Terminal 启动游戏服务:
echo   1. GameServer (game_test, 端口 7011)
echo   2. LoginServer (login_test, 端口 9390)
echo ======================================================================

set "BASE=D:\work-all\game\server"

start wt.exe --title "GameServer" -d "%BASE%" cmd.exe /k "run_game.bat" ; nt --title "LoginServer" -d "%BASE%" cmd.exe /k "run_login.bat"

echo 已在 Windows Terminal 中拉起各个页签。
