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
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LevelUp,EventTypeEnum.LoginSuccess };
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
			int level = event.getIntParameter(1);
			if (exp == Asset.playerExp.ID) {
				refreshFuncOpen(level);
			}
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

	public boolean isFuncOpen(int id) {
		return true;
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

	public void refreshFuncOpen(int level) {
		InitialUI[] values = InitialUI.values();
		for (InitialUI initialUI : values) {
			if (initialUI.DisplayLevel == level) {
				setFuncOpen(initialUI);
			}
		}
	}
	public void initFuncOpenData()
	{
		InitialUI[] values = InitialUI.values();
		for (InitialUI initialUI : values) {
			funcOpenMap.put(initialUI,false);
		}
		refreshFuncOpen(1);
	}
	void setFuncOpen(InitialUI type) {
		if(funcOpenMap.get( type)== false) {
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
			for (InitialUI initialUI : values) {
				if(!funcOpenMap.containsKey(initialUI))
				{
					if(initialUI.DisplayLevel<=player.getLevel())
					{
						funcOpenMap.put( initialUI, true);
						player.handleEvent(EventTypeEnum.FuncOpen, initialUI);
					}else {
						funcOpenMap.put(initialUI, false);
					}
				}
			}
		}

	}
}
