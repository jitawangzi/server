:: 打全量包

:: 指定game工程assembly插件的描述文件xml
set game.assembly.descriptor="-Dgame.assembly.descriptor=package_full.xml"

:: 设置服务器配置目录
:: set game.server=-Dgame.server=%server.id%


cd /D C:\work_all\work_2025\server

echo [Maven] 开始构建...
echo   - Descriptor: %game.assembly.descriptor%
echo   - Server:     %game.server%
pause
rem 执行 Maven 命令
call mvn clean install -f pom.xml %game.assembly.descriptor%

if %ERRORLEVEL% neq 0 (
    echo.
    echo [Packet错误] Maven 构建失败！
    pause
    exit /b 1
)

echo [Maven] 构建成功。
exit /b 0

pause
