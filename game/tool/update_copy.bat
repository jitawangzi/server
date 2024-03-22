:: 更新

cd /D %metafolder%
git pull
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
