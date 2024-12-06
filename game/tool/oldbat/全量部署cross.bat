set server.id=test


::打包
call %workspace%\game\tool\packet_all.bat

::部署
call %workspace%\game\tool\deploy_cross.bat

pause
