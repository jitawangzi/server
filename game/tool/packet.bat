:: packet.bat
@echo off
chcp 65001 > nul

rem ========================================================
rem 参数检查
rem ========================================================
if "%workspace%"=="" (
    echo [Packet错误] 环境变量 workspace 未定义。
    exit /b 1
)

rem ========================================================
rem 执行打包
rem ========================================================
echo [Maven] 切换工作目录: "%workspace%"
cd /D "%workspace%"

echo [Maven] 设置 Java 选项...
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=GMT+08
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

echo [Maven] 开始构建...
echo   - Descriptor: %game.assembly.descriptor%
echo   - Server:     %game.server%

rem 执行 Maven 命令
call mvn clean install -f pom.xml %game.assembly.descriptor% %game.server%

if %ERRORLEVEL% neq 0 (
    echo.
    echo [Packet错误] Maven 构建失败！
    exit /b 1
)

echo [Maven] 构建成功。
exit /b 0