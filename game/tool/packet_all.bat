:: 打全量包

::打包一下util
::call %workspace%\game\tool\packet-util.bat

::call %workspace%\game\tool\packet-protocol.bat

::call %workspace%\game\tool\packet-core.bat

:: cd %workspace%\game\tool
:: 初始化变量
call %workspace%\game\tool\init_env.bat

:: 指定assembly插件的描述文件xml
set game.assembly.descriptor="-Dgame.assembly.descriptor=package_full.xml"

:: 设置服务器配置目录
set game.server=-Dgame.server=%server.id%
