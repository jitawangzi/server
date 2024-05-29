@echo off
cd /D D:\src\First_party\program\tools\Proto

git pull

git add all.proto ProtosMessageID.ts ProtosMessageName.ts


cd /D D:\src\First_party\program\tools\Excels

git pull

git add ErrorMsgEnum.xlsx

git commit -m "update proto"
REM 推送到远程仓库
git push origin Branch_West

pause
