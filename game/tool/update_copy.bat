@echo off
setlocal enabledelayedexpansion

rem 设置metafolder路径和workspace路径
rem set metafolder=path\to\your\repo
rem set workspace=path\to\your\workspace

rem 检查当前所在目录的分支名
for /f "tokens=*" %%i in ('git -C %cd% rev-parse --abbrev-ref HEAD') do set current_branch=%%i

rem 判断当前分支是否为pre_release
if "%current_branch%"=="pre_release" (
    echo 当前分支为 pre_release，执行特殊逻辑...

    rem 记录%metafolder%所在的分支名
    for /f "tokens=*" %%j in ('git -C %metafolder% rev-parse --abbrev-ref HEAD') do set meta_branch=%%j
    echo %metafolder% 当前分支为: %meta_branch%

    rem 切换到 xianshang 分支
    git -C %metafolder% checkout xianshang
    if %errorlevel% neq 0 (
        echo 切换到 xianshang 分支失败，请手动处理。
        pause
        exit /b %errorlevel%
    )

    rem 拉取代码
    git -C %metafolder% pull
    if %errorlevel% neq 0 (
        echo.
        echo git pull失败，手动处理后重试
        echo.
        pause
        exit /b %errorlevel%
    )

    rem 执行文件复制逻辑
    call %workspace%\game\tool\copy.bat

    rem 切换回原来的分支
    git -C %metafolder% checkout %meta_branch%
    if %errorlevel% neq 0 (
        echo 切换回原分支 %meta_branch% 失败，请手动处理。
        pause
        exit /b %errorlevel%
    )
) else (
    echo 当前分支不是 pre_release，正常执行逻辑...

    rem 拉取代码
    git -C %metafolder% pull
    if %errorlevel% neq 0 (
        echo.
        echo git pull失败，手动处理后重试
        echo.
        pause
        exit /b %errorlevel%
    )

    rem 执行文件复制逻辑
    call %workspace%\game\tool\copy.bat
)

endlocal

pause