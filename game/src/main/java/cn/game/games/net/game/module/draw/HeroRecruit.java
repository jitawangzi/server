package cn.game.games.net.game.module.draw;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.games.net.game.module.guarantee.Guarantee;
import cn.game.games.net.game.module.guarantee.GuaranteeModule;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.GuaranteeConfig;
import cn.game.protocol.generated.enume.GuaranteeTypeEnum;
import cn.game.protocol.generated.manager.GuaranteeManager;
import cn.game.protocol.protobuf.DrawMsg.DrawHeroInfo;
import cn.game.util.DateUtil;
import cn.game.util.Rnd;

public class HeroRecruit {
	/** 英雄碎片id*/
	private List<Integer> itemIdList = new ArrayList<Integer>();
	/** 英雄碎片id对应的数量 */
	private List<Integer> itemCountList = new ArrayList<Integer>();
	/** 已经招募过的英雄碎片位置，从0开始 */
	private List<Integer> recruitedPosList = new ArrayList<Integer>();
	/** 英雄刷新时间 */
	private int heroRefreshTime;
	/** 招募次数 */
	private int recruitCount;
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
		itemIdList.clear();
		itemCountList.clear();
		recruitedPosList.clear();

		int randomId = 1001001;

		GuaranteeModule guaranteeModule = player.getGuaranteeModule();
		Guarantee guarantee = guaranteeModule.get(GuaranteeTypeEnum.DrawRefresh);
		int guaranteeIndex = -1;
		int guaranteeRandomId = -1;
		if (guarantee.isInGuarantee()) {
			GuaranteeConfig guaranteeConfig = GuaranteeManager.instance().get(guarantee.getId());
			guarantee.reset();
			guaranteeIndex = Rnd.nextInt(3);
			guaranteeRandomId = guaranteeConfig.effectiveParam;
		}
		
		for (int i = 0; i < 3; i++) {
			int tmpRandomId = randomId;
			if (guaranteeIndex > -1 && guaranteeIndex == i) {
				tmpRandomId = guaranteeRandomId;
			}
			List<Goods> randomReward = PlayerHelper.randomReward(tmpRandomId);
			if (randomReward.size() != 1) {
				throw new IllegalArgumentException("RandomGiven: " + tmpRandomId + " 刷新招募英雄配置错误，生成的数量不对: " + randomReward.size());
			}
			Goods goods = randomReward.get(0);
			itemIdList.add(goods.getId());
			itemCountList.add(goods.getCount());
		}
		heroRefreshTime = DateUtil.currentTimeSeconds();

	}
	public void initHeros() {
		if (!itemIdList.isEmpty()) {
			return ; 
		}
		int[][] gachaFirstTime = GlobalConst.GachaFirstTime;
		for (int i = 0; i < gachaFirstTime.length; i++) {
			int itemId = gachaFirstTime[i][0];
			int itemCount = gachaFirstTime[i][1];
			itemIdList.add(itemId);
			itemCountList.add(itemCount);
		}
	}

	public DrawHeroInfo buildDrawHeroInfo() {
		DrawHeroInfo.Builder resp = DrawHeroInfo.newBuilder();
		resp.addAllItemId(itemIdList);
		resp.addAllItemCount(itemCountList);
		resp.addAllRecruitedPos(recruitedPosList);

		resp.setCanMultiple(recruitCount >= GlobalConst.GachaMultipleCount);
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

	public List<Integer> getItemIdList() {
		return itemIdList;
	}

	public List<Integer> getItemCountList() {
		return itemCountList;
	}

	public void setItemCountList(List<Integer> itemCountList) {
		this.itemCountList = itemCountList;
	}


	public List<Integer> getRecruitedPosList() {
		return recruitedPosList;
	}

	public void setRecruitedPosList(List<Integer> recruitedPosList) {
		this.recruitedPosList = recruitedPosList;
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

}
