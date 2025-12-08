package cn.game.games.net.game.module.develop.gem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.develop.equip.EquipPart;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.generated.config.GemAttrConfig;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.manager.GemAttrManager;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg;
import cn.game.util.Rnd;

public class GemModule extends AbstractItemNoStackModule<Gem> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay};

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
			case PLAYER_CREATE: {
				freeHigh=1;
				freeNormal=1;
				break;
			}
			case NewDay: {
				freeHigh=1;
				freeNormal=1;
				break;
			}
		}
	}

	@Override
	public void setInstanceExt(Gem instance) {
		// 随机宝石属性
		GemConfig gemConfig = GemManager.instance().get(instance.getConfigId());
		List<GemAttrConfig> posqualityList = GemAttrManager.instance().getPosqualityList(gemConfig.pos, gemConfig.quality);
		GemAttrConfig config = Rnd.randomElement(posqualityList, r -> r.weight);
		instance.getEntryEffectList().add(config.effectId);
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Gem;
	}

	@Override
	public Gem newInstance() {
		return new Gem();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		for (Gem obj : list()) {
			builder.addGems(obj.toGemInfo());
			builder.setGemGacheFreeTimesHigh(freeHigh);
			builder.setGemGacheFreeTimesNormal(freeNormal);
			builder.setGemGacheBaoDiNormal(baoDiNormal);
			builder.setGemGacheBaoDiHigh(baoDiHigh);
		}
	}

	@Override
	public void checkConfig(int id) {
		GemManager.instance().get(id);
	}

	public int getCountGTQualityWearCount(int quality,boolean gt) {
		EquipModule module = player.getModule(EquipModule.class);
		Set<Entry<Integer, EquipPart>> entrySet = module.getEquipPartMap().entrySet();
		int ret = 0;

		for (Entry<Integer, EquipPart> entry : entrySet) {
			Map<Long, Integer> gemPosMap = entry.getValue().getGemPosMap();
			for (Long gemUid : gemPosMap.keySet()) {
				Gem gem = get(gemUid);
				if (gem != null) {
					GemConfig config = GemManager.instance().get(gem.getConfigId());
					if (gt ? config.quality >= quality : config.quality < quality) {
						ret++;
					}
				}
			}
		}
		return ret;
	}

	/** 
	 * 获取所有宝石增加的 效果条目
	 * @return
	 */
	public List<Integer> getAllEntryEffect() {
		List<Integer> ret = new ArrayList<>();
		EquipModule equipModule = player.getEquipModule();
		Iterator<Entry<Integer, EquipPart>> iterator = equipModule.getEquipPartMap().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, EquipPart> next = iterator.next();
			EquipPart equipPart = next.getValue();
			Map<Long, Integer> gemPosMap = equipPart.getGemPosMap();
			for (Long gemUid : gemPosMap.keySet()) {
				Gem gem = get(gemUid);
				if (gem != null) {
					ret.addAll(gem.getEntryEffectList());
				}
			}
		}
		return ret;
	}

	/**
	 * 随机宝石属性
	 * @param uid
	 */
	public Gem gemXiLian(long uid)
	{
		// 随机宝石属性
		Gem instance = get(uid);
		if(instance==null) {
			log.error("gem not exist uid:{}",uid);
			return instance;
		}
		if(instance.isLock()) {
			log.error("gem is lock uid:{}",uid);
			return instance;
		}
		GemConfig gemConfig = GemManager.instance().get(instance.getConfigId());
		if(gemConfig.quality<5) {
			log.error("gem quality too low uid:{} quality:{}",uid,gemConfig.quality);
			return  instance;
		}
		EquipModule equipModule = player.getEquipModule();
		if(equipModule.getEquipPart(gemConfig.pos).getGemPosMap().containsKey(uid)) {
			log.error("gem already exist uid:{}",uid);
			return instance;
		}
		int index=gemConfig.quality-5;
		if(index>=GlobalConst.GemRefreshCost.length)
		{
			log.error("gem quality too high uid:{} quality:{}",uid,gemConfig.quality);
			return instance;
		}
		int cost =GlobalConst.GemRefreshCost[index][1];
		int Id   =GlobalConst.GemRefreshCost[index][2];
		if(!player.getCurrencyModule().isEnough(Id,cost)) {
			log.error("gem refresh cost not enough uid:{} cost:{}",uid,cost);
			return instance;
		}
		PlayerHelper.delResources(player, Id, cost, OpType.GemXilian);
		List<GemAttrConfig> posqualityList = GemAttrManager.instance().getPosqualityList(gemConfig.pos, gemConfig.quality);
		List<GemAttrConfig> posqualityListCopy= new ArrayList<>();
		for (GemAttrConfig gemAttrConfig : posqualityList) {
			if(gemAttrConfig.effectId!=instance.getEntryEffectList().get(0)) {
				posqualityListCopy.add(gemAttrConfig);
			}
		}
		GemAttrConfig config = Rnd.randomElement(posqualityListCopy, r -> r.weight);
		instance.getEntryEffectList().clear();
		instance.getEntryEffectList().add(config.effectId);
		return instance;
	}
    //宝石抽奖保底
	 int  baoDiNormal=0;
	 int  baoDiHigh=0;
	 int  freeNormal=1;
	 int  freeHigh=1;
	public List<RewardMsg.RewardInfo>  gemGache(int gemcount ,int pool,int costType)
	{
		if(!gemGache_Check(gemcount, pool, costType)) {
			return null;
		}
		int dropId=0;
		int dropIndex=0;// 处于保底状态是1  非保底状态是0
		int costId=0;
		if(pool==1) {
			dropIndex=baoDiNormal==10?1:0;
			dropId =GlobalConst.GemGachaNormalRandom[dropIndex];

		}else if(pool==2){
			dropIndex=baoDiHigh==10?1:0;
			dropId =GlobalConst.GemGachaHighRandom[dropIndex];
		}
		List<RewardMsg.RewardInfo>  list= new ArrayList<>();
		for (int i = 0; i < gemcount; i++)
		{
			list.addAll(PlayerHelper. addReward(player,dropId, OpType.GemGache));
			// 保底处理
			if(pool==1&&baoDiNormal==10) {
				baoDiNormal=0 ;
			}else if(pool==2&&baoDiHigh==10){
				baoDiHigh=0 ;
			}
			if(pool==1) {
				baoDiNormal++ ;
				dropIndex=baoDiNormal==10?1:0;
				dropId =GlobalConst.GemGachaNormalRandom[dropIndex];
			}else if(pool==2){
				baoDiHigh++ ;
				dropIndex=baoDiHigh==10?1:0;
				dropId =GlobalConst.GemGachaHighRandom[dropIndex];
			}
		}
		return  list;
	}

	public boolean gemGache_Check(int gemcount ,int pool,int costType)
	{
		if(gemcount!=1&&gemcount!=10) {
			return false;
		}
		if(pool!=1&&pool!=2) {
			return false;
		}
		if(costType!=1&&costType!=2&&costType!=3) {
			return false;
		}
		// 先检查消耗
		if(costType==1) {
			if(pool==1 && freeNormal==1) {
				//普通池免费合法
				freeNormal=0;
			} else if (pool==2 && freeHigh==1) {
				//高级池免费合法
				freeHigh=0;
			}else {
				//其余都不合法
				return false;
			}
		}else if (costType==2||costType==3) {
			//2 扣钱
			int uid =0;
			int cost =0;
			if(pool==1 ) {
				uid =GlobalConst.GemGachaNormalCost[costType-2][0];
				cost =GlobalConst.GemGachaNormalCost[costType-2][1];
			} else if (pool==2 ) {
				//高级池
				uid =GlobalConst.GemGachaHighCost[costType-2][0];
				cost =GlobalConst.GemGachaHighCost[costType-2][1];
			}
			cost=cost*gemcount;
			if(!player.getCurrencyModule().isEnough(uid,cost)) {
				log.error("gemGache refresh cost not enough uid:{} cost:{}",uid,cost);
				return false;
			}else {
				PlayerHelper.delResources(player, uid, cost, OpType.GemGache);
				return true;
			}
		}
		return false;
	}
}
