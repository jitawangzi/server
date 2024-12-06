@echo off
setlocal enabledelayedexpansion

rem 拉取配置表数据时，根据出包的分支，选择不同的配置表分支数据

rem 线上出包的代码分支
set SOURCE_CODE_BRANCH=pre_release
rem 如果当前代码是上面的分支，则使用下面的配置表分支
set META_BRANCH=xianshang


rem 获取当前分支名
for /f "tokens=* USEBACKQ" %%F in (`git rev-parse --abbrev-ref HEAD`) do set current_branch=%%F
if %errorlevel% neq 0 (
    echo [错误] 获取当前分支失败
    pause
    exit /b 1
)

rem 获取目标文件夹的分支名
for /f "tokens=* USEBACKQ" %%F in (`git -C %metafolder% rev-parse --abbrev-ref HEAD`) do set meta_branch=%%F
if %errorlevel% neq 0 (
    echo [错误] 获取目标分支失败
    pause
    exit /b 1
)

cls
echo 当前分支: %current_branch%
echo 目标分支: %meta_branch%
echo.

if "%current_branch%"=="%SOURCE_CODE_BRANCH%" (
    for /f "tokens=* USEBACKQ" %%F in (`git -C %metafolder% rev-parse HEAD`) do set original_commit=%%F
    
    echo [操作] 切换到%META_BRANCH%分支...
    git -C %metafolder% checkout -q xianshang
    if %errorlevel% neq 0 (
        echo [错误] 切换分支失败
        pause
        exit /b 1
    )
    echo [成功] 已切换到%META_BRANCH%分支
    echo.
)

echo [操作] 正在拉取最新代码...
git -C %metafolder% pull
if %errorlevel% neq 0 (
    echo [错误] 拉取代码失败
    pause
    exit /b 1
)
echo [成功] 代码拉取完成
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

if "%current_branch%"=="%SOURCE_CODE_BRANCH%" (
    if not "%original_commit%"=="" (
        echo [操作] 还原到原始状态...
        git -C %metafolder% checkout -q !original_commit!
        if %errorlevel% neq 0 (
            echo [错误] 还原状态失败
            pause
            exit /b 1
        )
        echo [成功] 已还原到原始状态
        echo.
    )
)

echo 所有操作已完成!
exit /b 0