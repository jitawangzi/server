@echo off
plink -batch -ssh -T -pw root root@test exit

@echo upload
pscp -batch -pw root "%workspace%\game\target\game.zip" root@test:/server/game

@echo stop
plink -batch -T -pw root root@test bash -lc 'source /etc/profile; cd /server/game/gameserver; ./game.sh stop'

@echo install
plink -batch -T -pw root root@test bash -lc 'export LIB_CLEAR=%lib.clear%; source /etc/profile; /server/bin/install_game.sh'

ping -n 1 127.1>nul

@echo start
plink -batch -T -pw root root@test bash -lc 'source /etc/profile; cd /server/game/gameserver; ./game.sh start'

pause