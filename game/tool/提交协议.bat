@echo off
cd /D %metafolder%\..\Proto

git pull

git add all.proto ProtosMessageID.ts ProtosMessageName.ts
git commit -m "更新proto文件"
REM 推送到远程仓库
git push origin main

pause
