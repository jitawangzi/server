
@echo restart

plink -pw Fe32#@ root@192.168.1.67 source /etc/profile; cd /server/game/gameserver; /server/game/gameserver/game.sh stop

ping -n 5 127.1>nul

plink -pw Fe32#@ root@192.168.1.67 source /etc/profile; cd /server/game/gameserver; /server/game/gameserver/game.sh start

pause