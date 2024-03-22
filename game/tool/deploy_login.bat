c:
:: cd %metafolder%\output
:: svn cleanup .
:: svn update .
 

:: call %workspace%\game\tool\ant\bin\ant -buildfile=build.xml


call %workspace%\game\tool\packet_login.bat
::  mvn package

cd %workspace%\login

@echo upload 
pscp -pw root ./target/login.zip root@test:/server/game

@echo install 
plink -pw root root@test source /etc/profile; /server/bin/install_login.sh; 

@echo restart
plink -pw root root@test source /etc/profile; cd /server/game/loginserver; /server/game/loginserver/login.sh stop
ping -n 3 127.1>nul

plink -pw root root@test source /etc/profile; cd /server/game/loginserver; /server/game/loginserver/login.sh start


pause