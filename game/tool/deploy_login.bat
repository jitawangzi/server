:: 设置控制台字符集为UTF-8
chcp 65001 > nul

call %workspace%\game\tool\update_copy.bat

call %workspace%\game\tool\packet.bat

call %workspace%\game\tool\upload_login.bat


pause
