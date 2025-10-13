@echo upload 
pscp -pw root %workspace%\login/target/login.zip root@test:/server/game

@echo stop
plink -pw root root@test source /etc/profile; cd /server/game/loginserver; ./login.sh stop
::ping -n 5 127.1>nul

@echo install 
plink -pw root root@test source /etc/profile; /server/bin/install_login.sh; 

ping -n 1 127.1>nul

@echo start
plink -pw root root@test source /etc/profile; cd /server/game/loginserver; ./login.sh start

pause