set server.id=test2

::打包
call %workspace%\game\tool\packet_all.bat

::部署
call %workspace%\game\tool\deploy_game__2.bat

pause
