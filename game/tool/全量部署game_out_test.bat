set server.id=out_test

::打包
call %workspace%\game\tool\packet_all.bat

::部署
call %workspace%\game\tool\deploy_game_out_test.bat

pause
