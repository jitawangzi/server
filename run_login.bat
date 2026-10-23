@echo off
chcp 65001 > nul
title LoginServer (login_test)
setlocal

set "JAVA_HOME=D:\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "workspace=D:\work-all\game\server"
set "metafolder=D:\work-all\game\configs\Server"

cd /d "D:\work-all\game\server\login"

echo ========================================================
echo   Starting LoginServer [login_test]...
echo ========================================================

"%JAVA_HOME%\bin\java.exe" ^
  -Dapp.id=login ^
  -Denv=dev ^
  -Dapollo.cluster=SYQ ^
  -Ddev_meta=http://test:9888 ^
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
  cn.game.login.LoginServer ^
  login_test

pause
