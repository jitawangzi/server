:: 只简单打包game

set project=game

set "file=%workspace%\%project%\pom.xml"
:: 指定assembly插件的描述文件xml 和服务器名
mvn -f %file% package %game.assembly.descriptor% %game.server%

::pause
