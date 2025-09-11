package cn.game.games.net.game.module.func;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

import java.util.HashMap;
import java.util.Map;

public class FuncModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LevelUp,EventTypeEnum.LoginSuccess,EventTypeEnum.ChapterWin };
    Map<InitialUI, Boolean> funcOpenMap=new HashMap<>();
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case LevelUp: {
			int exp = event.getIntParameter(0);
			if (exp == Asset.playerExp.ID) {
				refreshFuncOpen();
			}
			break;
		}
		case ChapterWin: {
			refreshFuncOpen();
			break;
		}
		case PLAYER_CREATE: {
			initFuncOpenData();
			break;
		}
		case LoginSuccess: {
			loginCheckNewFuncOpen();
			break;
		  }
		}

	}

	public boolean isFuncOpen(InitialUI type) {
		if(funcOpenMap.containsKey(type)) {
           return funcOpenMap.get(type);
		}
		return true;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	public void refreshFuncOpen() {
		int level=player.getLevel();
		int chapterId = player.getBattleModule().getMainBattleHighest();
		funcOpenMap.forEach((k, v) -> {
			if ( v==false && k.DisplayLevel >= level) {
				if(k.FuncOpen.length<=0) {
					// 单等级解锁
					setFuncOpen(k);
				}else {
					// 有2级条件 目前只处理主线 其余不管
					if(k.FuncOpen[0]==9001) {
						if(k.FuncOpen[1]<=chapterId) {
							setFuncOpen(k);
						}
					}else{
						setFuncOpen(k);
					}
				}
			}
		});
	}
	public void initFuncOpenData()
	{
		InitialUI[] values = InitialUI.values();
		for (InitialUI initialUI : values) {
			funcOpenMap.put(initialUI,false);
		}
		refreshFuncOpen();
	}
	void setFuncOpen(InitialUI type) {
		if(funcOpenMap.containsKey( type) && funcOpenMap.get( type)== false) {
			funcOpenMap.put( type, true);
			player.handleEvent(EventTypeEnum.FuncOpen, type);
		}
	}
	void loginCheckNewFuncOpen()
	{
		if(funcOpenMap.size()<InitialUI.values().length)
		{
			// 有新功能
			InitialUI[] values = InitialUI.values();
			int level=player.getLevel();
			int chapterId = player.getBattleModule().getMainBattleHighest();
			for (InitialUI initialUI : values) {
				if (!funcOpenMap.containsKey(initialUI)) {
					if (initialUI.DisplayLevel <= level) {
						if (initialUI.FuncOpen.length <= 0) {
							// 单等级解锁
							setFuncOpen(initialUI);
						} else {
							// 有2级条件 目前只处理主线 其余不管
							if (initialUI.FuncOpen[0] == 9001) {
								if (initialUI.FuncOpen[1] <= chapterId) {
									setFuncOpen(initialUI);
								} else {
									funcOpenMap.put(initialUI, false);
								}
							} else {
								setFuncOpen(initialUI);
							}
						}
					} else {
						funcOpenMap.put(initialUI, false);
					}
				}
			}
		}

	}

	public void gmUnlockFunc(byte type) {
		if (type > 0) {
			setFuncOpen(InitialUI.get(type));
		} else {
			funcOpenMap.forEach((k, v) -> setFuncOpen(k)
			);
		}
	}

	@Override
	public int processOrder() {
		return EVENT_PROCESS_ORDER_LOW;
	}
}
