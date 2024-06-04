:: 更新

cd /D %metafolder%
git pull

if %errorlevel% neq 0 (
  echo.
  echo git pull失败，手动处理后重试
  echo.
  pause
  goto :eof
)

:: svn revert -R .
:: svn cleanup .
:: svn update .


:: pull 一下，防止冲突

:: cd /D %workspace%\util
:: git pull

:: cd /D %workspace%\protocol
:: git pull

:: cd /D %workspace%\core
:: git pull

:: cd /D %workspace%\game
:: git pull

call %workspace%\game\tool\copy.bat
