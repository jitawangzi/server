package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.games.cache.entity.ZongmenData;
import cn.game.games.core.SimplePlayer;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuildBasicConfig;
import cn.game.protocol.generated.config.GuildPermissionsConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.RankType;
import cn.game.protocol.generated.manager.GuildBasicManager;
import cn.game.protocol.generated.manager.GuildPermissionsManager;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenPersonalInfo;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenSharedInfo;
import cn.game.protocol.protobuf.ZongMenMsg.ZongMenShowInfo;
import cn.game.util.DateUtil;
import cn.game.util.GameUtil;
import cn.game.util.JsonUtil;
import cn.game.util.LockUtil;
import cn.game.util.RedisUtil;

/**
 * @ClassName ZongMen
 *
 * @description:  宗门对象
 * @author: ly
 * @create: 2025-02-05 14:45 @Version 1.0
 */
public class ZongMen {
	/**数据库 t_zongmen 表的数据*/
	private ZongmenData data;
	/**宗门模块的数据 */
	private ZongMenModuleData module;
	/**保存数据到数据库的时间戳*/
	private long saveDataTimer;

	public ZongMen() {
	}

	public ZongMen(ZongmenData data) {
		this.data = data;
		module = JsonUtil.parseObjectWithType(this.data.getModules());
		module.registerAllModuleEventHandler();
	}

	public void init(ZongMenMsg.createZongMenRequest_40000005 req, long newZongMenId, long createPlayerId) {
		SimplePlayer creator = PlayerHelper.getSimplePlayer(createPlayerId);

		module = new ZongMenModuleData();
		saveDataTimer = System.currentTimeMillis() + ZongMenConstants.SAVE_ZONG_MEN_DATA_TIMER;
		// 初始化 Zongmen 对象
		data = new ZongmenData();
		data.setName(req.getName());
		data.setId(newZongMenId);
		data.setLv((byte) 1);
		data.setIcon(req.getIcon() == 0 ? GlobalConst.ZongmenIconRes : req.getIcon());
		data.setNotice(StringUtils.isEmpty(req.getNotice()) ? GlobalConst.ZongmenGonggao : req.getNotice());
		data.setNotification(StringUtils.isEmpty(req.getDeclaration()) ? GlobalConst.ZongmenXuanyan : req.getDeclaration());
		data.setCreateTime(DateUtil.getTimeByPattern(new Date(), DateUtil.pattern_en));
		data.setExp(0);
		data.setServerId(creator.getServerId());

		// 初始化各个模块
		module = new ZongMenModuleData();
		module.init();
		module.afterInit(this);
		module.registerAllModuleEventHandler();

		module.handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_CREATE, this, createPlayerId, creator.getName());
		module.handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_LEVEL_UP, this, getLv());
		joinZongMen(createPlayerId, ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU);
	}

	public void joinZongMen(long joinPlayerId, int position) {
		ZongMenMember member = new ZongMenMember(joinPlayerId, position);
		module.addMember(member, this);
		module.handleEvent(ZongMenConstants.ZongMenEvenType.JOIN_ZONG_MEN, this, member, member.getName());
	}

	/** 
	 * 判断一个宗门是否可以自动加入
	 * 一般需要人数没满，并且设置了自动加入
	 * @return
	 */
	public boolean canAutoJoin() {
		return !isFull() && isAutoJoin();
	}

	public ZongmenData getData() {
		return data;
	}

	public void setData(ZongmenData data) {
		this.data = data;
	}

	public long getSaveDataTimer() {
		return saveDataTimer;
	}

	public void setSaveDataTimer(long saveDataTimer) {
		this.saveDataTimer = saveDataTimer;
	}

	public void updateModuleData() {
		data.setModules(JsonUtil.toJsonStringWithType(module));
	}

	public long getId() {
		return data.getId();
	}

	public int getLv() {
		return data.getLv();
	}

	public void setLv(int lv) {
		this.data.setLv((byte) lv);
	}

	public String getName() {
		return data.getName();
	}

	public void setName(String name) {
		data.setName(name);
	}

	public void setIcon(int icon) {
		data.setIcon(icon);
	}

	public int getIcon() {
		return data.getIcon();
	}

	public void setNotice(String notice) {
		data.setNotice(notice);
	}

	public String getNotice() {
		return data.getNotice();
	}

	public int getExp() {
		return data.getExp();
	}

	public void setExp(int exp) {
		data.setExp(exp);
	}

	public long getCreateTimer() {
		return DateUtil.parseDate(data.getCreateTime()).getTime();
	}

	public void handleEvent(ZongMenConstants.ZongMenEvenType zongMenEvenType, Object... params) {
		module.handleEvent(zongMenEvenType, this, params);
	}

	public SimpleZongMen toSimpleZongMen() {
		SimpleZongMen simpleZongMen = new SimpleZongMen();
		simpleZongMen.setId(data.getId());
		simpleZongMen.setLv(data.getLv());
		simpleZongMen.setName(data.getName());
		simpleZongMen.setIcon(data.getIcon());
		simpleZongMen.setTotalPower((int) module.getTotalPower());
		simpleZongMen.setNum(module.menMemberMap.size());
		simpleZongMen.setIsAutoJoin(getModule().setting.getAutoJoin());
		simpleZongMen.getApplyPidList().addAll(module.applyList);
		return simpleZongMen;
	}

	public ZongMenMsg.ZongMenShowInfo toShowProto() {
		ZongMenShowInfo.Builder showInfoBuilder = ZongMenShowInfo.newBuilder();
		showInfoBuilder.setSimpleInfo(toSimpleZongMen().toProto());
		module.menMemberMap.forEach((pid, member) -> {
			showInfoBuilder.addMembers(member.toProto().build());
		});
		return showInfoBuilder.build();
	}

	public ZongMenMsg.ZongMenSharedInfo toSharedProto(long playerId) {
		ZongMenSharedInfo.Builder builder = ZongMenSharedInfo.newBuilder();
		ZongMenMember member = getMember(playerId);
		if (member == null) {
			return builder.build(); // 非宗门成员
		}
		builder.setExp(getExp());
		builder.setBargain(module.bargain.toProto());

		// 封装 ZongMenSetting
		ZongMenMsg.ZongMenSettingProto.Builder settingProto = module.setting.toProto();
		settingProto.setNotification(data.getNotification());

		builder.setSetting(settingProto.build());
		builder.setLiveness(module.liveness);
		// 申请列表，看权限发

		GuildPermissionsConfig permissionsConfig = GuildPermissionsManager.instance().get(member.position);
		if (permissionsConfig.Approval) {
			List<SimplePlayer> simplePlayers = RedisLocalCache.getInstance()
					.multiGet(CacheType.PLAYER_SIMPLE, GameUtil.transformToStringArray(module.applyList));
			for (SimplePlayer simplePlayer : simplePlayers) {
				if (simplePlayer != null) {
					builder.addApplyPlayerList(simplePlayer.toSimplePlayerInfo());
				}
			}
		}
		return builder.build();
	}

	public ZongMenMsg.ZongMenPersonalInfo toPersonalProto(long playerId) {
		ZongMenPersonalInfo.Builder builder = ZongMenPersonalInfo.newBuilder();

		ZongMenMember member = getMember(playerId);
		if (member == null) {
			return builder.build(); // 非宗门成员
		}
		builder.setIsBargain(member.isBargain);
		builder.setIsBargainBuy(member.isBargainBuy);
		return builder.build();
	}

	/** 
	 * 
	 * @param playerId 一般表示该宗门内的某个成员id，如果是0，表示非工会成员
	 * @return
	 */
	public ZongMenMsg.ZongMenAllInfo toProto(long playerId) {
		ZongMenMsg.ZongMenAllInfo.Builder builder = ZongMenMsg.ZongMenAllInfo.newBuilder();
		builder.setShowInfo(toShowProto());
		builder.setSharedInfo(toSharedProto(playerId));
		builder.setPersonalInfo(toPersonalProto(playerId));
		return builder.build();
	}

	public long callTotalPower() {
		return module.getTotalPower();
	}

	public boolean isHasMember(long playerId) {
		return module.menMemberMap.containsKey(playerId);
	}

	public boolean isFull() {
		GuildBasicConfig basicConfig = GuildBasicManager.instance().get(getLv());
		return module.menMemberMap.size() >= basicConfig.NumberMax;
	}

	public boolean hasApply(long playerId) {
		return module.applyList.contains(playerId);
	}

	public void applyJoin(long playerId) {
		module.applyList.add(playerId);
	}

	public boolean isAutoJoin() {
		return module.setting.getAutoJoin() == 1;
	}

	public ZongMenMember getMember(long playerId) {
		return module.menMemberMap.get(playerId);
	}

	// 解散宗门
	public void dissolveZongMen() {
		// 删除所有玩家
		module.removeAllMember();
		// 删除宗门排行榜
		RankService.getInstance().removeRankAsync(RankType.Battle);
		// 删除宗门名称 id 映射
		delZongMenNameIdRedisData();
		// 删除宗门 simple 对象
		RedisLocalCache.getInstance().deleteAsync(CacheType.ZONG_MEN_SIMPLE_DATA.key(getId()));
		// 删除数据库宗门
		DAO.delete(data);
		ZongMenManager.getInstance().delZongMen(data.getId());
		ZongMenManager.log.info("解散宗门成功 id:" + getId() + " name:" + getName() + "");
	}

	void delZongMenNameIdRedisData() {
		RedisUtil.delete(ZongMenHelper.getNameKey(getName()));
	}

	public ZongMenModuleData getModule() {
		return module;
	}

	public void quitZongMen(ZongMenMember member, String playerName) {
		module.removeMember(member.getPlayerId());
		module.handleEvent(ZongMenConstants.ZongMenEvenType.QUIT_ZONG_MEN, this, playerName);
	}

	public int getPositionMemberNum(int position) {
		return (int) module.menMemberMap.values().stream().filter(member -> member.position == position).count();
	}

	public void addExp(int addExp) {
		int totalExp = getExp() + addExp;
		GuildBasicConfig basicConfig = GuildBasicManager.instance().get(getLv());
		GuildBasicConfig nextConfig = GuildBasicManager.instance().getNullable(getLv() + 1);
		if (basicConfig == null) {
			return; // 配置错误
		}
		while (totalExp >= basicConfig.Exp) {
			totalExp -= basicConfig.Exp;
			nextConfig = GuildBasicManager.instance().getNullable(getLv() + 1);
			if (nextConfig == null) {
				break;
			}
			setLv(getLv() + 1);
			basicConfig = GuildBasicManager.instance().getNullable(getLv());
			if (basicConfig == null) {
				break;
			}
			handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_LEVEL_UP, this, getLv());
		}
		setExp(totalExp);
	}

	public void addZongMenAsset(long playerId, int id, int num) {
		if (id == Asset.ZongMenPoint.ID) {// 宗门活跃度
			module.setLiveness(module.liveness + num);
		} else if (id == Asset.ZongMenExp.ID) {// 宗门经验
			addExp(num);
		} else if (id == Asset.ZongMenContribute.ID) {// 个人贡献
			ZongMenMember member = getMember(playerId);
			member.addcontribution(num);

		}
	}

	/** 
	 * 增加成员
	 * @param targetPidList
	 * @param playerName
	 */
	public void addMember(List<Long> targetPidList) {
		List<Long> joinPidList = new ArrayList<>();
		for (Long targetPid : targetPidList) {
			if (isFull()) {
				break;
			}
			// 删除申请记录
			module.removeApply(targetPid);
			// 加入宗门
			joinZongMen(targetPid, ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
			joinPidList.add(targetPid);
		}
		// 通知被加入的玩家 加入宗门
		ZongMenHelper.broadcastNotifyMsgToPlayer(
				ZongMenMsg.notifyJoinZongMen_40000044.newBuilder().setZongMen(toSimpleZongMen().toProto()).build(),
				PbProtocol.notifyJoinZongMen_40000044, joinPidList);
		// 更新宗门战斗力排行榜
		ZongMenManager.getInstance().saveZongMenTotalPowerRank(this);
	}
	
	/**
	 * 审批 同意 加入
	 *
	 * @param targetPidListList 被加入的列表
	 * @param playerName 审批人
	 */
	public void addMemberAuth(List<Long> targetPidList, String playerName) {
		addMember(targetPidList);
		
	}

	// 删除申请记录
	public void removeApplyAuth(List<Long> targetPidListList, String playerName) {
		targetPidListList.forEach(targetPid -> {
			module.removeApply(targetPid);
		});
	}

	// 踢人
	public void kickMember(List<Long> targetPidListList, String playerName) {
		PlayerManager.getInstance().batchGetSimplePlayerListFromRedisAsync(targetPidListList).onSuccess(res -> {
			res.forEach(simplePlayer -> {
				module.removeMember(simplePlayer.getId());
				handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_KICK_MEMBER, playerName, simplePlayer.getName());
			});
			// 更新宗门战斗力排行榜
			ZongMenManager.getInstance().saveZongMenTotalPowerRank(this);
		}).onFailure(err -> {
			err.printStackTrace();
		});
	}

	/**
	 * 检查宗主转让
	 * - 自动转让宗主规则：
	 *   - 宗主3天未上线则进行自动转让宗主（ZongmenSuzerainTransfer）；
	 *   - 自动转让：职位最高且在3天内上线的玩家，同一职位转让至贡献度最高的玩家，贡献度相同则转让给战力最高的玩家；
	 */
	public void checkZongZhuTransfer(long now) {
		List<ZongMenMember> memberList = new ArrayList<>(module.menMemberMap.values());

		memberList.sort((m1, m2) -> {
			if (m1.position == m2.position) {
				if (m1.getTotalContribution() == m2.getTotalContribution()) {
					return m1.getPower() - m2.getPower();
				}
				return m1.getTotalContribution() - m2.getTotalContribution();
			}
			return m1.position < m2.position ? 1 : -1;
		});
		ZongMenMember zongZhu = memberList.get(0);
		PlayerManager.getInstance().getSimplePlayerFromRedisAsync(zongZhu.getPlayerId()).onSuccess(player -> {
			if (DateUtil.diffDays(now, player.getLastLoginTimer()) >= GlobalConst.ZongmenSuzerainTransfer) {
				if (memberList.size() <= 1) {// 宗门没人
					return;
				}
				List<Long> targetPidListList = new ArrayList<>();
				PlayerManager.getInstance().batchGetSimplePlayerFromRedisAsync(targetPidListList).thenAccept(simplePlayerMap -> {
					// 被转让的人
					ZongMenMember targetZongZhu = null;
					for (int i = 1; i < memberList.size(); i++) {
						long lastLoginTimer = simplePlayerMap.get(memberList.get(i).getPlayerId()).getLastLoginTimer();
						if (DateUtil.diffDays(now, lastLoginTimer) < GlobalConst.ZongmenSuzerainTransfer) {
							targetZongZhu = memberList.get(i);
							break;
						}
					}
					if (targetZongZhu == null) {
						return;
					}
					// 转让宗主
					zongZhuTransfer(zongZhu, targetZongZhu);

				}).exceptionally(err -> {
					err.printStackTrace();
					return null;
				});
			}
		}).onFailure(err -> {
			err.printStackTrace();
		});

	}

	public void zongZhuTransfer(ZongMenMember zongZhu, ZongMenMember targetZongZhu) {
		zongZhu.setPosition(ZongMenConstants.ZONG_MEN_POSITION_BANG_ZHONG);
		handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE, zongZhu.playerId,
				ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU, zongZhu.position);
		int targetOldPosition = targetZongZhu.position;
		targetZongZhu.setPosition(ZongMenConstants.ZONG_MEN_POSITION_ZONG_ZHU);
		handleEvent(ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE, targetZongZhu.playerId, targetOldPosition,
				targetZongZhu.position);
	}

}
