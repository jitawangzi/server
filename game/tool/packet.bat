::打包命令，一般不会单独使用 

::打包一下util
call %workspace%\game\tool\packet-util.bat

call %workspace%\game\tool\packet-protocol.bat

call %workspace%\game\tool\packet-core.bat

cd /D %workspace%\game
:: call %workspace%\game\tool\ant\bin\ant -buildfile=build.xml
:: 指定assembly插件的描述文件xml 和服务器名
mvn clean install %game.assembly.descriptor% %game.server%

pause
