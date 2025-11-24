:: build_start.bat
@echo off
:: 设置编码，防止乱码
chcp 65001 > nul

echo ========================================================
echo              正在初始化构建环境...
echo ========================================================

:: ---------------------------------------------------------
:: 1. 环境变量安全检查 (使用 if defined 防止语法错误)
:: ---------------------------------------------------------

:: 检查 workspace
if not defined workspace (
    echo.
    echo [致命错误] 环境变量 workspace 未定义！
    echo 请在系统环境变量中设置 workspace。
    echo.
    pause
    exit /b
)

:: 检查路径是否真的存在
if not exist "%workspace%" (
    echo.
    echo [致命错误] workspace 指向的路径不存在！
    echo 当前值: "%workspace%"
    echo.
    pause
    exit /b
)

:: 检查 metafolder
if not defined metafolder (
    echo.
    echo [致命错误] 环境变量 metafolder 未定义！
    echo.
    pause
    exit /b
)

if not exist "%metafolder%" (
    echo.
    echo [致命错误] metafolder 指向的路径不存在！
    echo 当前值: "%metafolder%"
    echo.
    pause
    exit /b
)

echo [环境检查] Workspace : "%workspace%"
echo [环境检查] Metafolder: "%metafolder%"
echo.

:: ---------------------------------------------------------
:: 2. 参数配置
:: ---------------------------------------------------------
set BRANCH_DEV=dev
set BRANCH_RELEASE=pre_release

set SCRIPT_UPDATE=update_copy.bat
set SCRIPT_PACKET=packet.bat

:: ---------------------------------------------------------
:: 3. 菜单选择
:: ---------------------------------------------------------
:MENU
echo ========================================================
echo           自动化构建系统
echo ========================================================
echo 1. 构建 开发环境包 (%BRANCH_DEV%)
echo 2. 构建 正式发布包 (%BRANCH_RELEASE%)
echo.
set /p choice=请输入选项 (1 或 2): 

if "%choice%"=="1" (
    set TARGET_CODE_BRANCH=%BRANCH_DEV%
    set BUILD_TYPE=开发版
    goto GIT_PROCESS
)
if "%choice%"=="2" (
    set TARGET_CODE_BRANCH=%BRANCH_RELEASE%
    set BUILD_TYPE=正式版
    goto GIT_PROCESS
)
echo [错误] 输入无效，请重新输入。
goto MENU

:: ---------------------------------------------------------
:: 4. Git 处理
:: ---------------------------------------------------------
:GIT_PROCESS
echo.
echo [阶段 1/3] 处理主工程 Git...
echo 正在进入目录: "%workspace%"
cd /d "%workspace%"

echo [Git] 切换到分支: %TARGET_CODE_BRANCH%
git checkout %TARGET_CODE_BRANCH%
if errorlevel 1 goto ERROR_EXIT

echo [Git] 拉取最新代码...
git pull
if errorlevel 1 goto ERROR_EXIT

:: ---------------------------------------------------------
:: 5. 执行子脚本
:: ---------------------------------------------------------
:RUN_SCRIPTS
:: 切回脚本所在目录，确保能找到 update_copy.bat
cd /d "%~dp0"

echo.
echo [阶段 2/3] 执行资源同步 (%SCRIPT_UPDATE%)...
if not exist "%SCRIPT_UPDATE%" (
    echo [错误] 找不到脚本: %SCRIPT_UPDATE%
    goto ERROR_EXIT
)
call "%SCRIPT_UPDATE%"
if errorlevel 1 goto ERROR_EXIT

echo.
echo [阶段 3/3] 执行 Maven 打包 (%SCRIPT_PACKET%)...
if not exist "%SCRIPT_PACKET%" (
    echo [错误] 找不到脚本: %SCRIPT_PACKET%
    goto ERROR_EXIT
)
call "%SCRIPT_PACKET%"
if errorlevel 1 goto ERROR_EXIT

echo.
echo ========================================================
echo        %BUILD_TYPE% 构建全部完成！
echo ========================================================
pause
exit /b 0

:: ---------------------------------------------------------
:: 错误处理模块
:: ---------------------------------------------------------
:ERROR_EXIT
echo.
echo [流程终止] 出现错误，请检查上方红色报错信息。
pause
exit /b 1