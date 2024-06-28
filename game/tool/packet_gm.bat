::打包命令，一般不会单独使用 

set project=gm

set "file=%workspace%\..\%project%\pom.xml"
mvn -f %file% clean package

:: 检查 Maven 命令的执行结果
if %ERRORLEVEL% neq 0 (
    echo Maven 命令执行失败，错误代码：%ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)