package cn.game.games.cache.entity;

import com.alibaba.fastjson.JSON;
import java.io.Serializable;
import java.util.BitSet;
import org.apache.commons.lang3.StringUtils;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class BattlePass implements Serializable, DbEntity {
    /**
	 * @mbg.generated
	 */
	private Long playerId;

	/**
	 * 周期id
	 * @mbg.generated
	 */
	private Integer battlepassId;

	/**
	 * 是否购买 0：无 1：购买
	 * @mbg.generated
	 */
	private Byte recharge;

	/**
	 * 等级
	 * @mbg.generated
	 */
	private Integer level;

	/**
	 * 经验
	 * @mbg.generated
	 */
	private Integer exp;

	/**
	 * 开启天数
	 * @mbg.generated
	 */
	private Byte openday;

	/**
	 * 领取的金色奖励 
	 * @mbg.generated
	 */
	private String goldRewards;

	/**
	 * 领取的银色奖励
	 * @mbg.generated
	 */
	private String silverRewards;

	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}


	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}


	/**
	 * @mbg.generated
	 */
	public Integer getBattlepassId() {
		return battlepassId;
	}


	/**
	 * @mbg.generated
	 */
	public void setBattlepassId(Integer battlepassId) {
		this.battlepassId = battlepassId;
	}


	/**
	 * @mbg.generated
	 */
	public Byte getRecharge() {
		return recharge;
	}


	/**
	 * @mbg.generated
	 */
	public void setRecharge(Byte recharge) {
		this.recharge = recharge;
	}


	/**
	 * @mbg.generated
	 */
	public Integer getLevel() {
		return level;
	}


	/**
	 * @mbg.generated
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}


	/**
	 * @mbg.generated
	 */
	public Integer getExp() {
		return exp;
	}


	/**
	 * @mbg.generated
	 */
	public void setExp(Integer exp) {
		this.exp = exp;
	}


	/**
	 * @mbg.generated
	 */
	public Byte getOpenday() {
		return openday;
	}


	/**
	 * @mbg.generated
	 */
	public void setOpenday(Byte openday) {
		this.openday = openday;
	}


	/**
	 * @mbg.generated
	 */
	public String getGoldRewards() {
		return goldRewards;
	}


	/**
	 * @mbg.generated
	 */
	public void setGoldRewards(String goldRewards) {
		this.goldRewards = goldRewards;
	}


	/**
	 * @mbg.generated
	 */
	public String getSilverRewards() {
		return silverRewards;
	}


	/**
	 * @mbg.generated
	 */
	public void setSilverRewards(String silverRewards) {
		this.silverRewards = silverRewards;
	}


	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.BattlePassMapper.class;
	}


	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}


	private transient BitSet goldRewardIndex = new BitSet();

	private transient BitSet silverRewardIndex = new BitSet();

	public BitSet getGoldRewardIndex() {
		if (this.goldRewardIndex.isEmpty() && !StringUtils.isEmpty(this.goldRewards)) {
			this.goldRewardIndex = BitSet.valueOf(JSON.parseObject(this.goldRewards, long[].class));
		}
		return this.goldRewardIndex;
	}


	public BitSet getSilverRewardIndex() {
		if (this.silverRewardIndex.isEmpty() && !StringUtils.isEmpty(this.silverRewards)) {
			//List<Long> longs1 = JSONArray.parseArray(this.silverRewards, Long.class);
			//long[] longs = longs1.stream().mapToLong(t -> t.longValue()).toArray();
			this.silverRewardIndex = BitSet.valueOf(JSON.parseObject(this.silverRewards, long[].class));
		}
		return this.silverRewardIndex;
	}


}