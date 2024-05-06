package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.protobuf.BaseMsg.HeroInfo;

public class Hero extends ItemNoStack implements Serializable, DbEntity {

	/**
	 * 等级经验
	 * @mbg.generated
	 */
	private Integer exp;
	/**
	 * 突破等级
	 * @mbg.generated
	 */
	private Integer breakLevel;
	/**
	 * 获取时间
	 * @mbg.generated
	 */
	private Long getTime;
	/**
	 * 当前使用的皮肤id
	 * @mbg.generated
	 */
	private Integer skin;
	/**
	 * @mbg.generated
	 */
	private String skills;
	/**
	 * @mbg.generated
	 */
	private String skillsUsed;
	/**
	 * 血量
	 * @mbg.generated
	 */
	private Integer hp;
	/**
	 * 血量当前最大值
	 * @mbg.generated
	 */
	private Integer hpCurMax;
	/**
	 * san值
	 * @mbg.generated
	 */
	private Integer san;
	/**
	 * ep
	 * @mbg.generated
	 */
	private Integer ep;
	/**
	 * 状态：0正常 1重伤 2死亡
	 * @mbg.generated
	 */
	private Byte state;
	/**
	 * 技能点
	 * @mbg.generated
	 */
	private Integer skillPoint;
	/**
	 * 队列id
	 * @mbg.generated
	 */
	private Integer lineupId;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	private int quality;

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
	public Integer getBreakLevel() {
		return breakLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setBreakLevel(Integer breakLevel) {
		this.breakLevel = breakLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setStar(Integer star) {
		this.star = star;
	}

	/**
	 * @mbg.generated
	 */
	public Long getGetTime() {
		return getTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGetTime(Long getTime) {
		this.getTime = getTime;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSkin() {
		return skin;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkin(Integer skin) {
		this.skin = skin;
	}

	/**
	 * @mbg.generated
	 */
	public String getSkills() {
		return skills;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkills(String skills) {
		this.skills = skills;
	}

	/**
	 * @mbg.generated
	 */
	public String getSkillsUsed() {
		return skillsUsed;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkillsUsed(String skillsUsed) {
		this.skillsUsed = skillsUsed;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHp() {
		return hp;
	}

	/**
	 * @mbg.generated
	 */
	public void setHp(Integer hp) {
		this.hp = hp;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHpCurMax() {
		return hpCurMax;
	}

	/**
	 * @mbg.generated
	 */
	public void setHpCurMax(Integer hpCurMax) {
		this.hpCurMax = hpCurMax;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSan() {
		return san;
	}

	/**
	 * @mbg.generated
	 */
	public void setSan(Integer san) {
		this.san = san;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getEp() {
		return ep;
	}

	/**
	 * @mbg.generated
	 */
	public void setEp(Integer ep) {
		this.ep = ep;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getState() {
		return state;
	}

	/**
	 * @mbg.generated
	 */
	public void setState(Byte state) {
		this.state = state;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSkillPoint() {
		return skillPoint;
	}

	/**
	 * @mbg.generated
	 */
	public void setSkillPoint(Integer skillPoint) {
		this.skillPoint = skillPoint;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getLineupId() {
		return lineupId;
	}

	/**
	 * @mbg.generated
	 */
	public void setLineupId(Integer lineupId) {
		this.lineupId = lineupId;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.HeroMapper.class;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


	public HeroInfo toHeroInfo() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		builder.setUid(id + "");
		builder.setConfigId(configId);
		builder.setStar(star);
		builder.setLevel(level);
		builder.setIsBattle(player.getHeroModule().isInBattle(id));
		builder.setQuality(quality);
//		builder.setExp(this.exp); 
//		builder.setGetTime(getTime.intValue());

		return builder.build();
	}

	public HeroInfo toHeroLevelInfo() {
		HeroInfo.Builder builder = HeroInfo.newBuilder();
		builder.setUid(id + "");
		builder.setLevel(level);
		return builder.build();
	}
}