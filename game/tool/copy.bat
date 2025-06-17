echo  start copying svn  files
@echo off

::  复制到工作空间
echo copy config
xcopy /Y /Q %metafolder%\output\java\entity %workspace%\protocol\src\main\java\cn\game\protocol\generated\config

echo copy enum
xcopy /Y /Q /EXCLUDE:%workspace%\game\tool\exclude.txt %metafolder%\output\java\enum %workspace%\protocol\src\main\java\cn\game\protocol\generated\enume

echo copy xml
xcopy /Y /Q %metafolder%\output\xml\data %workspace%\game\src\main\resources\xml
xcopy /Y /Q %metafolder%\output\xml\data\GlobalConst.xml %workspace%\login\src\main\resources\xml

if %copytotarget% equ 1 (

xcopy /Y /Q %metafolder%\output\xml\data %workspace%\game\target\classes\xml
xcopy /Y /Q %metafolder%\output\xml\data\GlobalConst.xml %workspace%\login\target\classes\xml

)

echo copy manager helper 
:: manager helper 
xcopy /Y /Q %metafolder%\output\java\init %workspace%\protocol\src\main\java\cn\game\protocol\generated\helper

:: manager
echo copy manager 
xcopy /Y /Q %metafolder%\output\java\manager %workspace%\protocol\src\main\java\cn\game\protocol\generated\manager

:: echo copy formula
:: xcopy /Y /Q %metafolder%\output\java\formula %workspace%\protocol\src\main\java\cn\game\protocol\generated\helper

:: 地图文件,先把本地的删除

:: del /Q %workspace%\game\src\main\resources\map
:: del /Q %workspace%\game\src\main\resources\mainline

:: echo copy tmx,tsx,json
:: XCopy /Y /Q %metafolder%\maps\*.tmx %workspace%\game\src\main\resources\map
:: XCopy  /Y /Q  %metafolder%\maps\*.tsx %workspace%\game\src\main\resources\map
:: XCopy  /Y /Q  %metafolder%\maps\mainline\*.json %workspace%\game\src\main\resources\mainline

 :: @echo on

 :: pause

