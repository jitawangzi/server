:: 只简单打包game

cd /D %workspace%\game

:: 指定assembly插件的描述文件xml 和服务器名
mvn package %game.assembly.descriptor% %game.server%

::pause
