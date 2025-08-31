@echo off
setlocal

set "src=D:\src\First_party\program\tools\ClientSetting"
set "dst=D:\src\First_party\program\client\Projectx"

:: 确保目标目录存在
if not exist "%dst%" (
    md "%dst%"
)

:: 递归复制，强制覆盖，无需确认
xcopy "%src%\*" "%dst%\" /E /H /Y /C /R

echo 完成！
pause