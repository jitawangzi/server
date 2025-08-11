package cn.game.games.net.cross.guild.service;

import java.util.List;

import cn.game.core.net.remote.RemoteCrossServerInterface;
import cn.game.games.net.cross.guild.Guild;
import cn.game.games.net.cross.guild.dto.MemberAuthRequest;
import cn.game.games.net.cross.guild.dto.GuildSettingRequest;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildAllInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildServiceInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import io.vertx.core.Future;

public interface GuildServiceInterface extends RemoteCrossServerInterface {

	/**
	 * 创建公会
	 * @param request 创建公会请求
	 * @param createPlayerId 创建玩家ID
	 * @return 新公会信息
	 */
	Future<GuildServiceInfo> createGuild(long createPlayerId, String name, String notice, String declaration, int icon);

	/**
	 * 获取公会信息
	 * @param guildId 公会ID
	 * @return 公会信息
	 */
	Guild getGuild(long guildId);

	/** 
	 * 获取某个公会的展示数据
	 * @param guildId
	 * @return
	 */
	GuildShowInfo getGuildShowInfo(long guildId);

	/** 
	 * 获取工会成员可以看到的工会共享数据 
	 * @param guildId
	 * @return
	 */
	GuildServiceInfo getGuildAllInfoForMember(long guildId);

	/**
	 * 申请加入公会
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @return 公会信息
	 */
	GuildServiceInfo applyJoinGuild(long guildId, long playerId);

	/**
	 * 解散公会
	 * @param guildId 公会ID
	 * @param playerId 操作玩家ID
	 * @return 是否成功
	 */
	void dissolveGuild(long guildId, long playerId);

	/**
	 * 设置公会配置
	 * @param guildId 公会ID
	 * @param request 设置请求
	 * @return 是否成功
	 */
	boolean setGuildSetting(long guildId, GuildSettingRequest request);

	/**
	 * 设置成员职位
	 * @param guildId 公会ID
	 * @param operatorId 操作者ID
	 * @param targetPlayerId 目标玩家ID
	 * @param position 新职位
	 * @return 是否成功
	 */
	void setMemberPosition(long guildId, long operatorId, long targetPlayerId, int position);

	/**
	 * 退出公会
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @return 是否成功
	 */
	void quitGuild(long guildId, long playerId, String playerName);

	/**
	 * 成员权限管理
	 * @param guildId 公会ID
	 * @param request 权限请求
	 * @return 是否成功
	 */
	void updateMemberAuth(long guildId, MemberAuthRequest request);

	/**
	 * 增加公会资产，经验、活跃度等
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @param assetId 资产ID
	 * @param value 资产值
	 * @return 是否成功
	 */
	Future<?> addGuildAsset(long guildId, long playerId, int assetId, int value);
	
	/** 
	 * 增加某个成员的贡献值
	 * @param guildId
	 * @param playerId
	 * @param value
	 * @return
	 */
	Future<?> addMemberContribute(long guildId, long playerId, int value);

	/**
	 * 领取公会活跃度奖励
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @param indexList 奖励索引列表
	 * @return 是否成功
	 */
	void receiveActiveReward(long guildId, long playerId, List<Integer> indexList);

	/**
	 * 公会砍价
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @return 当前的砍价id， 砍掉了多少的数量
	 */
	int[] bargain(long guildId, long playerId);

	/** 
	 * 查看工会的砍价物品当前购买价格
	 * @param guildId
	 * @return  当前的砍价id， 当前购买价格
	 */
	int[] getBargainPrice(long guildId);

	/** 
	 * 玩家随机加入一个可以加的公会
	 * @param playerId
	 * @return 成功加入的公会，如果为null，表示没有能加入的公会
	 */
	GuildServiceInfo randomJoin(long playerId);

}