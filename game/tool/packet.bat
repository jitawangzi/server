::打包命令，一般不会单独使用 

cd /D %workspace%
:: 指定assembly插件的描述文件xml 和服务器名
mvn clean install %game.assembly.descriptor% %game.server%

if %ERRORLEVEL% neq 0 goto :error

:error
echo 执行过程中出现错误，批处理停止。
goto :end

:end
pause
