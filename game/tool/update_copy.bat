@echo off
setlocal enabledelayedexpansion

rem 拉取配置表数据时，根据出包的分支，选择不同的配置表分支数据

rem 线上出包的代码分支
set SOURCE_CODE_BRANCH=pre_release
rem 如果当前代码是上面的分支，则使用下面的配置表分支
set META_BRANCH=xianshang
rem 设置默认远程仓库名（通常是origin）
set REMOTE_NAME=origin

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
echo 当前代码分支: %current_branch%
echo 当前配置表分支: %current_meta_branch%
echo.

rem 保存当前配置表的分支状态
set original_meta_branch=%current_meta_branch%

if "%current_branch%"=="%SOURCE_CODE_BRANCH%" (
    rem 如果代码分支是pre_release，切换配置表到xianshang分支
    echo [操作] 切换配置表到%META_BRANCH%分支...
    git -C %metafolder% checkout -q %META_BRANCH%
    if %errorlevel% neq 0 (
        echo [错误] 切换配置表分支失败
        pause
        exit /b 1
    )
    echo [成功] 已切换到%META_BRANCH%分支
    echo.
    
    rem 拉取xianshang分支的最新代码
    echo [操作] 正在拉取最新配置...
    git -C %metafolder% pull %REMOTE_NAME% %META_BRANCH%
) else (
    rem 如果不是pre_release分支，直接在当前配置表分支拉取
    echo [操作] 正在拉取最新配置...
    git -C %metafolder% pull %REMOTE_NAME% %current_meta_branch%
)

if %errorlevel% neq 0 (
    echo [错误] 拉取配置失败
    pause
    exit /b 1
)
echo [成功] 配置拉取完成
echo.

echo [操作] 开始复制文件...
call %workspace%\game\tool\copy.bat
if %errorlevel% neq 0 (
    echo [错误] 复制文件失败
    pause
    exit /b 1
)
echo [成功] 文件复制完成
echo.

rem 如果之前切换过配置表分支，需要切换回原来的分支
if "%current_branch%"=="%SOURCE_CODE_BRANCH%" (
    if not "%original_meta_branch%"=="%META_BRANCH%" (
        echo [操作] 还原配置表分支到原始状态...
        git -C %metafolder% checkout -q %original_meta_branch%
        if %errorlevel% neq 0 (
            echo [错误] 还原配置表分支失败
            pause
            exit /b 1
        )
        echo [成功] 已还原到原始状态
        echo.
    )
)

echo 所有操作已完成!
exit /b 0