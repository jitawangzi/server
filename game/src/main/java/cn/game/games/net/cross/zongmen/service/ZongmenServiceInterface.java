package cn.game.games.net.cross.zongmen.service;

import java.util.List;

import cn.game.core.net.remote.RemoteCrossServerInterface;
import cn.game.games.net.cross.zongmen.ZongMen;
import cn.game.games.net.cross.zongmen.dto.MemberAuthRequest;
import cn.game.games.net.cross.zongmen.dto.ZongmenSettingRequest;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenAllInfo;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenShowInfo;
import io.vertx.core.Future;

public interface ZongmenServiceInterface extends RemoteCrossServerInterface{

	/**
	 * 创建宗门
	 * @param request 创建宗门请求
	 * @param createPlayerId 创建玩家ID
	 * @return 新宗门信息
	 */
	Future<ZongMen> createZongmen(ZongMenMsg.createZongMenRequest_40000005 req,long createPlayerId);

	/**
	 * 获取宗门信息
	 * @param zongMenId 宗门ID
	 * @return 宗门信息
	 */
	ZongMen getZongmen(long zongMenId);
	
	/** 
	 * 获取某个宗门的展示数据
	 * @param zongMenId
	 * @return
	 */
	ZongMenShowInfo getZongmenShowInfo(long zongMenId);

	/**
	 * 申请加入宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 宗门信息
	 */
	ZongMenAllInfo applyJoinZongmen(long zongMenId, long playerId);

	/**
	 * 解散宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 操作玩家ID
	 * @return 是否成功
	 */
	void dissolveZongmen(long zongMenId, long playerId);

	/**
	 * 设置宗门配置
	 * @param zongMenId 宗门ID
	 * @param request 设置请求
	 * @return 是否成功
	 */
	boolean setZongmenSetting(long zongMenId, ZongmenSettingRequest request);

	/**
	 * 设置成员职位
	 * @param zongMenId 宗门ID
	 * @param operatorId 操作者ID
	 * @param targetPlayerId 目标玩家ID
	 * @param position 新职位
	 * @return 是否成功
	 */
	void setMemberPosition(long zongMenId, long operatorId, long targetPlayerId, int position);

	/**
	 * 退出宗门
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @return 是否成功
	 */
	void quitZongmen(long zongMenId, long playerId, String playerName);

	/**
	 * 成员权限管理
	 * @param zongMenId 宗门ID
	 * @param request 权限请求
	 * @return 是否成功
	 */
	void updateMemberAuth(long zongMenId, MemberAuthRequest request);

	/**
	 * 更新宗门资产
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param assetId 资产ID
	 * @param value 资产值
	 * @return 是否成功
	 */
	void updateZongmenAsset(long zongMenId, long playerId, int assetId, int value);

	/**
	 * 领取宗门活跃度奖励
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param indexList 奖励索引列表
	 * @return 是否成功
	 */
	void receiveActiveReward(long zongMenId, long playerId, List<Integer> indexList);


	/**
	 * 宗门砍价
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 砍价次数
	 */
	int bargain(long zongMenId, long playerId);

	/**
	 * 砍价购买
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @return 是否成功
	 */
	void buyBargain(long zongMenId, long playerId);

	/**
	 * 更新贡献度
	 * @param zongMenId 宗门ID
	 * @param playerId 玩家ID
	 * @param value 贡献度值
	 * @return 是否成功
	 */
	void updateContributeValue(long zongMenId, long playerId, int value);
	
	/** 
	 * 玩家随机加入一个可以加的宗门
	 * @param playerId
	 * @return 成功加入的宗门，如果为null，表示没有能加入的宗门
	 */
	ZongMenAllInfo randomJoin(long playerId);

}