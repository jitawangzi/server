:: 设置控制台字符集为UTF-8
chcp 65001 > nul

@echo off
setlocal enabledelayedexpansion

rem ========================================================
rem 配置区域
rem ========================================================

rem 规则1：线上出包 (pre_release -> Branch_West)
set CODE_BRANCH_RELEASE=pre_release
set META_BRANCH_RELEASE=Branch_West

rem 规则2：开发分支 (dev -> Branch_West_Dev)
set CODE_BRANCH_DEV=dev
set META_BRANCH_DEV=Branch_West_Dev

:: 其他分支保持不变

rem 设置默认远程仓库名
set REMOTE_NAME=origin

rem ========================================================
rem 获取当前状态
rem ========================================================

rem 获取当前代码分支名
for /f "tokens=* USEBACKQ" %%F in (`git rev-parse --abbrev-ref HEAD`) do set current_branch=%%F
if %errorlevel% neq 0 (
    echo [错误] 获取当前代码分支失败
    pause
    exit /b 1
)

rem 获取当前配置表分支名
for /f "tokens=* USEBACKQ" %%F in (`git -C %metafolder% rev-parse --abbrev-ref HEAD`) do set current_meta_branch=%%F
if %errorlevel% neq 0 (
    echo [错误] 获取当前配置表分支失败
    pause
    exit /b 1
)

cls
echo ========================================================
echo 当前代码分支:   %current_branch%
echo 当前配置表分支: %current_meta_branch%
echo ========================================================
echo.

rem 保存原始配置表分支，用于后续还原
set original_meta_branch=%current_meta_branch%

rem ========================================================
rem 决策逻辑：确定目标配置表分支
rem ========================================================

rem 默认目标分支为当前分支（即不匹配任何规则时，保持原样）
set target_meta_branch=%current_meta_branch%
set need_restore=0

rem 检查规则1：如果是 pre_release
if "%current_branch%"=="%CODE_BRANCH_RELEASE%" (
    set target_meta_branch=%META_BRANCH_RELEASE%
    echo [匹配规则] 代码分支为 %CODE_BRANCH_RELEASE%，将使用 %META_BRANCH_RELEASE% 配置。
)

rem 检查规则2：如果是 dev
if "%current_branch%"=="%CODE_BRANCH_DEV%" (
    set target_meta_branch=%META_BRANCH_DEV%
    echo [匹配规则] 代码分支为 %CODE_BRANCH_DEV%，将使用 %META_BRANCH_DEV% 配置。
)

echo.

rem ========================================================
rem 执行切换与拉取
rem ========================================================

rem 如果当前配置分支不是目标分支，则进行切换
if not "%current_meta_branch%"=="%target_meta_branch%" (
    echo [操作] 正在切换配置表分支: %current_meta_branch% -^> %target_meta_branch% ...
    git -C %metafolder% checkout -q %target_meta_branch%
    if !errorlevel! neq 0 (
        echo [错误] 切换配置表分支失败
        pause
        exit /b 1
    )
    echo [成功] 已切换到 %target_meta_branch%
    set need_restore=1
) else (
    echo [信息] 配置表分支已正确，无需切换。
)

echo.
echo [操作] 正在拉取 %target_meta_branch% 分支最新配置...
git -C %metafolder% pull %REMOTE_NAME% %target_meta_branch%
if %errorlevel% neq 0 (
    echo [错误] 拉取配置失败
    pause
    exit /b 1
)
echo [成功] 配置拉取完成
echo.

rem ========================================================
rem 执行复制任务
rem ========================================================

echo [操作] 开始复制文件...
call %workspace%\game\tool\copy.bat
if %errorlevel% neq 0 (
    echo [错误] 复制文件失败
    pause
    exit /b 1
)
echo [成功] 文件复制完成
echo.

rem ========================================================
rem 还原状态 (如果之前发生了切换)
rem ========================================================

if "%need_restore%"=="1" (
    echo [操作] 还原配置表分支到原始状态 (%original_meta_branch%)...
    git -C %metafolder% checkout -q %original_meta_branch%
    if !errorlevel! neq 0 (
        echo [错误] 还原配置表分支失败
        pause
        exit /b 1
    )
    echo [成功] 已还原到原始状态
    echo.
)

pause
echo 所有操作已完成!
exit /b 0