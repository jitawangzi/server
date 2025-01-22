@echo upload 
pscp -pw root %workspace%\game/target/classes\cn\game\games\net\game\module\rank/RankService.class root@test:/server/game/gameserver/cn/game/games/net/game/module/rank


pause