:: 更新
c:

cd %metafolder%
git pull
:: svn revert -R .
:: svn cleanup .
:: svn update .


:: pull 一下，防止冲突

:: cd %workspace%\util
:: git pull

:: cd %workspace%\protocol
:: git pull

:: cd %workspace%\core
:: git pull

:: cd %workspace%\game
:: git pull

call %workspace%\game\tool\copy.bat
