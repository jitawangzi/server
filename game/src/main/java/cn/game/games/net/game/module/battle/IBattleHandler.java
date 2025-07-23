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
	
	protected transient Player player;
	public void setPlayer(Player player) {
		this.player = player;
	}

	/** 
	 * 通用的BattleConfig战斗前关卡检查
	 * @param id  BattleConfig的ID
	 * @param subId TODO
	 * @return
	 */
	abstract int check(int id, int subId);
	
	/** 
	 * 玩法的特殊规则校验
	 * @param id
	 * @param subId
	 * @return
	 */
	abstract int checkCustom(int id, int subId);
	
	/** 
	 * 战斗开始前的一些处理。 
	 * @param id
	 * @return
	 */
	abstract int battleStart(int id);
	/** 
	 * 战斗开始前的一些处理。 
	 * @param id
	 * @param subId
	 * @return
	 */
	abstract int battleStart(int id,int subId);

	/** 
	 * 处理一些战斗结束的逻辑
	 * @param request
	 * @return	战斗奖励，这里一般处理每个玩法的特殊奖励，通用奖励在这之后处理
	 */
	abstract ResultObject<List<RewardInfo>> battleEnd(BattleFieldEndRequest_13000003 request);
	
	/** 
	 * 战斗奖励
	 * @param request
	 * @return
	 */
	abstract List<RewardInfo> battleEndReward(BattleFieldEndRequest_13000003 request);

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
	 * 跨天重置数据
	 */
	abstract void newDay();

	abstract void onLogin();

	public boolean hasRedPoint() {
		return false;
	}
}
