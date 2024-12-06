:: 更新

git -C %metafolder% pull

if %errorlevel% neq 0 (
  echo.
  echo git pull失败，手动处理后重试
  echo.
  pause
  exit /b %ERRORLEVEL%
)

:: svn revert -R .
:: svn cleanup .
:: svn update .


:: pull 一下，防止冲突

:: cd /D %workspace%\game
:: git pull

call %workspace%\game\tool\copy.bat
