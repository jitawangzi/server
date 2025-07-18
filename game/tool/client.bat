@echo off
setlocal

set "src=%metafolder%\..\ClientSetting"
set "dst=%metafolder%\..\..\client\Projectx"

REM 解析 .. 为实际路径
for %%A in ("%src%") do set "src=%%~fA"
for %%A in ("%dst%") do set "dst=%%~fA"

echo 源路径: %src%
echo 目标路径: %dst%

if not exist "%src%" (
    echo 源目录不存在: %src%
    pause
    exit /b
)

if not exist "%dst%" (
    md "%dst%"
)

xcopy "%src%\*" "%dst%\" /E /H /Y /C /R

echo 完成！
pause