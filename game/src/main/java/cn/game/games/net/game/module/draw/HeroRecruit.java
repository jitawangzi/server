package cn.game.games.net.game.module.draw;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.Hero;
import cn.game.protocol.generated.config.*;
import cn.game.protocol.generated.manager.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.guarantee.Guarantee;
import cn.game.games.net.game.module.guarantee.GuaranteeModule;
import cn.game.protocol.generated.enume.GuaranteeTypeEnum;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroInfo;
import cn.game.util.DateUtil;
import cn.game.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeroRecruit {
	private static final Logger log = LoggerFactory.getLogger(HeroRecruit.class);
	/**随机出来的3英雄*/
	private List<DrawHeroInPool> drawHeroInPoolList = new ArrayList<DrawHeroInPool>();
	/** 英雄刷新时间 */
	private int heroRefreshTime;
	/** 招募次数 */
	private int recruitCount;
	/** 没有招募到高品质的次数 */
	private int noHighQualityRecruitCount;
	/** 回流增益次数   登录触发回流 设置为N次  之后随机基础的3英雄会带有回流增益 */
	private int huiLiuCount;
	private int multiple = 1; // 招募时选择的倍数
	private int luckyValue; // 当前保底幸运值
	private int luckyValueMax; // 最大保底幸运值
	private int luckyValueQuality; // 当前保底品质--幸运值颜色

	@JsonIgnore
	private transient Player player;


	public HeroRecruit() {
	};

	public void setPlayer(Player player) {
		this.player = player;
	}
	/** 
	 * 不需要刷新的情况
	 * @return
	 */
	public boolean notRefresh() {
		return heroRefreshTime == 0 ; 
	}

	public boolean checkRefresh() {
		if (notRefresh()) {
			return false;
		}
		int t = GlobalConst.GachaRefreshTime - (DateUtil.currentTimeSeconds() - this.getHeroRefreshTime());
		return t <= 0;
	}
	public void refresh() {
		drawHeroInPoolList.clear();
		int baodi = -1;
        if(huiLiuCount>0 ) {
            huiLiuCount--;
        }
		GuaranteeModule guaranteeModule = player.getGuaranteeModule();
		Guarantee guarantee = guaranteeModule.get(GuaranteeTypeEnum.DrawRefresh);
		int guaranteeIndex = -1;
		int guaranteeRandomId = -1;
		if (guarantee.isInGuarantee()) {
			GuaranteeConfig guaranteeConfig = GuaranteeManager.instance().get(guarantee.getId());
			guarantee.reset();
			guaranteeIndex = Rnd.nextInt(3);
			guaranteeRandomId = guaranteeConfig.effectiveParam;
			RandomGivenConfig randomGivenConfig = RandomGivenManager.instance().get(guaranteeRandomId);
			baodi = randomGivenConfig.RandomParameterGroupId[0];
		}
		
		for (int i = 0; i < 3; i++) {
//			int tmpRandomId = randomId;
//			if (guaranteeIndex > -1 && guaranteeIndex == i) {
//				tmpRandomId = guaranteeRandomId;
//			}
			//if(Rnd.nextInt(10000) <= GlobalConst.HeroRecruitBagDetect)
			//{
			//	bagDetect(i);
			//}else {
			if(baodi > -1 && guaranteeIndex == i)
			{
				log.info("commonDrop:保底权重 baodi={} ", baodi);
				int group =baodi;
				List<RandomGroupConfig> randomGroupIDList = RandomGroupManager.instance().getRandomGroupIDList(group);
				RandomGroupConfig groupConfig = Rnd.randomWeighableElement(randomGroupIDList);
				Goods goods = new Goods(groupConfig.AssetID, Rnd.randomInRange(groupConfig.Several));
				drawHeroInPoolList.add(new DrawHeroInPool(goods.getId(), goods.getCount(), 0, i, noHighQualityRecruitCount, huiLiuCount > 0));
			}else {
				commonDrop(i);
			}

		//}
		}
		heroRefreshTime = DateUtil.currentTimeSeconds();

	}
	public void commonDrop(int i)
	{
		int vipLevel = player.getVipLevel();
		var randomGivenConfig = GlobalConst.UltimateCardDraw;
		int lanweight= randomGivenConfig[3][1];
		int lanDrop= randomGivenConfig[3][0];

		int ziweight= randomGivenConfig[2][1];
		int ziDrop= randomGivenConfig[2][0];

		int jinweight= randomGivenConfig[1][1];
		int jinDrop= randomGivenConfig[1][0];

		int hongweight= randomGivenConfig[0][1];
		int hongDrop= randomGivenConfig[0][0];
		if(vipLevel>=3)
		{
			lanweight+=GlobalConst.UltimateCardDrawVIPWeight[vipLevel-3][3];
			ziweight+=GlobalConst.UltimateCardDrawVIPWeight[vipLevel-3][2];
			jinweight+=GlobalConst.UltimateCardDrawVIPWeight[vipLevel-3][1];
			hongDrop+=GlobalConst.UltimateCardDrawVIPWeight[vipLevel-3][0];
		}
		List<Integer> weightList = new ArrayList<>();
		weightList.add(lanweight);
		weightList.add(ziweight);
		weightList.add(jinweight);
		weightList.add(hongweight);
		List<Integer> dropList = new ArrayList<>();
		dropList.add(lanDrop);
		dropList.add(ziDrop);
		dropList.add(jinDrop);
		dropList.add(hongDrop);
		log.info("commonDrop:随机权重 lanweight={} ziweight={} jinweight={} hongweight={}", lanweight,ziweight,jinweight,hongweight);
		int randomIndex = Rnd.randomIndex(weightList);
		int group =dropList.get(randomIndex);
		List<RandomGroupConfig> randomGroupIDList = RandomGroupManager.instance().getRandomGroupIDList(group);
		RandomGroupConfig groupConfig = Rnd.randomWeighableElement(randomGroupIDList);
		Goods goods = new Goods(groupConfig.AssetID, Rnd.randomInRange(groupConfig.Several));
		drawHeroInPoolList.add(new DrawHeroInPool(goods.getId(), goods.getCount(), 0, i, noHighQualityRecruitCount, huiLiuCount > 0));
	}
	public void initHeros() {
		if (!drawHeroInPoolList.isEmpty()) {
			return ;
		}
		int[][] gachaFirstTime = GlobalConst.GachaFirstTime;
		for (int i = 0; i < gachaFirstTime.length; i++) {
			int itemId = gachaFirstTime[i][0];
			int itemCount = gachaFirstTime[i][1];
			drawHeroInPoolList.add(new DrawHeroInPool(itemId, itemCount, 0,i,noHighQualityRecruitCount,huiLiuCount>0));
		}
	}

	public DrawHeroInfo buildDrawHeroInfo() {
		DrawHeroInfo.Builder resp = DrawHeroInfo.newBuilder();
		drawHeroInPoolList.forEach(
				drawHeroInPool -> {
					resp.addItemId(drawHeroInPool.getItemId());
					resp.addItemCount(drawHeroInPool.getItemCount());
					if(drawHeroInPool.getIsDraw()==1)
					{
						resp.addRecruitedPos(drawHeroInPool.getPosition());
					}
				}
		);

		resp.setCanMultiple(recruitCount >= GlobalConst.GachaTripleTime);
		if (notRefresh()) {
			resp.setFreeRefreshRemaningSeconds(-1);
		}else {
			int t = GlobalConst.GachaRefreshTime - (DateUtil.currentTimeSeconds() - this.getHeroRefreshTime());
			resp.setFreeRefreshRemaningSeconds(t > 0 ? t : 0);
		}

		GuaranteeModule guaranteeModule = player.getGuaranteeModule();
		Guarantee guarantee = guaranteeModule.get(GuaranteeTypeEnum.DrawRefresh);
		GuaranteeConfig guaranteeConfig = GuaranteeManager.instance().get(guarantee.getId());

		resp.setLuckyValue(guarantee.getCount());
		resp.setLuckyValueMax(guaranteeConfig.count);
		resp.setLuckyValueQuality(guaranteeConfig.displayParam);

		resp.setMultiple(this.getMultiple());
		return resp.build();
	}


	public int getHeroRefreshTime() {
		return heroRefreshTime;
	}

	public int getRecruitCount() {
		return recruitCount;
	}

	public void setRecruitCount(int recruitCount) {
		this.recruitCount = recruitCount;
	}

	public int getMultiple() {
		return multiple;
	}

	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}

	public int getLuckyValue() {
		return luckyValue;
	}

	public void setLuckyValue(int luckyValue) {
		this.luckyValue = luckyValue;
	}

	public int getLuckyValueMax() {
		return luckyValueMax;
	}

	public void setLuckyValueMax(int luckyValueMax) {
		this.luckyValueMax = luckyValueMax;
	}

	public int getLuckyValueQuality() {
		return luckyValueQuality;
	}

	public void setLuckyValueQuality(int luckyValueQuality) {
		this.luckyValueQuality = luckyValueQuality;
	}

	public  List<DrawHeroInPool> getDrawHeroInPoolList() {
		return drawHeroInPoolList;
	}
    /**
     * 是否已招募
     * @return
     */
	public boolean isRecruited() {
		for (int i = 0; i < drawHeroInPoolList.size(); i++) {
			if(drawHeroInPoolList.get( i).getIsDraw()==1) {
				return true;
			}
		}
        return false;
    }
	/**
	 * 获取已招募的英雄位置
	 * @return
	 */
	public List<Integer> getRecruitedPosList() {
		List<Integer> resp = new ArrayList<>();
		drawHeroInPoolList.forEach(
				drawHeroInPool -> {
					if(drawHeroInPool.getIsDraw()==1)
					{
						resp.add(drawHeroInPool.getPosition());
					}
				}
		);
		return resp;
	}
	public List<Integer> setIsDraw(int pos) {
		List<Integer> resp = new ArrayList<>();
		drawHeroInPoolList.forEach(
				drawHeroInPool -> {
					if(drawHeroInPool.getPosition()==pos)
					{
						drawHeroInPool.setIsDraw(1);
					}
				}
		);
		return resp;
	}
	// 根据身上卡牌的品质 删选
	public void bagDetect(int pos) {
		// 筛选出初始品质大于等于3的英雄
		List<Hero> all = new ArrayList<>();
		List<Integer> lowlist = new ArrayList<>();
		// 计算平均等级
		float avgLevel = 0;
		int totalLevel = 0;
		int highestLevel = 0;
		int lowestLevel = 0;
		Hero lowesthero = null;
		var heroList = player.getHeroModule().list().stream().toList();
		for (int i = 0; i < heroList.size(); i++) {
			var hero = heroList.get(i);
			HeroConfig heroConfig = HeroManager.instance().get(hero.getConfigId());
			if (heroConfig.InitialQuality >= 4) {
				all.add(hero);
				totalLevel += hero.getLevel();
				if (highestLevel < hero.getLevel()) {
					highestLevel = hero.getLevel();
				}
				if (lowestLevel==0||lowestLevel > hero.getLevel()) {
					lowestLevel = hero.getLevel();
					lowesthero = hero;
				}
			}
		}
		avgLevel = 1.0f * totalLevel / all.size();
		for (int i = 0; i < all.size(); i++) {
			var hero = all.get(i);
			if (hero.getLevel() <= avgLevel) {
				lowlist.add(hero.getConfigId());
			}
		}
		List<Integer> basepool = new ArrayList<>();
		HeroManager.instance().list().forEach(heroConfig -> {
			if(heroConfig.HeroType==1)
			{
				basepool.add(heroConfig.ID);
			}
		});
		if (highestLevel - lowestLevel >= GlobalConst.HeroRecruitDetectCondition) {
			// 找等级最低的神将
			int heroid=0;
			if (lowlist.isEmpty()) {
				heroid = Rnd.randomElement(basepool);
			}else {
				heroid = Rnd.randomElement(lowlist);
			}
			HeroConfig heroConfig = HeroManager.instance().get(heroid);
			drawHeroInPoolList.add(new DrawHeroInPool(heroConfig.Fragment, 1, 2, pos,noHighQualityRecruitCount,huiLiuCount>0));
			//是否还要随机

		} else {
			//找新神将
			List<Integer> radompool = new ArrayList<>(basepool);
			var owen = player.getHeroModule().getId_items().keySet();
			if (owen != null && !owen.isEmpty()) {
				radompool.removeAll(owen);
			}
			int heroid = 0;
			if (radompool.isEmpty()) {
				heroid = Rnd.randomElement(basepool);
			} else {
				heroid = Rnd.randomElement(radompool);
			}
			HeroConfig heroConfig = HeroManager.instance().get(heroid);
			drawHeroInPoolList.add(new DrawHeroInPool(heroConfig.Fragment, 1, 2, pos,noHighQualityRecruitCount,huiLiuCount>0));
		}

	}
	// 从权重3里挑一个
	public DrawHeroInPool radom31() {
		List<Integer> radomWeight = new ArrayList<>();
		List<DrawHeroInPool> value = new ArrayList<>();
		for (int j = 0; j < drawHeroInPoolList.size(); j++) {
			var drawHeroInPool = drawHeroInPoolList.get(j);
			if(drawHeroInPool.getIsDraw()==0){
				radomWeight.add(drawHeroInPool.getItemWeight());
				value.add(drawHeroInPool);
				log.info("当前可随机英雄  pos:{}  Id:{} radomWeight:{}",drawHeroInPool.getPosition(),drawHeroInPool.getItemId(),drawHeroInPool.getItemWeight());
			}
		}
        int index = Rnd.randomIndex(radomWeight);
		var drawHeroInPool = value.get(index).getItemId();
		ItemConfig itemConfig = ItemManager.instance().get(drawHeroInPool);

		log.info("当前可随机英雄  品质:{}",itemConfig.Quality);
		return value.get(index);
	}
	public int radom31test() {
		List<Integer> radomWeight = new ArrayList<>();
		List<DrawHeroInPool> value = new ArrayList<>();
		for (int j = 0; j < drawHeroInPoolList.size(); j++) {
			var drawHeroInPool = drawHeroInPoolList.get(j);
			if(drawHeroInPool.getIsDraw()==0){
				radomWeight.add(drawHeroInPool.getItemWeight());
				value.add(drawHeroInPool);
			//	log.info("当前可随机英雄  pos:{}  Id:{} radomWeight:{}",drawHeroInPool.getPosition(),drawHeroInPool.getItemId(),drawHeroInPool.getItemWeight());
			}
		}
		int index = Rnd.randomIndex(radomWeight);
		var drawHeroInPool = value.get(index);
		return drawHeroInPool.quality;
	}
    public int getNoHighQualityRecruitCount() {
        return noHighQualityRecruitCount;
    }

    public void setNoHighQualityRecruitCount(int noHighQualityRecruitCount) {
        this.noHighQualityRecruitCount = noHighQualityRecruitCount;
    }

    public int getHuiLiuCount() {
        return huiLiuCount;
    }

    public void setHuiLiuCount(int huiLiuCount) {
        this.huiLiuCount = huiLiuCount;
    }
}
