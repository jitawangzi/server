@echo upload 
pscp -pw root %workspace%\game/target/game.zip root@test:/server/game

@echo stop
plink -pw root root@test source /etc/profile; cd /server/game/crossserver; /server/game/crossserver/cross.sh stop
::ping -n 5 127.1>nul

@echo install 
plink -pw root root@test source /etc/profile; /server/bin/install_cross.sh; 

ping -n 1 127.1>nul

@echo start
plink -pw root root@test source /etc/profile; cd /server/game/crossserver; /server/game/crossserver/cross.sh start

pause