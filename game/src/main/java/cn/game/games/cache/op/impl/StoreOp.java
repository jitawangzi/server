package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.Store;
import cn.game.games.cache.entity.StoreData;
import cn.game.games.cache.op.face.IStoreOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.StoreDataMapper;
import cn.game.games.net.data.mapper.StoreMapper;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.StoreHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.award.RewardItem;
import cn.game.games.net.game.module.equip.EquipModule;
import cn.game.games.net.game.module.item.ItemModule;
import cn.game.games.net.game.module.store.StoreGoods;
import cn.game.games.util.DAO;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.CityEquipmentStoreConfig;
import cn.game.protocol.generated.config.CityItemStoreConfig;
import cn.game.protocol.generated.config.StoreConfig;
import cn.game.protocol.generated.config.StoreGiftConfig;
import cn.game.protocol.generated.enume.ResourceEnum;
import cn.game.protocol.generated.manager.CityEquipmentStoreManager;
import cn.game.protocol.generated.manager.CityItemStoreManager;
import cn.game.protocol.generated.manager.StoreGiftManager;
import cn.game.protocol.generated.manager.StoreManager;
import cn.game.protocol.manual.OldErrorMsgEnum;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.StoreMsg.StoreGoodsInfo;
import cn.game.protocol.protobuf.StoreMsg.StoreType;
import cn.game.util.DateUtil;
import cn.game.util.KryoUtils;
import cn.game.util.Pair;
import cn.game.util.Rnd;

public class StoreOp extends BasePlayerModule implements IStoreOp{
	// k-googsId或giftId；v-当前已购买次数
	private Map<Integer, Integer> id_buyCnt;
	/** 局间道具商店商品 */
	private Map<Long, StoreGoods> exploreRoomItemMap;
	/** 局间装备商店商品 */
	private Map<Long, StoreGoods> exploreRoomEquipMap;
	/** 主城道具商店商品 dictId->StoreGoods */
	private Map<Long, StoreGoods> cityItemMap;
	/** 主城装备商店商品 */
	private Map<Long, StoreGoods> cityEquipMap;
	
	private StoreData storeData;
	/** 首次刷新5天的商品 */
	public static int FIRST_REFRESH_HOW_DAY = 5;

	@Override
	public void init() {
		id_buyCnt = new HashMap<>();
		exploreRoomItemMap = new HashMap<>();
		exploreRoomEquipMap = new HashMap<>();
		cityItemMap = new HashMap<>();
		cityEquipMap = new HashMap<>();
	}
	
	@Override
	public void initLoadData(List<Store> stores, StoreData data) {
		init();
		for (Store store : stores) {
			int id = store.getGoodsId();
			int cnt = store.getCount();

			if (cnt <= 0) {
				delete(id);
				continue;
			}
			this.id_buyCnt.put(id, cnt);
		}
		if (data == null) {
			StoreData s = new StoreData();
			s.setPlayerId(this.playerId);
			DAO.insert(StoreDataMapper.class, s);
			
		} else {
			this.storeData = data;
			byte[] exploreRoomEquip = data.getExploreRoomEquip();
			if (exploreRoomEquip != null) {
				this.exploreRoomEquipMap = blobToObject(exploreRoomEquip);
			}
			byte[] exploreRoomItem = data.getExploreRoomItem();
			if (exploreRoomItem != null) {
				this.exploreRoomItemMap = blobToObject(exploreRoomItem);
			}
			byte[] cityItem = data.getCityItem();
			if (cityItem != null) {
				this.cityItemMap = blobToObject(cityItem);
			}
			byte[] cityEquip = data.getCityEquip();
			if (cityEquip != null) {
				this.cityEquipMap = blobToObject(cityEquip);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<Long, StoreGoods> blobToObject(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		return KryoUtils.deserializeWithVersion(bytes, HashMap.class);
	}

	/**
	 * 代币商店商品、礼包当前周期内已购数量（月卡特殊处理，在这里返回0）
	 * 
	 * @param goodsId
	 * @return
	 */
	public int getBuyCnt(int goodsId) {
		if (id_buyCnt.get(goodsId) != null) {
			return id_buyCnt.get(goodsId);
		}
		return 0;
	}

	// 购买后更新数据
	public void update(int goodsId, int addCnt) {
		// 第一次买
		if (id_buyCnt.get(goodsId) == null) {
			id_buyCnt.put(goodsId, addCnt);
			insert(Store.valueOf(playerId, goodsId, addCnt));
			return;
		}
		int newCnt = id_buyCnt.get(goodsId) + addCnt;
		id_buyCnt.put(goodsId, newCnt);
		update(Store.valueOf(playerId, goodsId, newCnt));
	}

	private void update(Store store) {
		DAO.update(StoreMapper.class, store);
	}

	private void insert(Store store) {
		DAO.insert(StoreMapper.class, store);
	}

	public void delete(int goodsId) {
		DAO.delete(StoreMapper.class, new Object[] { playerId, goodsId });
	}


	/**
	 * 检查礼包状态
	 * @param giftConf
	 * @return
	 */
	public boolean checkGiftState(StoreGiftConfig giftConf) {
		boolean onTime = DateUtil.between(giftConf.getUpTime(), giftConf.getDownTime());
		boolean isUp = giftConf.getUpDown();
		// 在有效期&&上架
		return onTime && isUp;
	}

	/**
	 * 判断礼包可买(月卡比较特殊，可买数量不限制)
	 * 
	 * @param giftId
	 * @return
	 */
	public boolean isCanBuyGift(int giftId) {
		StoreGiftConfig giftConf = StoreGiftManager.getInstance().getStoreGiftConfig(giftId);
		boolean onTime = DateUtil.between(giftConf.getUpTime(), giftConf.getDownTime());
		boolean onSale = giftConf.getUpDown();
		int giftCnt = giftConf.getLimitParameters2();
		int haveCnt = getBuyCnt(giftId);
		int canBuyCnt = giftCnt - haveCnt;
		return onTime && onSale && canBuyCnt > 0;
	}

	/**
	 * 新版本这个方法用不到了
	 * @param goodsId
	 * @param count
	 * @return
	 */
	public int buyGoods(int goodsId, int count) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		StoreConfig goodsConf = StoreManager.getInstance().getStoreConfig(goodsId);
		// 商品已下架
		if (!goodsConf.getUpDown()) {
			return OldErrorMsgEnum.not_sale.getId();
		}
		int maxBuyCnt = goodsConf.getLimitParameters2();
		// 商品单价
		int price = goodsConf.getTotalPrcie();
		// 订单价格
		int orderPrice = count * price;
		// 货币类型
		ResourceEnum moneyType = ResourceEnum.get(goodsConf.getItemUnitPrice());
		// 如果是爬塔商店
		if (moneyType == ResourceEnum.TowerToken) {
			ClimbingTowerOp climbTowerOp = player.getModule(ClimbingTowerOp.class);
			if (!climbTowerOp.isInitialized()) {
				return OldErrorMsgEnum.module_disabled.getId();
			}
		}
		// 订单数量异常
		if (count <= 0) {
			return OldErrorMsgEnum.illegal_request.getId();
		}
		// 购买数量超上限
		int haveCnt = getBuyCnt(goodsId);
		int canBuyCnt = maxBuyCnt - haveCnt;
		if (count > canBuyCnt) {
			return OldErrorMsgEnum.buy_over_limit.getId();
		}
		long moneyCnt = ItemHelper.getCount(player, moneyType.getId());
		log.info("player:[{}],will buy StoreToken's goods:[{}],count:[{}],buy before [{}],count:[{}]",
				playerId, goodsId, count, moneyType.getName(), moneyCnt);

		if (PlayerHelper.isEnough(playerId, moneyType.getId(), orderPrice)) {
			// 扣钱
			PlayerHelper.delResources(playerId, moneyType.getId(), orderPrice, ResourceConsumeEnum.BuyGoods);
			buyGoods(goodsConf, count);

		} else {
			log.info("player:[{}],buy StoreToken's goods:[{}],count:[{}] fail !! [{}] not enough", playerId,
					goodsId, count, moneyType.getName());
			return OldErrorMsgEnum.resource_not_enough.getId();
		}
		return OldErrorMsgEnum.ok.getId();

	}

	private void buyGoods(StoreConfig goodsConf, int orderCnt) {
		Player player = PlayerManager.getInstance().getPlayer(playerId);
		// 商品单价
		int price = goodsConf.getTotalPrcie();
		// 道具总数量(加资源用)
		int totalCnt = orderCnt * goodsConf.getNumber();
		// 订单价格
		int orderPrice = orderCnt * price;
		// 货币类型
		ResourceEnum moneyType = ResourceEnum.get(goodsConf.getItemUnitPrice());
		// 加道具
		PlayerHelper.addResources(playerId, goodsConf.getItemId(), totalCnt);
		long curMoneyCnt = ItemHelper.getCount(player, moneyType.getId());
		log.info("player:[{}],buy StoreToken's goods:[{}] succ count:[{}], buy after [{}],count:[{}],consume:[{}]x[{}]",
				playerId, goodsConf.getId(), orderCnt, moneyType.getName(), curMoneyCnt, moneyType.getName(),
				orderPrice);
		
		// 更新购买次数,次数不刷新的除外
		int refreshRule = goodsConf.getLimitParameters1();
		if (refreshRule == StoreHelper.refreshByNo) {
			return;
		}
		update(goodsConf.getId(), orderCnt);	
	}

	public Map<Integer, Integer> getId_buyCnt() {
		return id_buyCnt;
	}
	
	@Override
	public boolean genExploreRoomStores(int exploreLevelId) {
		saveStore(new StoreType[] { StoreType.EXPLORE_ROOM_ITEM, StoreType.EXPLORE_ROOM_EQUIP });
		return true;
	}
	
	@Override
	public void saveStore(StoreType[] types) {
		StoreData data = new StoreData();
		data.setPlayerId(this.playerId);
		for (StoreType storeType : types) {
			setStoreData(data, storeType);
		}
		DAO.updateSelective(StoreDataMapper.class, data);
	}

	private void setStoreData(StoreData update, StoreType type) {
		byte[] data = KryoUtils.serializeWithVersion(getGoodsMap(type));
		switch (type) {
			case EXPLORE_ROOM_ITEM:
				update.setExploreRoomItem(data);
				break;
	
			case EXPLORE_ROOM_EQUIP:
				update.setExploreRoomEquip(data);
				break;
				
			case CITY_ITEM:
				update.setCityItem(data);
				update.setCityItemRefreshTime(this.storeData.getCityItemRefreshTime());
				break;
				
			case CITY_EQUIP:
				update.setCityEquip(data);
				update.setCityEquipRefreshTime(this.storeData.getCityEquipRefreshTime());
				break;
	
			default:
				break;
		}

	}



	@Override
	public boolean delExploreRoomStores() {
		this.exploreRoomItemMap.clear();
		this.exploreRoomEquipMap.clear();
		saveStore(new StoreType[] { StoreType.EXPLORE_ROOM_ITEM, StoreType.EXPLORE_ROOM_EQUIP });
		return true;
	}

	/**
	 * 根据商店类型获取商店商品列表
	 * @param type
	 * @return
	 */
	public Map<Long, StoreGoods> getGoodsMap(StoreType type) {

		switch (type) {
			case EXPLORE_ROOM_ITEM:
				return exploreRoomItemMap;
	
			case EXPLORE_ROOM_EQUIP:
				return exploreRoomEquipMap;
	
			case CITY_ITEM:
				return cityItemMap;
	
			case CITY_EQUIP:
				return cityEquipMap;
	
			case STOREDAILY:// 日常商店
			case STORETOKEN:// 兑换商店
				// 商店礼包
			case STOREGIFT:
	
			default:
				return Collections.emptyMap();
		}
	}

	@Override
	public Collection<StoreGoodsInfo> seeStore(StoreType type) {
		if (type == StoreType.CITY_ITEM) {
			refreshCityItemStore();
		} else if (type == StoreType.CITY_EQUIP) {
			refreshCityEquipStore();
		}

		Map<Long, StoreGoods> goodsMap = getGoodsMap(type);
		if (goodsMap == null) {
			goodsMap = new HashMap<>();
		}
		return PbBuilder.buildStoreGoodsInfos(goodsMap.values());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Pair<OldErrorMsgEnum, RewardItem> buy(long uid, int count) {
		if (curStoreType == null) {
			return new Pair(OldErrorMsgEnum.store_need_refresh, null);
		}
		if (count <= 0) {
			return new Pair(OldErrorMsgEnum.illegal_request, null);
		}
		if (needRefresh(curStoreType)) {
			return new Pair(OldErrorMsgEnum.store_need_refresh, null);
		}
		Map<Long, StoreGoods> goodsMap = getGoodsMap(curStoreType);
		if (goodsMap == null) {
			return new Pair(OldErrorMsgEnum.player_data_not_found, null);
		}
		StoreGoods g = goodsMap.get(uid);
		if (g == null) {
			return new Pair(OldErrorMsgEnum.player_data_not_found, null);
		}
		int total = g.getCount();
		if (total < count) {
			return new Pair(OldErrorMsgEnum.illegal_request, null);
		}

		int costId = g.getCostId();
		int costCount = g.getCostCount();
		int totalCostCount = costCount * count;
		float discount = StoreHelper.getDiscount(playerId,curStoreType);
		if (discount > 0) {
			//触发事件
//			EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.MainCityDiscountBuyItem));
		}
		if (true) {
//			return new Pair(ErrorMsgEnum.illegal_request, null);
		}
		totalCostCount *= discount;
		totalCostCount = Math.round(totalCostCount);
		log.info(curStoreType.name() + ",当前折扣" + discount + ",商品uid:" + uid + ",原价:" + costCount * count + ",打折后商品价格:" + totalCostCount);
		
		boolean enough = PlayerHelper.isEnough(this.playerId, costId, totalCostCount);
		if (!enough) {
			return new Pair(OldErrorMsgEnum.resource_not_enough, null);
		}
		
		ItemModule itemModule = player.getModule(ItemModule.class);
		OldErrorMsgEnum e = OldErrorMsgEnum.ok;
		RewardItem reward = new RewardItem();
//		if (itemModule.getBagIdleSize() < 1) {
//			e = ErrorMsgEnum.item_bag_capacity_not_enough;
//			return new Pair(e, reward);
//		}

		if (g.isEquip()) {
			Equip equip = g.getEquip();
//			e = itemModule.addNewEquip(equip, false, false);
			if (e != OldErrorMsgEnum.ok) {
				return new Pair(e, reward);
			}
			List<RewardInfo> ret = new ArrayList<>();
			reward.setEquip(equip);
//			ret.add(reward);
			PlayerHelper.sendProtcol(playerId, PbBuilder.buildRewardPush(ret));

		//自动/非自动使用型的道具
		} else {
			int itemId = getItemId(curStoreType, g.getId());
			PlayerHelper.addResources(playerId, itemId, count);
			reward = RewardItem.valueOf(itemId, count);
		}

		// 普通道具库存归零,装备道具删除
		int curCount = total - count;
		g.setCount(curCount);
		if (curCount <= 0 && g.isEquip()) {
			goodsMap.remove(uid);
		}
		
		PlayerHelper.delResources(this.playerId, costId, totalCostCount, ResourceConsumeEnum.BuyGoods);
//		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.BuyItems, g.getId(), count));
		saveStore(new StoreType[] { curStoreType });
		//触发事件(局间)
//		EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.ExploreBuyItem, costId, totalCostCount));
		return new Pair(OldErrorMsgEnum.ok, reward);
	}

	/**
	 * 该商店类型的道具,购买后是否存储在城市
	 * @param type
	 * @return
	 */
	private boolean isStorageToCtiy(StoreType type) {
		switch (type) {
			case EXPLORE_ROOM_ITEM:
			case EXPLORE_ROOM_EQUIP:
				return false;
	
			case CITY_ITEM:
			case CITY_EQUIP:
				return true;
	
			default:
				throw new IllegalArgumentException("未知商店类型: " + type);
		}
	}

	/**
	 * 根据道具商店类型,获取商品对应的itemId
	 * @param type
	 * @param dictId
	 * @return
	 */
	private int getItemId(StoreType type, int dictId) {
		switch (type) {
			case CITY_ITEM:
				CityItemStoreConfig cityItemStoreConfig = CityItemStoreManager.getInstance().getCityItemStoreConfig(dictId);
				return cityItemStoreConfig.getItemId();
	
			default:
				throw new IllegalArgumentException("商店类型错误,此商店不卖道具: " + type);
		}

	}

	@Override
	public void insertStoreData() {
		StoreData data = new StoreData();
		data.setPlayerId(this.playerId);
		DAO.insert(StoreDataMapper.class, data);
		this.storeData = data;
	}

	/**
	 * 当前查看的商店类型
	 */
	private StoreType curStoreType;
	private boolean genExploreRoomStore = true;

	public void setCurStoreType(StoreType curStoreType) {
		this.curStoreType = curStoreType;
		if (genExploreRoomStore) {
//			genExploreRoomStores(32001);
			genExploreRoomStore = false;
		}
	}

	/**
	 * 刷新主城装备商店
	 * 
	 * 	2)商品库存限制固定为1； 
	 * 	3)需要配置每个商品的解锁条件，一句主线进度解锁，未解锁的商品不在列表中显示； 
	 * 2.商品上架
	 * 	1)每次刷新时移除货架上剩余的商品； 
	 * 	2)商品按游戏天数自动上架； 
	 * 	3)所有商品统一每天刷新一次； 
	 * 	4)每次上架每件数量固定一个； 
	 * 	5)装等限制
	 * 		a)商品列表中只有符合当前装等范围的装备才会被上架； 
	 * 		b)以主线任务完成度作为改变装等范围的条件；
	 * 	#例如：当主线进度到达12%，切换装等范围至100~200。当主线进度到达24%，切换装等范围至200~300；
	 * 	6)每次上架时，查找全部符合当前装等范围的装备，并依据权重从中随机10件放入货架；
	 */
	private void refreshCityEquipStore() {
		Integer lastRefreshTime = this.storeData.getCityEquipRefreshTime();
		if (!needRefresh(lastRefreshTime)) {
			return;
		}
		Player player = PlayerManager.getInstance().getPlayer(this.playerId);
		int day = player.getData().getDay();
		
		this.cityEquipMap.clear();
		
		// TODO 当前装等范围当前无法获取,先临时用此值
		int curEquipmentLevelRangeMin = 1;
		int curEquipmentLevelRangeMax = 100;

		int start = curEquipmentLevelRangeMin;
		int end = curEquipmentLevelRangeMax;
		List<CityEquipmentStoreConfig> confs = new ArrayList<>();
		// 根据装等范围,筛选可以出的装备
		for (int level = start; level <= end; level++) {
			List<CityEquipmentStoreConfig> levelList = CityEquipmentStoreManager.getInstance().getLevelList(level);
			if (levelList == null) {
				continue;
			}
			confs.addAll(levelList);
		}
		
		// 去掉没有解锁的
		Iterator<CityEquipmentStoreConfig> iterator = confs.iterator();
		while (iterator.hasNext()) {
			CityEquipmentStoreConfig next = iterator.next();
			int condition = next.getCondition();
//			boolean ok = PlayerHelper.checkCondition(this.playerId, Arrays.asList(condition));
			// TODO 先不校验是否解锁
			boolean ok = true;
			if (!ok) {
				iterator.remove();
			}
		}

		EquipModule equipOp = player.getModule(EquipModule.class);
		// 依据权重从中随机10件放入货架；
		List<Integer> indexs = Rnd.randomWeighableIndexsNonRepeating(confs, 10);
		for (int index : indexs) {
			if (index == -1) {
				continue;
			}
			CityEquipmentStoreConfig conf = confs.get(index);
			Equip equip = equipOp.gen(conf.getEquipmentId());
			StoreGoods s = StoreGoods.valueOf(conf, equip);
			this.cityEquipMap.put(s.getUid(), s);
		}
		
		this.storeData.setCityEquipRefreshTime(day);
		this.saveStore(new StoreType[] { StoreType.CITY_EQUIP });
	}
	
	/**
	 * 商店是否需要刷新
	 * @param storeType
	 * @return
	 */
	private boolean needRefresh(StoreType storeType) {
		switch (storeType) {
			case CITY_ITEM:		return needRefresh(this.storeData.getCityItemRefreshTime());
			case CITY_EQUIP:	return needRefresh(this.storeData.getCityEquipRefreshTime());
			default:			return false;
		}
	}

	/**
	 * 商店是否需要刷新
	 * @param lastRefreshTime
	 * @return
	 */
	private boolean needRefresh(Integer lastRefreshTime) {
		if (lastRefreshTime == null) {
			return true;
		}
		Player player = PlayerManager.getInstance().getPlayer(this.playerId);
		int playerDay = player.getData().getDay();
		return playerDay > lastRefreshTime;
	}

	/**
	 * 主城商店商品补货
	 * @param storeGoods 商品
	 */
	private void refreshCityItemGoodsNum(StoreGoods storeGoods) {
		int id = storeGoods.getId();
		CityItemStoreConfig cityItemStoreConfig = CityItemStoreManager.getInstance().getCityItemStoreConfig(id);
		int cycle = cityItemStoreConfig.getCycle();
		int cycleNumbers = cityItemStoreConfig.getCycleNumbers();
		int max = cityItemStoreConfig.getStockMax();
		
		Player player = PlayerManager.getInstance().getPlayer(this.playerId);
		int nowDay = player.getData().getDay();
		int lastRefreshDay = storeGoods.getLastRefresh();
		// 未到补货周期
		if (nowDay - lastRefreshDay < cycle) {
			return;
		}
		
		int howCycle = (nowDay - lastRefreshDay) / cycle;
		int curCount = storeGoods.getCount();
		int addCount = howCycle * cycleNumbers;
		
		int newCount = curCount + addCount;
		if (newCount > max) {
			newCount = max;
		}
		storeGoods.setCount(newCount);
		storeGoods.setLastRefresh(lastRefreshDay + howCycle * cycle);
	}

	/**
	 * 刷新主城道具商店
	 */
	private void refreshCityItemStore() {
		Integer lastRefresh = this.storeData.getCityItemRefreshTime();
		if (!needRefresh(lastRefresh)) {
			return;
		}
		Player player = PlayerManager.getInstance().getPlayer(this.playerId);
		int day = player.getData().getDay();
		int extraAdd = StoreHelper.getExtraAdd(this.playerId, StoreType.CITY_ITEM);

		Collection<CityItemStoreConfig> list = CityItemStoreManager.getInstance().list();
		for (CityItemStoreConfig conf : list) {
			int condition = conf.getCondition();
//			boolean unlock = PlayerHelper.checkCondition(this.playerId, Arrays.asList(condition));
			// TODO 先不校验是否解锁
			boolean unlock = true;
			if (!unlock) {
				continue;
			}
			// 刷新周期
			int cycle = conf.getCycle();
			// 每次补货数量
			int cycleNum = conf.getCycleNumbers();

			int id = conf.getId();
			StoreGoods storeGoods = cityItemMap.get((long) id);
			if (storeGoods == null) {
				int initNum = (FIRST_REFRESH_HOW_DAY / cycle) * cycleNum;
				storeGoods = StoreGoods.valueOf(conf, initNum);
				cityItemMap.put((long) id, storeGoods);
			}

			refreshCityItemGoodsNum(storeGoods);
			// 额外增加的库存,可以超过配置的库存上限
			int newCount = storeGoods.getCount() + extraAdd;
			if (newCount < 0) {
				newCount = 0;
			}
			storeGoods.setCount(newCount);
		}

		this.storeData.setCityItemRefreshTime(day);
		this.saveStore(new StoreType[] { StoreType.CITY_ITEM });
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}
	@Override
	public void initFromDbAfter() {

	};
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
	
	
}