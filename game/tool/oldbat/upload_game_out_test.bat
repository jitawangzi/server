@echo upload 
"C:\Program Files\PuTTY"\pscp -i D:\key.ppk d:/work/game/target/game.zip root@gsali:/server/game

@echo stop
"C:\Program Files\PuTTY"\plink -i D:\key.ppk root@gsali source /etc/profile; cd /server/game/gameserver; /server/game/gameserver/game.sh stop
ping -n 3 127.1>nul

@echo install 
"C:\Program Files\PuTTY"\plink -i D:\key.ppk root@gsali source /etc/profile; /server/game/bin/install_game.sh; 

@echo start
"C:\Program Files\PuTTY"\plink -i D:\key.ppk root@gsali source /etc/profile; cd /server/game/gameserver; /server/game/gameserver/game.sh start

pause