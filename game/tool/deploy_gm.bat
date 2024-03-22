d:
 
cd %workspace%\gm

call %workspace%\game\tool\packet_gm.bat

@echo upload 
pscp -pw Fe32#@ ./target/gm.war root@192.168.1.67:/server/game/gmserver

@echo restart
plink -pw Fe32#@ root@192.168.1.67 cd /server/game/gmserver; /server/game/gmserver/run.sh stop
ping -n 3 127.1>nul

plink -pw Fe32#@ root@192.168.1.67 cd /server/game/gmserver; /server/game/gmserver/run.sh start


pause