@echo off
setlocal

set "src=%metafolder:\=\\%\ClientSetting"
set "dst=%metafolder:\=\\%\..\client\Projectx"

:: 解析 .. 为实际路径
for %%A in ("%dst%") do set "dst=%%~fA"

:: 确保目标目录存在
if not exist "%dst%" (
    md "%dst%"
)

:: 递归复制，强制覆盖，无需确认
xcopy "%src%\*" "%dst%\" /E /H /Y /C /R

echo 完成！
pause