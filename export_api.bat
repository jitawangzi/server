@echo off
setlocal

REM =================配置区域=================

REM 1. 工具 JAR 包的位置 (请修改为你 JAR 的实际位置)
set TOOL_JAR=%workspace%\tools\util-1.0.jar

REM =========================================

if "%workspace%"=="" (
    echo [ERROR] 'workspace' environment variable is NOT set.
    echo Use 'set workspace=...' or edit this script.
    goto :end
)

if not exist "%TOOL_JAR%" (
    echo [ERROR] Tool Jar not found at: %TOOL_JAR%
    goto :end
)

REM 执行 Java 工具                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
REM 我们不需要 -Dworkspace，因为 System.getenv 也能读到上面的 set workspace
REM %* 代表传递所有参数 (例如: game login -o context.txt)
java -cp .;%workspace%\tools\* cn.game.util.ai.AiContextExporter game login util -o %workspace%\ai\_common\api_index.txt

:end
endlocal


pause