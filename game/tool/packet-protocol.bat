set project=protocol

set "file=%workspace%\%project%\pom.xml"
:: mvn clean install
if defined clean (mvn -f %file% clean install ) else ( mvn -f %file% install)


:: 检查 Maven 命令的执行结果
if %ERRORLEVEL% neq 0 (
    echo Maven 命令执行失败，错误代码：%ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)