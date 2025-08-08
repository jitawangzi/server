package cn.game.games.net.cross.zongmen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.game.core.cache.CacheType;
import cn.game.core.cache.RedisLocalCache;
import cn.game.games.core.SimplePlayer;
import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.protocol.protobuf.ZongMenMsg;
import cn.game.util.Rnd;
import io.vertx.core.Future;

/**    
 * 宗门砍价
 * 2025年2月14日 12:02:19
 * @author SYQ
 */
public class ZongMenBargain implements ZongMenConstants.ZongMenEventHandler {
	private long zongMenId;
	/** 累计砍下来的数量 */
	private int bargainTotalNum;
	/** 累计砍价次数 */
	private int bargainTimes;
	/** 刷新出来的砍价物品id ： GuildBargain 表id */
	private int bargainItemId;
	/** 砍价数量记录 */
	private Map<String, Integer> bargainLogMap = new HashMap<>();

	void init() {
		initBargain();
	}

	private void refreshBargain() {
		bargainLogMap.clear();
		bargainTotalNum = 0;
		bargainTimes = 0;
		ZongMen zongMen = ZongMenManager.getInstance().getZongMen(zongMenId); 
		List<GuildBargainConfig> levelList = GuildBargainManager.instance().getLevelList(zongMen.getLv()); 
		GuildBargainConfig guildBargainConfig = Rnd.randomElement(levelList); 
		bargainItemId = guildBargainConfig.ID;
	}
	public void initBargain() {
		List<GuildBargainConfig> levelList = GuildBargainManager.instance().getLevelList(1); 
		GuildBargainConfig guildBargainConfig = Rnd.randomElement(levelList); 
		bargainItemId = guildBargainConfig.ID;
	}

	/**
	 * 执行砍价操作
	 * @param member 砍价成员
	 * @return 砍价数量
	 */
	public int performBargain(long playerId,int level) {
		GuildBargainConfig guildBargainConfig = GuildBargainManager.instance().get(bargainItemId);
		int curMax = guildBargainConfig.Price[1] - bargainTotalNum;
		int[] range = bargainTimes >= guildBargainConfig.Bargain.length - 1
				? guildBargainConfig.Bargain[guildBargainConfig.Bargain.length - 1]
				: guildBargainConfig.Bargain[bargainTimes];
		float rangeLow = range[0] / 100f;
		float rangeHigh = range[1] / 100f;
		double nextDouble = Rnd.nextDouble(rangeLow, rangeHigh); 
		
		int count = (int) ((curMax - guildBargainConfig.PriceLow[1]) * nextDouble);
		
		bargainTotalNum += count;
		bargainTimes++;

		// 异步记录砍价日志
		Future<SimplePlayer> simplePlayerFuture = RedisLocalCache.getInstance().getAsync(CacheType.PLAYER_SIMPLE.key(playerId));
		simplePlayerFuture.onSuccess(simplePlayer -> {
			addBargainLog(simplePlayer.getName(), count);
		});

		return count;
	}

	public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
		return new ZongMenConstants.ZongMenEvenType[] { ZongMenConstants.ZongMenEvenType.CROSS_DAY };
	}

	@Override
	public void handleEventType(ZongMenConstants.ZongMenEvenType type, ZongMen info, Object... params) {
		switch (type) {
		case CROSS_DAY -> refreshBargain();
		}
	}

	public int getBargainTotalNum() {
		return bargainTotalNum;
	}

	public int getBargainItemId() {
		return bargainItemId;
	}

	public void setBargainTotalNum(int bargainTotalNum) {
		this.bargainTotalNum = bargainTotalNum;
	}

	public void setBargainItemId(int bargainItemId) {
		this.bargainItemId = bargainItemId;
	}

	public void addBargainLog(String playerName, int num) {
		bargainLogMap.compute(playerName, (k, v) -> v == null ? num : v + num);
	}

	public ZongMenMsg.ZongMenBargainSharedInfo toProto() {
		return ZongMenMsg.ZongMenBargainSharedInfo.newBuilder()
				.setTotalBargainCount(bargainTotalNum)
				.setTotalMemberCount(bargainLogMap.size())
				.setBargainItemId(bargainItemId)
				.putAllBargainLogMap(bargainLogMap)
				.build();
	}

	public void setZongMenId(long zongMenId) {
		this.zongMenId = zongMenId;
	}
	
}
