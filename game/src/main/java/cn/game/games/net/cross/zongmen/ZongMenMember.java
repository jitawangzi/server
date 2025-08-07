package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.core.cache.RedisLocalCache;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.protobuf.ZongMenMsg;

/**
 * @ClassName ZongMenMember
 *
 * @description: 宗门成员
 * @author: ly
 * @create: 2025-02-05 18:30 @Version 1.0
 */
public class ZongMenMember implements ZongMenConstants.ZongMenEventHandler {

	public long playerId;
	/** 加入时间戳*/
	long joinTime;
	/**贡献值 */
	int contribution;
	/**累计贡献值 */
	int totalContribution;
	/** 本周贡献 */
	int weekContribution;
	/**职位 */
	public int position;
	/** 领取过的宗门活跃度奖励  */
	List<Integer> rewardLivenessIndexList = new ArrayList<>();

	public ZongMenMember() {
	}

	public ZongMenMember(long playerId, int position) {
		this.playerId = playerId;
		setJoinTime(System.currentTimeMillis());
		setPosition(position);
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/** 
	 * 获取成员战斗力
	 * @return
	 */
	public int getPower() {
		return PlayerHelper.getSimplePlayer(playerId).getCombatEffectiveness();
	}

	/** 
	 * 获取成员名字
	 * @return
	 */
	public String getName() {
		return PlayerHelper.getSimplePlayer(playerId).getName();
	}

	public long getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(long joinTime) {
		this.joinTime = joinTime;
	}


	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getWeekContribution() {
		return weekContribution;
	}

	public void addcontribution(int contribution) {
		this.contribution += contribution;
		this.weekContribution += contribution;
		this.totalContribution += contribution;
	}

	public List<Integer> getRewardLivenessIndexList() {
		return rewardLivenessIndexList;
	}

	public void setRewardLivenessIndexList(List<Integer> rewardLivenessIndexList) {
		this.rewardLivenessIndexList = rewardLivenessIndexList;
	}

	public ZongMenMsg.ZongMenMemberInfo.Builder toProto() {
		ZongMenMsg.ZongMenMemberInfo.Builder builder = ZongMenMsg.ZongMenMemberInfo.newBuilder();
		builder.setJoinTime((int) (joinTime / 1000L));
		builder.setPosition(position);
		builder.setTodayContribute(contribution);
		builder.setTotalContribute(totalContribution);
		return builder;
	}

	@Override
	public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
		return new ZongMenConstants.ZongMenEvenType[] { ZongMenConstants.ZongMenEvenType.CROSS_DAY,ZongMenConstants.ZongMenEvenType.CROSS_WEEK };
	}

	@Override
	public void handleEventType(ZongMenConstants.ZongMenEvenType type, ZongMen info, Object... params) {
		switch (type) {
		case CROSS_DAY -> {
			// 每日重置 贡献度
			this.contribution = 0;
		}
		case CROSS_WEEK -> {
			this.weekContribution = 0;
		}
		}
	}
}
