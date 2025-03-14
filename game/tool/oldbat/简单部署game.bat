REM 只打包game上传，节省点时间

call %workspace%\game\tool\update_copy.bat

call %workspace%\game\tool\packet_game.bat

call %workspace%\game\tool\upload.bat

pause
