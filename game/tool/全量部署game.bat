set server.id=test

call %workspace%\game\tool\update_copy.bat
::打包
call %workspace%\game\tool\packet_all.bat
::部署
call %workspace%\game\tool\upload.bat
pause
