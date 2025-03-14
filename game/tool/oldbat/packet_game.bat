:: 只简单打包game

set project=game

set "file=%workspace%\%project%\pom.xml"
:: 指定assembly插件的描述文件xml 和服务器名
mvn -f %file% package %game.assembly.descriptor% %game.server%

:: 检查 Maven 命令的执行结果
if %ERRORLEVEL% neq 0 (
    echo Maven 命令执行失败，错误代码：%ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

::pause
