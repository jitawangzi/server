@echo off
setlocal enabledelayedexpansion

:: Server Name
set "ServerName=GameServer"


:: jvm arguments
set JVM_ARGS=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=oom.dump ^
-Xmx4g ^
-Xms256m ^
-Xss256k ^
-XX:MaxDirectMemorySize=256m ^
-XX:+UseG1GC ^
-XX:+UseCompressedOops ^
-XX:+UseCompressedClassPointers ^
-XX:+SegmentedCodeCache ^
-verbose:gc ^
-XX:+PrintCommandLineFlags ^
-XX:+ExplicitGCInvokesConcurrent ^
-Djava.security.egd=file:/dev/./urandom ^
-Xlog:gc*,safepoint:gc.log:time,uptime:filecount=100,filesize=50M ^
--add-opens java.base/java.lang=ALL-UNNAMED ^
--add-opens java.base/java.util=ALL-UNNAMED -Dio.netty.tryReflectionSetAccessible=true

:: set DEBUG=-agentlib:jdwp=transport=dt_socket,address=*:8011,server=y,suspend=n
:: set PROFILER=-agentpath:/game/jprofiler/bin/linux-x64/libjprofilerti.so=port=8849,nowait

set "APP_CLASSPATH=-cp .;.\resources;.\lib\*;..\lib\*"
set "PROPERTY=-Djava.library.path=C:\usr\local\lib -Djdk.attach.allowAttachSelf=true"
:: set "JPROFILER=-agentpath:C:\server\jprofiler14.0.3\bin\windows-x64\jprofilerti.dll=port=8011,nowait,address=0.0.0.0"
set "APOLLO_ARGS=-Dapp.id=game -Denv=dev -Dapollo.cluster=SYQ -Dserver.run.mode=test -Ddev_meta=http://test:9888"
:: set RUN_MODULE=-Dserver.run.mode=test
:: set LOG_PATH=-DSEVER_PATH=.\logs
set "MAIN_CLASS=cn.game.games.net.game.GameServer"

:: application arguments
set "APP_ARGS=SYQ"


:start
echo begin start %ServerName% server....
echo %APP_ARGS%
:: 启动 java 服务（注意根据你的实际 JAVA_HOME 路径调整）
:: start /b "" java %JVM_ARGS% %JPROFILER% %PROPERTY% %APOLLO_ARGS% %APP_CLASSPATH% %MAIN_CLASS% %APP_ARGS% 
java %JVM_ARGS% %JPROFILER% %PROPERTY% %APOLLO_ARGS% %APP_CLASSPATH% %MAIN_CLASS% %APP_ARGS% 

endlocal

pause