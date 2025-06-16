:: 打全量包

:: 初始化变量
call %workspace%\game\tool\init_env.bat

set lib.clear=1

:: 指定game工程assembly插件的描述文件xml
set game.assembly.descriptor="-Dgame.assembly.descriptor=package_full.xml"

:: 设置服务器配置目录
:: set game.server=-Dgame.server=%server.id%

::打包
call %workspace%\game\tool\packet.bat

pause
