:: 只修改game工程代码时，可以用这个快速更新服务器，省略其他工程打包的编译过程。 

call %workspace%\game\tool\update_copy.bat

call %workspace%\game\tool\packet_game_7z.bat

call %workspace%\game\tool\upload.bat

pause
