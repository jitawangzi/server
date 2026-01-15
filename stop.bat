@echo off
echo Stopping servers using JPS...

:: 遍历 jps 输出，查找包含指定关键词的 PID 并终止
for /f "tokens=1" %%i in ('jps -l ^| findstr "GameServer"') do taskkill /F /PID %%i
for /f "tokens=1" %%i in ('jps -l ^| findstr "CrossServer"') do taskkill /F /PID %%i
for /f "tokens=1" %%i in ('jps -l ^| findstr "LoginServer"') do taskkill /F /PID %%i

echo Done.
pause