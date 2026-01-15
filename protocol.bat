@echo off
setlocal EnableExtensions EnableDelayedExpansion

chcp 65001 > nul

rem 1) 解析仓库根目录
for %%I in ("%~dp0.") do set "REPO_ROOT=%%~fI"

rem 2) workspace/metafolder 默认指向仓库根，可在外部预先设置覆盖
if not defined workspace (
    set "workspace=%REPO_ROOT%"
)

if not exist "%workspace%" (
    echo [ERROR] workspace 路径 "%workspace%" 不存在。
    exit /b 1
)

if not defined metafolder (
    set "metafolder=%workspace%"
)

if not exist "%metafolder%" (
    echo [ERROR] metafolder 路径 "%metafolder%" 不存在。
    exit /b 1
)

set "PROTO_DIR=%REPO_ROOT%\protocol"
set "PROTO_CLASSES=%PROTO_DIR%\target\classes"
set "PROTO_DEPS=%PROTO_DIR%\target\dependency"
set "GENERATOR_CLASS=%PROTO_CLASSES%\cn\game\protocol\tool\PbProtocolGenerator.class"

echo [INFO] workspace = "%workspace%"
echo [INFO] metafolder = "%metafolder%"
echo [INFO] 仓库根目录 = "%REPO_ROOT%"
echo.

if exist "%GENERATOR_CLASS%" (
    echo [INFO] 检测到已编译的 PbProtocolGenerator，跳过 Maven 编译。
) else (
    echo [INFO] 未找到 "%GENERATOR_CLASS%"，开始执行 Maven 构建...
    pushd "%REPO_ROOT%" > nul
    call mvn -pl protocol -am -DskipTests package
    set "EXIT_CODE=!ERRORLEVEL!"
    popd > nul
    if !EXIT_CODE! neq 0 (
        echo.
        echo [失败] Maven 构建失败，exit code = !EXIT_CODE!
        exit /b !EXIT_CODE!
    )
    if not exist "%GENERATOR_CLASS%" (
        echo.
        echo [失败] 构建完成后仍找不到 PbProtocolGenerator.class，请检查工程。
        exit /b 1
    )
)

set "NEED_DEPS="
if not exist "%PROTO_DEPS%" (
    set "NEED_DEPS=1"
) else (
    dir /b "%PROTO_DEPS%\*.jar" >nul 2>&1
    if errorlevel 1 set "NEED_DEPS=1"
)
if not defined NEED_DEPS (
    dir /b "%PROTO_DEPS%\velocity-*.jar" >nul 2>&1
    if errorlevel 1 set "NEED_DEPS=1"
)
if defined NEED_DEPS (
    echo [INFO] 依赖目录缺失或不完整，执行 Maven dependency:copy-dependencies...
    pushd "%REPO_ROOT%" > nul
    call mvn -pl protocol -am -DskipTests dependency:copy-dependencies -DincludeScope=compile
    set "EXIT_CODE=!ERRORLEVEL!"
    popd > nul
    if !EXIT_CODE! neq 0 (
        echo.
        echo [失败] Maven 复制依赖失败，exit code = !EXIT_CODE!
        exit /b !EXIT_CODE!
    )
)
if not exist "%PROTO_DEPS%" (
    echo [WARN] 未找到依赖目录 "%PROTO_DEPS%"，将仅使用 classes 目录。
    echo        如果运行失败，请删除 target 后重新执行本脚本触发 Maven 构建。
)

set "PROTO_CLASSPATH=%PROTO_CLASSES%"
if exist "%PROTO_DEPS%" (
    set "PROTO_CLASSPATH=%PROTO_CLASSPATH%;%PROTO_DEPS%\*"
)

echo [INFO] 使用 classpath: %PROTO_CLASSPATH%
echo.

pushd "%PROTO_DIR%" > nul
java -Dworkspace="%workspace%" -Dmetafolder="%metafolder%" -cp "%PROTO_CLASSPATH%" cn.game.protocol.tool.PbProtocolGenerator
set "EXIT_CODE=!ERRORLEVEL!"
popd > nul

if !EXIT_CODE! neq 0 (
    echo.
    echo [失败] PbProtocolGenerator 执行失败，exit code = !EXIT_CODE!
    exit /b !EXIT_CODE!
)

echo.
echo [完成] PbProtocolGenerator 执行成功。
exit /b 0
