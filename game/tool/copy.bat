@echo off

echo metafolder=%metafolder%
echo workspace=%workspace%
echo copytotarget=%copytotarget%

:: 检查变量是否为空
if "%metafolder%"=="" (
    echo metafolder变量未定义!
    pause
    exit /b
)
if "%workspace%"=="" (
    echo workspace变量未定义!
    pause
    exit /b
)

echo start copying svn files

echo copy config
xcopy /Y /Q "%metafolder%\output\java\entity" "%workspace%\protocol\src\main\java\cn\game\protocol\generated\config"

echo copy enum
xcopy /Y /Q /EXCLUDE:%workspace%\game\tool\exclude.txt "%metafolder%\output\java\enum" "%workspace%\protocol\src\main\java\cn\game\protocol\generated\enume"

echo copy xml
xcopy /Y /Q "%metafolder%\output\xml\data" "%workspace%\game\src\main\resources\xml"
xcopy /Y /Q "%metafolder%\output\xml\data\GlobalConst.xml" "%workspace%\login\src\main\resources\xml"

if "%copytotarget%"=="1" (
    xcopy /Y /Q "%metafolder%\output\xml\data" "%workspace%\game\target\classes\xml"
    xcopy /Y /Q "%metafolder%\output\xml\data\GlobalConst.xml" "%workspace%\login\target\classes\xml"
)

echo copy manager helper 
xcopy /Y /Q "%metafolder%\output\java\init" "%workspace%\protocol\src\main\java\cn\game\protocol\generated\helper"

echo copy manager 
xcopy /Y /Q "%metafolder%\output\java\manager" "%workspace%\protocol\src\main\java\cn\game\protocol\generated\manager"
