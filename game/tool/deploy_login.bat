
:: cd %metafolder%\output
:: svn cleanup .
:: svn update .

call %workspace%\game\tool\update_copy.bat

call %workspace%\game\tool\packet.bat

::call %workspace%\game\tool\packet_login.bat
::  mvn package

cd /D %workspace%\login

@echo upload 
pscp -pw root ./target/login.zip root@test:/server/game

@echo install 
plink -pw root root@test source /etc/profile; /server/bin/install_login.sh; 

@echo restart
plink -pw root root@test source /etc/profile; cd /server/game/loginserver; ./login.sh stop
ping -n 3 127.1>nul

plink -pw root root@test source /etc/profile; cd /server/game/loginserver; ./login.sh start


pause