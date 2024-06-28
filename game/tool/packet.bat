::打包命令，一般不会单独使用 

::打包一下util
call %workspace%\game\tool\packet-util.bat

if %ERRORLEVEL% neq 0 goto :error

call %workspace%\game\tool\packet-protocol.bat
if %ERRORLEVEL% neq 0 goto :error

call %workspace%\game\tool\packet-core.bat
if %ERRORLEVEL% neq 0 goto :error

cd /D %workspace%\game
:: call %workspace%\game\tool\ant\bin\ant -buildfile=build.xml
:: 指定assembly插件的描述文件xml 和服务器名
mvn clean install %game.assembly.descriptor% %game.server%

if %ERRORLEVEL% neq 0 goto :error

:error
echo 执行过程中出现错误，批处理停止。
goto :end

:end
pause
