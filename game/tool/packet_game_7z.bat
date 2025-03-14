
:: rd %workspace%\game\target\resources

del %workspace%\game\target\game.zip

:: mkdir %workspace%\game\target\resources

:: xcopy /S /Y /Q %workspace%\game\target\classes %workspace%\game\target\resources /EXCLUDE:%workspace%\game\tool\game.zip_exclude.txt

%workspace%\game\tool\7z a %workspace%\game\target\game.zip %workspace%\game\target\classes\cn

::%workspace%\game\tool\7z a %workspace%\game\target\game.zip %workspace%\game\target\resources

:: pause
