package cn.game.games.net.cross.guild.service;

import java.util.List;

import cn.game.core.net.remote.RemoteCrossServerInterface;
import cn.game.games.net.cross.guild.Guild;
import cn.game.games.net.cross.guild.GuildMember;
import cn.game.games.net.cross.guild.dto.GuildSettingRequest;
import cn.game.games.net.cross.guild.dto.MemberAuthRequest;
import cn.game.protocol.protobuf.GuildMsg.GuildServiceInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import io.vertx.core.Future;

public interface GuildServiceInterface extends RemoteCrossServerInterface {

	/** 
	 * 创建工会
	 * @param createPlayerId  创建人id
	 * @param name	公会名
	 * @param notice
	 * @param declaration
	 * @param icon
	 * @param joinType 加入类型 1 快速加入、2 需要验证加入、3 不可加入；
	 * @return 新公会信息
	 */
	Future<GuildServiceInfo> createGuild(long createPlayerId, String name, String notice, String declaration, int icon,int joinType);

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
	
	String getGuildName(long guildId);

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
	void updateMemberAuth(long guildId, long operatorId,String operatorName, int optType, List<Long> targetPidList);

	/**
	 * 增加公会资产，经验、活跃度等
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @param assetId 资产ID
	 * @param value 资产值
	 * @return 如果升级了，返回新的等级，否则返回0
	 */
	Future<Integer> addGuildAsset(long guildId, long playerId, int assetId, int value);
	
	/** 
	 * 增加某个成员的贡献值
	 * @param guildId
	 * @param playerId
	 * @param value
	 * @return
	 */
	@Deprecated
	Future<?> addMemberContribute(long guildId, long playerId, int value);

	/**
	 * 公会砍价
	 * @param guildId 公会ID
	 * @param playerId 玩家ID
	 * @return 当前的砍价id， 砍掉了多少的数量,今天第多少次砍价
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
	
	Future<?> donate(long guildId, long playerId, int donateType);
	
	/** 
	 *  是否有可以处理的入会申请
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	boolean hasPendingApplication(long guildId, long playerId);
	
	GuildMember getMember(long guildId, long playerId);
	
	/** 
	 * 公会服务跨天，只在测试使用
	 */
	void testGuildNewDay(); 
	
}