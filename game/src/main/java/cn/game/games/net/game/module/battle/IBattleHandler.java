package cn.game.games.net.game.module.battle;

import java.util.List;

import cn.game.games.cache.entity.Player;
import cn.game.games.core.ResultObject;
import cn.game.protocol.protobuf.BattleMsg.BattleFieldEndRequest_13000003;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**
 * 战斗接口
 * 2021年1月14日 下午6:17:52
 * @author SYQ
 */
public abstract class IBattleHandler {
	protected Player player;
	public void setPlayer(Player player) {
		this.player = player;
	}
	abstract int battleStart(int id);
	
	abstract ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request);
	
	/** 
	 * 有些战斗可以直接结束
	 * @param id  看战斗类型，通常是battle表id
	 * @return
	 */
	abstract ResultObject<List<RewardInfo>> quickEnd(int id);

	abstract int getType();

	abstract int check(int id);

	abstract void newDay();
}
