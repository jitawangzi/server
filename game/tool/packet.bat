::打包命令，一般不会单独使用 

:: 设置Maven编码相关环境变量
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=GMT+08
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

cd /D %workspace%
:: 指定assembly插件的描述文件xml 和服务器名
mvn clean install %game.assembly.descriptor% %game.server%

if %ERRORLEVEL% neq 0 goto :error

:error
echo 执行过程中出现错误，批处理停止。
goto :end

:end
pause
