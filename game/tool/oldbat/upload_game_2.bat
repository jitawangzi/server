@echo upload 
pscp -pw Fe32#@ d:/work/game/target/game.zip root@192.168.1.67:/server/game

@echo stop
plink -pw Fe32#@ root@192.168.1.67 source /etc/profile; cd /server/game/gameserver2; /server/game/gameserver2/game.sh stop
ping -n 3 127.1>nul

@echo install 
plink -pw Fe32#@ root@192.168.1.67 source /etc/profile; /server/game/bin/install_game2.sh; 

@echo start
plink -pw Fe32#@ root@192.168.1.67 source /etc/profile; cd /server/game/gameserver2; /server/game/gameserver2/game.sh start


:: pause