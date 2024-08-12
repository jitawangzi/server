package cn.game.games.net.game.module.battle;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	@JsonIgnore
	protected Player player;
	public void setPlayer(Player player) {
		this.player = player;
	}
	abstract int battleStart(int id);
	
	abstract ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request);
	
	/** 
	 * 有些战斗可以直接结束
	 * @param id  看战斗类型，通常是battle表id
	 * @param subId 
	 * @param isWin 
	 * @return
	 */
	abstract ResultObject<List<RewardInfo>> quickEnd(int id, int subId, boolean isWin);

	abstract int getType();

	/** 
	 * 战斗前关卡检查
	 * @param id
	 * @param subId TODO
	 * @return
	 */
	abstract int check(int id, int subId);

	/** 
	 * 跨天重置数据
	 */
	abstract void newDay();
}
