call %workspace%\game\tool\packet_gm.bat

@echo upload 
pscp -pw root %workspace%\..\gm\target\gm.war root@test:/server/game/gm

@echo restart
plink -pw root root@test cd /server/game/gm; /server/game/gm/run.sh stop
ping -n 3 127.1>nul

plink -pw root root@test cd /server/game/gm; /server/game/gm/run.sh start


pause