@echo off
chcp 936 > nul

cd /D D:\src\First_party\program\tools\Proto

git pull

if %errorlevel% neq 0 (
  echo.
  echo git pull失败，手动处理后重试
  echo.
  pause
  goto :eof
)

git add all.proto ProtosMessageID.ts ProtosMessageName.ts


cd /D D:\src\First_party\program\tools\Excels

git pull

git add ErrorMsgEnum.xlsx

git commit -m "update proto"
setlocal enabledelayedexpansion

REM 推送到远程仓库
 for /f "tokens=*" %%i in ('git rev-parse --abbrev-ref HEAD') do set currentBranch=%%i

git push origin !currentBranch!

endlocal
pause
