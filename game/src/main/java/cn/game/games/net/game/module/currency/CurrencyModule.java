package cn.game.games.net.game.module.currency;

import java.util.Collection;

import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.Money;
import cn.game.protocol.generated.manager.ItemManager;
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
		checkConfig(configId);
		Asset money = Asset.get(configId);
		if (money.Type == 2) {
			addExp(configId, count);
		} else {
			currencyMap.add(configId, count);
		}
		return new Currency(configId, count);
	}

	@Override
	public boolean del(int configId, long count, OpType... args) {

		if (count <= 0) {
			return true;
		}
		return currencyMap.del(configId, count);
	}

	@Override
	public Currency get(int configId) {
		return new Currency(configId, getCount(configId));
	}

	public long get(Asset asset) {
		return getCount(asset.ID);
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Resource;
	}
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
		if (id == Asset.CatalogPoints.ID) {
			// 图鉴积分手动升级，只加经验，不升级
			currencyMap.add(id, count);
			return;
		}

		long curExp = currencyMap.getValue(id) + count;

		IntMapWrapper levelsMap = player.getPlayerModule().getExpLevelMap();

		ExpConfig expConfig = PlayerHelper.getExpConfig(id, (int) levelsMap.getValue(id), 0);
		ExpConfig nextExpConfig = PlayerHelper.getExpConfig(id, (int) (levelsMap.getValue(id) + 1), 0);
		while (expConfig != null && curExp >= expConfig.experience && nextExpConfig != null) {
			curExp -= expConfig.experience;
			levelsMap.add(id, 1);

			player.handleEvent(new GameEvent(EventTypeEnum.LevelUp, id, levelsMap.getValue(id),curExp));

			expConfig = PlayerHelper.getExpConfig(id, (int) levelsMap.getValue(id), 0);
			nextExpConfig = PlayerHelper.getExpConfig(id, (int) (levelsMap.getValue(id) + 1), 0);

//			levellog.info("opType[levelUp]playerId[{}]exp[{}]newLevel[{}]", player.getData().getPlayerId(), id,
//					levelsMap.getValue(id));
			if (id == Asset.playerExp.ID) {
				GameLogger.levelUp(player);
			}
		}
		// 不能升了，设置经验为最大
		if (expConfig != null && curExp > expConfig.experience) {
			curExp = expConfig.experience;
		}
		currencyMap.setValue(id, curExp);

		player.getGameClient().sendProtocol(PlayerExpLevelPush_01100050.newBuilder()
				.setExpLevel(ExpLevelInfo.newBuilder().setId(id).setExp((int) currencyMap.getValue(id)).setLevel(levelsMap.getValue(id)).build()).build());

	}

	@Override
	public void addCacheStackable(Currency item) {
		
	}

	@Override
	public void addCacheNoStackable(Currency item) {
		
	}

	@Override
	public void checkConfig(int id) {
		Asset.get(id);
	}

	public MapWrapper getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(MapWrapper currencyMap) {
		this.currencyMap = currencyMap;
	}

	public void setMaxCurrency() {
		Asset[] values = Asset.values();
		for (Asset asset : values) {
			if (asset.Type == 1) {
				currencyMap.setValue(asset.ID, Integer.MAX_VALUE / 2);
			} else if (asset.Type == 2) {
				if (asset == Asset.playerExp) {
					addExp(asset.ID, 1000000000);
				}
			} else if (asset.Type == 3) {
				currencyMap.setValue(asset.ID, Integer.MAX_VALUE / 2);
			}
		}

		// 在给些道具。
		ItemModule itemModule = player.getItemModule();
		Collection<ItemConfig> list = ItemManager.instance().list();
		for (ItemConfig itemConfig : list) {
			int itemType = itemConfig.ItemType;
			if (itemType == 1 || itemType == 2 || itemType == 3 || itemType == 10) {
				itemModule.add(itemConfig.ID, Integer.MAX_VALUE / 2, OpType.PressureTest);
			}
		}
	}
}
