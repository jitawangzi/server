@echo off
chcp 65001 > nul
title CrossServer (cross_test)
setlocal

set "JAVA_HOME=D:\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "workspace=D:\work-all\game\server"
set "metafolder=D:\work-all\game\configs\Server"
if "%APOLLO_META%"=="" set "APOLLO_META=http://127.0.0.1:9888"

cd /d "D:\work-all\game\server\game"

echo ========================================================
echo   Starting CrossServer [cross_test]...
echo ========================================================

"%JAVA_HOME%\bin\java.exe" ^
  -Dapp.id=cross ^
  -Denv=dev ^
  -Dapollo.cluster=SYQ ^
  -Dapollo.configService=%APOLLO_META% ^
  -Ddev_meta=%APOLLO_META% ^
  -DdefaultNetworkIp=127.0.0.1 ^
  --add-opens=java.base/java.lang=ALL-UNNAMED ^
  --add-opens=java.base/java.util=ALL-UNNAMED ^
  -Dio.netty.tryReflectionSetAccessible=true ^
  -Dfile.encoding=utf-8 ^
  -Dserver.run.mode=test ^
  -Djdk.attach.allowAttachSelf=true ^
  -Dzookeeper.clientCnxnSocket=org.apache.zookeeper.ClientCnxnSocketNetty ^
  -Dworkspace="%workspace%" ^
  -Dmetafolder="%metafolder%" ^
  -cp ".;target\classes;target\lib\*" ^
  cn.game.games.net.cross.CrossServer ^
  cross_test

pause
