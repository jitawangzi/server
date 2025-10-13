package cn.game.games.net.game.module.guild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import cn.game.games.cache.entity.GuildJoin;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.data.mapper.GuildJoinMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.generated.config.GuildDonateConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.manager.GuildDonateManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.GuildMsg;
import cn.game.protocol.protobuf.GuildMsg.GuildPersonalInfo;
import cn.game.protocol.protobuf.GuildMsg.GuildShowInfo;
import cn.game.protocol.protobuf.PlayerMsg;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import io.vertx.core.Future;

public class GuildModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] {EventTypeEnum.PLAYER_CREATE,EventTypeEnum.GetItem, EventTypeEnum.NewDay, EventTypeEnum.LoginFinish,
			EventTypeEnum.GuildDonate };
	/** 上一个公会的id */
	private long lastId;
	/** 公会反复加入次数 * */
	int disbandCount;

	/**
	 * 下次加入公会的时间 第二次及后续解散时，宗主需要1小时才可加入其它公会（GuildSuzerainCD）
	 * 第二次及后续退出时，需要1小时才可加入其它公会（GuildMemberCD）*
	 */
	long nextJoinTimer;
	/** 加入时间 */
	long joinTime;
	/** 申请过加入公会列表 */
	List<Long> applyJoinList = new ArrayList<>();
	private boolean inited = false;

	/** 每日捐献次数记录 */
	private IntMapWrapper donateMap = new IntMapWrapper();
	/** 当日砍价次数 */
	private int bargainCount;
	/** 砍价后是否购买 */
	private boolean isBargainBuy;
	private transient GuildJoin guildJoin;

	public int getDisbandCount() {
		return disbandCount;
	}

	public void setDisbandCount(int disbandCount) {
		this.disbandCount = disbandCount;
	}

	public long getNextJoinTimer() {
		return nextJoinTimer;
	}

	public void setNextJoinTimer(long nextJoinTimer) {
		this.nextJoinTimer = nextJoinTimer;
	}

	public long getJoinTime() {
		return joinTime;
	}

	@Override
	public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {
		// 公会信息
		builder.setGuildId(player.getGuildId());
		builder.addAllApplyGuildIds(applyJoinList);
		builder.setGuildQuitCount(disbandCount);
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	protected Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { GuildJoinMapper.class };
	};

	@Override
	protected String[] defaultSelectMethodName() {
		return new String[] { MapperConstant.selectByPrimaryKey };
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		GuildJoin guildJoin = (GuildJoin) iterator.next();
		this.guildJoin = guildJoin;
	}

	@Override
	public void onLogin() {
		checkGuild();
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE -> { 
			// 砍价默认为1次
			player.getCurrencyModule().setCount(Asset.GuildBargain.ID, 1);
		}
		case NewDay -> { // 跨天刷新公会任务
			initGuildTask();
			donateMap.clear();
			// 砍价重置为1次
			player.getCurrencyModule().setCount(Asset.GuildBargain.ID, 1);
			player.getCurrencyModule().setCount(Asset.GuildQuestPoint.ID, 0);
			bargainCount = 0;
			isBargainBuy = false;
		}
		case GetItem -> {
			// 可能更新公会资源
			if (player.getGuildId() > 0) {
				int id = event.get(0);
				int count = event.get(1);
				if (id == Asset.GuildExp.ID || id == Asset.GuildPoint.ID || id == Asset.GuildContribute.ID) {
					GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(player.getGuildId());
					Future<Integer> newLevelFuture = guildProxy.addGuildAsset(player.getGuildId(), playerId, id, count);
					newLevelFuture.onComplete(ar -> {
						if (ar.succeeded()) {
							int newLevel = ar.result() == null ? 0 :ar.result() ;
							if (newLevel > 0) {
								// 公会升级了
								GameLogger.guildUpgrade(player, count, newLevel) ; 
							}
						} else {
							log.error("addGuildAsset failed! guildId={}, playerId={}, assetId={}, value={}, cause={}",
									player.getGuildId(), playerId, id, count, ar.cause().getMessage());
						}
					});
				}
			}
			;
		}
		case GuildDonate -> {
			GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(player.getGuildId());
			guildProxy.donate(player.getGuildId(), playerId, event.get(0));
		}
		}
	}

	public void initGuildTask() {
		QuestModule questModule = player.getQuestModule();
		questModule.refreshQuest(QuestTypeEnum.Guild,true);
	}

	private void checkGuild() {
		// 现在没有公会
		if (guildJoin == null) {
			// 离线期间被退出了
			if (lastId > 0) {
				quit();
			} else {
				// 一直没有,忽略
			}
		} else {
			// 有公会
			if (guildJoin.getGuildId() != lastId) {
				join(player.getGuildId());
			} else {
				// 已经有公会了,并且没有变化
			}
		}
	}

	/** 
	 * 初次新加入一个工会
	 */
	private void initFirstTime() {

		initGuildTask();
		player.getShopModule().refreshShopByShopType(17);
	}

	/** 
	 * 由一个工会退出后，  加入到另外一个工会时
	 */
	private void change() {
//        refreshGuildTask();
//        player.getShopModule().refreshGuildShop(17);
	}

	public void kickGuild(GuildMsg.GuildQuitPush_40000024 quitGuildMsg) {
		quit();
		player.getGameClient().sendProtocol(quitGuildMsg);
	}

	public void joinGuild(GuildMsg.GuildJoinPush_40000044 req) {
		join(req.getGuild().getId());
	}

	public void joinAndPush(GuildMsg.GuildJoinPush_40000044 req) {
		joinGuild(req);
		player.getGameClient().sendProtocol(req);
	}

	/** 
	 * 退出一个公会
	 */
	public void quit() {
		if (lastId == 0) {
			return;
		}
//		lastId = 0;

		if (guildJoin != null) {
			guildJoin.delete(); 
			guildJoin = null;
			player.getData().setUnionId(0);
		}
		player.getCurrencyModule().setCount(Asset.GuildContribute.ID, 0);
	}

	public void join(long guildId) {
		if (guildJoin != null) {
			return ; // 已经有公会了
		}
//		if (guildId == lastId) {
//			return;
//		}
		boolean isFirstJoin = true;
		// 之前有加入过公会，不是第一次加入
		if (lastId > 0) {
			change();
			isFirstJoin = false;
		} else {
			// 初次加入
			initFirstTime();
		}

		lastId = guildId;
		applyJoinList.clear();
		inited = true;
		
		if (guildJoin == null) {
			guildJoin = new GuildJoin(); 
			guildJoin.setPlayerId(playerId);
			guildJoin.setGuildId(guildId);
			guildJoin.setCreateTime(DateUtil.currentTimeMillis());
			player.getData().setUnionId(guildId);
		}
        GameLogger.guildJoin(player, guildId);
		player.handleEvent(EventTypeEnum.GuildJoin, guildId, isFirstJoin);
	}

	public List<Long> getApplyJoinList() {
		return applyJoinList;
	}

	public void setApplyJoinList(List<Long> applyJoinList) {
		this.applyJoinList = applyJoinList;
	}

	public void removeApplyJoinList(Long guildId) {
		applyJoinList.remove(guildId);
	}

	public IntMapWrapper getDonateMap() {
		return donateMap;
	}

	public GuildPersonalInfo toPersonalInfo() {
		GuildPersonalInfo.Builder builder = GuildPersonalInfo.newBuilder();
		builder.setIsBargainBuy(isBargainBuy);
		builder.setBargainCount(bargainCount);
		builder.putAllDonate(donateMap.getMap());

		return builder.build();
	}

	public boolean isInited() {
		return inited;
	}

	public int getBargainCount() {
		return bargainCount;
	}

	public boolean isBargainBuy() {
		return isBargainBuy;
	}

	public void setBargainCount(int bargainCount) {
		this.bargainCount = bargainCount;
	}

	public void setBargainBuy(boolean isBargainBuy) {
		this.isBargainBuy = isBargainBuy;
	}

	public long getGuildId() {
		if (guildJoin == null) {
			return 0;
		}
		return guildJoin.getGuildId();
	}

	public boolean hasGuild() {
		return guildJoin != null;
	}
	
	public boolean hasRed() {
		if (!hasGuild()) {
			return false ; 
		}
        Collection<GuildDonateConfig> guildDonateConfigs = GuildDonateManager.instance().list();
        for (GuildDonateConfig guildDonateConfig : guildDonateConfigs) {
			if (guildDonateConfig.Price.length == 0 && donateMap.getValue(guildDonateConfig.ID) < guildDonateConfig.DayCount) {
				return true;
			}
		}
		if (player.getCurrencyModule().getCount(Asset.GuildBargain.ID) > 0) {
			return true;
		}
		GuildServiceInterface guildProxy = ServerHelper.getGuildProxy(getGuildId()); 
		return guildProxy.hasPendingApplication(player.getGuildId(), playerId); 
	}
}
