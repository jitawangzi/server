package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;

public class PlayerData implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * @mbg.generated
	 */
	private Long uid;
	/**
	 * 渠道id
	 * @mbg.generated
	 */
	private String channelId;
	/**
	 * 性别 1男2女
	 * @mbg.generated
	 */
	private Boolean gender;
	/**
	 * @mbg.generated
	 */
	private String name;
	/**
	 * 账号注册时，根据ip查询出来的地区
	 * @mbg.generated
	 */
	private String region;
	/**
	 * 经验
	 * @mbg.generated
	 */
	private Integer exp;
	/**
	 * 等级
	 * @mbg.generated
	 */
	private Integer level;
	/**
	 * 累积vip经验,只增不减
	 * @mbg.generated
	 */
	private Integer vipExpTotal;
	/**
	 * vip等级
	 * @mbg.generated
	 */
	private Integer vipLevel;
	/**
	 * 累积充值元宝
	 * @mbg.generated
	 */
	private Integer goldTotal;
	/**
	 * 累积消费元宝
	 * @mbg.generated
	 */
	private Integer costGoldTotal;
	/**
	 * 战力
	 * @mbg.generated
	 */
	private Integer fightPower;
	/**
	 * 头像id
	 * @mbg.generated
	 */
	private Integer head;
	/**
	 * 头像框id
	 * @mbg.generated
	 */
	private Integer headFrame;
	/**
	 * 创建日期
	 * @mbg.generated
	 */
	private String createDate;
	/**
	 * 最后登陆日期
	 * @mbg.generated
	 */
	private String loginDate;
	/**
	 * @mbg.generated
	 */
	private Long offlineTime;
	/**
	 * 跨月数据刷新日期
	 * @mbg.generated
	 */
	private Integer refreshMonth;
	/**
	 * 跨周数据刷新日期
	 * @mbg.generated
	 */
	private Integer refreshWeek;
	/**
	 * 跨天数据刷新日期 0点
	 * @mbg.generated
	 */
	private Integer refreshDay;
	/**
	 * 刷新每日5点的
	 * @mbg.generated
	 */
	private Integer refreshFiveDay;
	/**
	 * @mbg.generated
	 */
	private Integer title;
	/**
	 * 所在工会id
	 * @mbg.generated
	 */
	private Long unionId;
	/**
	 * 所在工会名
	 * @mbg.generated
	 */
	private String unionName;
	/**
	 * 战斗力
	 * @mbg.generated
	 */
	private Integer combat;
	/**
	 * 游戏天数
	 * @mbg.generated
	 */
	private Integer day;
	/**
	 * 累计游戏时长（秒）
	 * @mbg.generated
	 */
	private Integer gameTime;
	/**
	 * 玩家的热点数据，经常修改的,一般保存复杂的数据结构
	 * @mbg.generated
	 */
	private cn.game.games.net.game.module.player.PlayerHotData hotData = new cn.game.games.net.game.module.player.PlayerHotData();
	/**
	 * 玩家的热点数据，经常修改的,一般保存复杂的数据结构
	 * @mbg.generated
	 */
	private String playerHotData;
	/**
	 * 所有模块的数据
	 * @mbg.generated
	 */
	private String modules;
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
	public Long getUid() {
		return uid;
	}

	/**
	 * @mbg.generated
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}

	/**
	 * @mbg.generated
	 */
	public String getChannelId() {
		return channelId;
	}

	/**
	 * @mbg.generated
	 */
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getGender() {
		return gender;
	}

	/**
	 * @mbg.generated
	 */
	public void setGender(Boolean gender) {
		this.gender = gender;
	}

	/**
	 * @mbg.generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * @mbg.generated
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @mbg.generated
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @mbg.generated
	 */
	public void setRegion(String region) {
		this.region = region;
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
	public Integer getVipExpTotal() {
		return vipExpTotal;
	}

	/**
	 * @mbg.generated
	 */
	public void setVipExpTotal(Integer vipExpTotal) {
		this.vipExpTotal = vipExpTotal;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getVipLevel() {
		return vipLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setVipLevel(Integer vipLevel) {
		this.vipLevel = vipLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getGoldTotal() {
		return goldTotal;
	}

	/**
	 * @mbg.generated
	 */
	public void setGoldTotal(Integer goldTotal) {
		this.goldTotal = goldTotal;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCostGoldTotal() {
		return costGoldTotal;
	}

	/**
	 * @mbg.generated
	 */
	public void setCostGoldTotal(Integer costGoldTotal) {
		this.costGoldTotal = costGoldTotal;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getFightPower() {
		return fightPower;
	}

	/**
	 * @mbg.generated
	 */
	public void setFightPower(Integer fightPower) {
		this.fightPower = fightPower;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHead() {
		return head;
	}

	/**
	 * @mbg.generated
	 */
	public void setHead(Integer head) {
		this.head = head;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHeadFrame() {
		return headFrame;
	}

	/**
	 * @mbg.generated
	 */
	public void setHeadFrame(Integer headFrame) {
		this.headFrame = headFrame;
	}

	/**
	 * @mbg.generated
	 */
	public String getCreateDate() {
		return createDate;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	/**
	 * @mbg.generated
	 */
	public String getLoginDate() {
		return loginDate;
	}

	/**
	 * @mbg.generated
	 */
	public void setLoginDate(String loginDate) {
		this.loginDate = loginDate;
	}

	/**
	 * @mbg.generated
	 */
	public Long getOfflineTime() {
		return offlineTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setOfflineTime(Long offlineTime) {
		this.offlineTime = offlineTime;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRefreshMonth() {
		return refreshMonth;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshMonth(Integer refreshMonth) {
		this.refreshMonth = refreshMonth;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRefreshWeek() {
		return refreshWeek;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshWeek(Integer refreshWeek) {
		this.refreshWeek = refreshWeek;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRefreshDay() {
		return refreshDay;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshDay(Integer refreshDay) {
		this.refreshDay = refreshDay;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRefreshFiveDay() {
		return refreshFiveDay;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshFiveDay(Integer refreshFiveDay) {
		this.refreshFiveDay = refreshFiveDay;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getTitle() {
		return title;
	}

	/**
	 * @mbg.generated
	 */
	public void setTitle(Integer title) {
		this.title = title;
	}

	/**
	 * @mbg.generated
	 */
	public Long getUnionId() {
		return unionId;
	}

	/**
	 * @mbg.generated
	 */
	public void setUnionId(Long unionId) {
		this.unionId = unionId;
	}

	/**
	 * @mbg.generated
	 */
	public String getUnionName() {
		return unionName;
	}

	/**
	 * @mbg.generated
	 */
	public void setUnionName(String unionName) {
		this.unionName = unionName;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCombat() {
		return combat;
	}

	/**
	 * @mbg.generated
	 */
	public void setCombat(Integer combat) {
		this.combat = combat;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getDay() {
		return day;
	}

	/**
	 * @mbg.generated
	 */
	public void setDay(Integer day) {
		this.day = day;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getGameTime() {
		return gameTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGameTime(Integer gameTime) {
		this.gameTime = gameTime;
	}

	/**
	 * @mbg.generated
	 */
	public cn.game.games.net.game.module.player.PlayerHotData getHotData() {
		return hotData;
	}

	/**
	 * @mbg.generated
	 */
	public void setHotData(cn.game.games.net.game.module.player.PlayerHotData hotData) {
		this.hotData = hotData;
	}

	/**
	 * @mbg.generated
	 */
	public String getPlayerHotData() {
		if (playerHotData == null || playerHotData.isEmpty()) {
			beforeSave();
		}
		return playerHotData;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerHotData(String playerHotData) {
		this.hotData = com.alibaba.fastjson.JSON.parseObject(playerHotData,
				new com.alibaba.fastjson.TypeReference<cn.game.games.net.game.module.player.PlayerHotData>() {
				});
		if (this.hotData == null) {
			this.hotData = new cn.game.games.net.game.module.player.PlayerHotData();
		}
	}

	/**
	 * @mbg.generated
	 */
	public String getModules() {
		return modules;
	}

	/**
	 * @mbg.generated
	 */
	public void setModules(String modules) {
		this.modules = modules;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public void beforeSave() {
		this.playerHotData = com.alibaba.fastjson.JSON.toJSONString(this.hotData, com.alibaba.fastjson.serializer.SerializerFeature.WriteNonStringKeyAsString);
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.PlayerDataMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}
}