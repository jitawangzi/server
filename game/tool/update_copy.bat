:: update_copy.bat
@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

rem ========================================================
rem 配置区域
rem ========================================================
set CODE_BRANCH_RELEASE=pre_release
set META_BRANCH_RELEASE=Branch_West

set CODE_BRANCH_DEV=dev
set META_BRANCH_DEV=Branch_West_Dev

set REMOTE_NAME=origin

rem ========================================================
rem 获取当前分支状态
rem ========================================================

rem 1. 获取代码分支 (使用 workspace 环境变量)
pushd "%workspace%"
for /f "tokens=* USEBACKQ" %%F in (`git rev-parse --abbrev-ref HEAD`) do set current_branch=%%F
popd

rem 2. 获取配置分支 (使用 metafolder 环境变量)
pushd "%metafolder%"
for /f "tokens=* USEBACKQ" %%F in (`git rev-parse --abbrev-ref HEAD`) do set current_meta_branch=%%F
popd

echo [状态] 代码分支: %current_branch%
echo [状态] 配置分支: %current_meta_branch%

set original_meta_branch=%current_meta_branch%
set target_meta_branch=%current_meta_branch%
set need_restore=0

rem ========================================================
rem 决策逻辑
rem ========================================================
if "%current_branch%"=="%CODE_BRANCH_RELEASE%" (
    set target_meta_branch=%META_BRANCH_RELEASE%
    echo [规则匹配] 正式环境 -> 使用配置: %META_BRANCH_RELEASE%
)

if "%current_branch%"=="%CODE_BRANCH_DEV%" (
    set target_meta_branch=%META_BRANCH_DEV%
    echo [规则匹配] 开发环境 -> 使用配置: %META_BRANCH_DEV%
)

rem ========================================================
rem Git 操作 (配置表)
rem ========================================================
if not "%current_meta_branch%"=="%target_meta_branch%" (
    echo [配置表] 正在切换分支: %target_meta_branch%
    git -C "%metafolder%" checkout -q %target_meta_branch%
    if !errorlevel! neq 0 (
        echo [错误] 配置表切换分支失败
        exit /b 1
    )
    set need_restore=1
)

echo [配置表] 拉取远程更新...
git -C "%metafolder%" pull %REMOTE_NAME% %target_meta_branch%
if !errorlevel! neq 0 (
    echo [错误] 配置表 Pull 失败
    exit /b 1
)

rem ========================================================
rem 执行复制任务
rem ========================================================
echo [复制] 执行 copy.bat ...

rem 假设 copy.bat 在 workspace/game/tool/ 下
set "COPY_SCRIPT=%workspace%\game\tool\copy.bat"

if not exist "%COPY_SCRIPT%" (
    echo [错误] 找不到复制脚本: "%COPY_SCRIPT%"
    exit /b 1
)

call "%COPY_SCRIPT%"
if !errorlevel! neq 0 (
    echo [错误] copy.bat 执行返回错误
    exit /b 1
)

rem ========================================================
rem 状态还原
rem ========================================================
if "%need_restore%"=="1" (
    echo [配置表] 还原分支到: %original_meta_branch%
    git -C "%metafolder%" checkout -q %original_meta_branch%
)

echo [Update] 资源同步成功。
exit /b 0