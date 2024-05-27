package cn.game.games.net.game.module.currency;

import java.util.ListIterator;

import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.Money;
import cn.game.protocol.generated.manager.FundPassUpgradeManager;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.PlayerMsg.ExpLevelInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.PlayerMsg.PlayerExpLevelPush_01100050;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.IntMapWrapper;
import cn.game.util.MapWrapper;

public class CurrencyModule extends GoodsModule<Currency, Currency> {
//	private static final Logger levellog = LoggerFactory.getLogger("levelLog");
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.NewDay };
	/** 货币,key:  {@link Money}*/
	private MapWrapper currencyMap = new MapWrapper();
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {

		player.getCurrencyModule().setCount(Asset.dailyIntegral.ID, 0);

		switch (event.getType()) {

		case NewDay: {
			player.getCurrencyModule().setCount(Asset.dailyIntegral.ID, 0);
			player.getCurrencyModule().setCount(Asset.DailyPoint.ID, 0);
			break;
		}
		}

	}

	@Override
	public long getCount(int configId) {
		return currencyMap.getValue(configId);
	}

	public void setCount(int configId, long count) {
		currencyMap.setValue(configId, count);
	}

	public boolean has(int configId) {
		return currencyMap.hasValue(configId);
	}

	@Override
	public Currency add(int configId, int count, OpType opType) {

		if (count < 0) {
			return new Currency(configId, 0);
		}
		ItemHelper.checkConfig(configId);
		Asset money = Asset.get(configId);
		if (money.Type == 2) {
			addExp(configId, count);
		} else {
			currencyMap.add(configId, count);
		}
		return new Currency(configId, count);
	}

	@Override
	public boolean del(int configId, int count, OpType... args) {

		if (count <= 0) {
			return true;
		}
		return currencyMap.del(configId, count);
	}

	@Override
	public Currency get(int configId) {
		return new Currency(configId, getCount(configId));
	}

	public Currency get(Asset asset) {
		return new Currency(asset.ID, getCount(asset.ID));
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Resource;
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {

	}
	@Override
	public void initFromDbAfter() {

	};
	@Override
	public Currency newInstance() {
		return new Currency();
	}

	@Override
	public RewardInfo toRewardInfo(Currency reward) {
		return RewardInfo.newBuilder()
				.setAsset(AssetInfo.newBuilder().setId(reward.getConfigId()).setCount(reward.getCount())).build();

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.putAllAssets(currencyMap.getMap());
	}

	@Override
	public void initAddCache(Currency item) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeCache(Currency item) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean del(long uid, OpType... args) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Currency get(long uid) {
		throw new UnsupportedOperationException();
	}

	public void addExp(int id, int count) {
		long curExp = currencyMap.getValue(id) + count;

		IntMapWrapper levelsMap = player.getPlayerModule().getExpLevelMap();

		ExpConfig expConfig = getExpConfig(id, (int) levelsMap.getValue(id));
		ExpConfig nextExpConfig = getExpConfig(id, (int) (levelsMap.getValue(id) + 1));
		while (expConfig != null && curExp >= expConfig.experience && nextExpConfig != null) {
			curExp -= expConfig.experience;
			levelsMap.add(id, 1);

			player.handleEvent(new GameEvent(EventTypeEnum.LevelUp, id, levelsMap.getValue(id)));

			expConfig = getExpConfig(id, (int) levelsMap.getValue(id));
			nextExpConfig = getExpConfig(id, (int) (levelsMap.getValue(id) + 1));

//			levellog.info("opType[levelUp]playerId[{}]exp[{}]newLevel[{}]", player.getData().getPlayerId(), id,
//					levelsMap.getValue(id));
			if (id == Asset.playerExp.ID) {
				GameLogger.levelUp(player);
			}
		}
		if (expConfig != null && curExp > expConfig.experience) {
			curExp = expConfig.experience;
		}
		currencyMap.setValue(id, curExp);

		player.getGameClient().sendProtocol(PlayerExpLevelPush_01100050.newBuilder()
				.setExpLevel(ExpLevelInfo.newBuilder().setId(id).setExp((int) currencyMap.getValue(id)).setLevel(levelsMap.getValue(id)).build()).build());

	}

	public ExpConfig getExpConfig(int id, int level) {
		if (id == Asset.playerExp.ID) {
			return UserUpgradeManager.instance().getNullable(level);
		} else if (id == Asset.FundPass.ID) {
			return FundPassUpgradeManager.instance().getNullable(level);
		}
		throw new IllegalArgumentException("没有实现的经验id： " + id);
	}

	@Override
	public void addCacheStackable(Currency item) {
		
	}

	@Override
	public void addCacheNoStackable(Currency item) {
		
	}

	public MapWrapper getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(MapWrapper currencyMap) {
		this.currencyMap = currencyMap;
	}

}
