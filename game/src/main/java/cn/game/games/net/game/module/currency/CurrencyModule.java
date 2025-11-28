package cn.game.games.net.game.module.currency;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.Chapter;
import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.config.EquipConfig;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.generated.manager.EquipManager;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.DungeonTypeEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.PlayerMsg.ExpLevelInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.PlayerMsg.PlayerExpLevelPush_01100050;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.IntMapWrapper;
import cn.game.util.MapWrapper;

public class CurrencyModule extends GoodsModule<Currency> {
//	private static final Logger levellog = LoggerFactory.getLogger("levelLog");
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.NewDay };
	/** 货币,key:  {@link Money}*/
	private MapWrapper currencyMap = new MapWrapper();

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {

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
		if (count < 0) {
			return false;
		}
		if (count == 0) {
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
		return RewardInfo.newBuilder().setAsset(AssetInfo.newBuilder().setId(reward.getConfigId()).setCount(reward.getCount())).build();

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
			player.getGameClient()
					.sendProtocol(PlayerExpLevelPush_01100050.newBuilder()
							.setExpLevel(ExpLevelInfo.newBuilder()
									.setId(id)
									.setExp((int) currencyMap.getValue(id))
									.setLevel(player.getLevel(Asset.get(id)))
									.build())
							.build());
			return;
		}
		if (id == Asset.GuildExp.ID) {
            // 仙会经验在CrossServer增加		
			return;
		}

		long curExp = currencyMap.getValue(id) + count;

		IntMapWrapper levelsMap = player.getPlayerModule().getExpLevelMap();

		ExpConfig expConfig = PlayerHelper.getExpConfig(id, (int) levelsMap.getValue(id), 0);
		ExpConfig nextExpConfig = PlayerHelper.getExpConfig(id, (int) (levelsMap.getValue(id) + 1), 0);
		while (expConfig != null && curExp >= expConfig.experience && nextExpConfig != null) {
			curExp -= expConfig.experience;
			levelsMap.add(id, 1);

			player.handleEvent(EventTypeEnum.LevelUp, id, levelsMap.getValue(id), curExp);

			expConfig = PlayerHelper.getExpConfig(id, (int) levelsMap.getValue(id), 0);
			nextExpConfig = PlayerHelper.getExpConfig(id, (int) (levelsMap.getValue(id) + 1), 0);

//			levellog.info("opType[levelUp]playerId[{}]exp[{}]newLevel[{}]", player.getData().getPlayerId(), id,
//					levelsMap.getValue(id));
			if (id == Asset.playerExp.ID) {
				GameLogger.levelUp(player);
			}
			GameLogger.ComonLevelUp(player, id,  levelsMap.getValue(id));
		}
		// 不能升了，设置经验为最大
		if (expConfig != null && curExp > expConfig.experience) {
			curExp = expConfig.experience;
		}
		currencyMap.setValue(id, curExp);

		player.getGameClient()
				.sendProtocol(PlayerExpLevelPush_01100050.newBuilder()
						.setExpLevel(ExpLevelInfo.newBuilder()
								.setId(id)
								.setExp((int) currencyMap.getValue(id))
								.setLevel(levelsMap.getValue(id))
								.build())
						.build());

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

	@Override
	public Currency removeFromCache(int id) {
		throw new UnsupportedOperationException("不支持通过id删除货币"); // 货币不支持通过id删除
	}

	@Override
	public Currency removeFromCache(long id) {
		throw new UnsupportedOperationException("不支持通过uid删除货币"); // 货币不支持通过uid删除
	}
}
