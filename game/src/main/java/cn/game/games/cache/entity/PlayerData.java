package cn.game.games.cache.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.base.DbEntity;

public class PlayerData implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * @mbg.generated
	 */
	private long uid;
	/**
	 * 渠道id
	 * @mbg.generated
	 */
	private String channelId;
	/**
	 * 设备唯一标识：MAC或者UUID或者IMEI，这里小游戏变为账号id ，即微信的openid
	 * @mbg.generated
	 */
	private String deviceId;
	/**
	 * 登录账号id，这里是微信的unionid
	 * @mbg.generated
	 */
	private String accountId;
	/**
	 * 是否是gm账号
	 * @mbg.generated
	 */
	private boolean isGm;
	/**
	 * 玩家所在服务器id，这个服务器是虚构出来的，和实际运行的服务器id没有关系
	 * @mbg.generated
	 */
	private String serverId;
	/**
	 * 性别 1男2女
	 * @mbg.generated
	 */
	private boolean gender;
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
	private int exp;
	/**
	 * 等级
	 * @mbg.generated
	 */
	private int level;
	/**
	 * 累积vip经验,只增不减
	 * @mbg.generated
	 */
	private int vipExpTotal;
	/**
	 * vip等级
	 * @mbg.generated
	 */
	private int vipLevel;
	/**
	 * 战力
	 * @mbg.generated
	 */
	private int fightPower;
	/**
	 * 头像id
	 * @mbg.generated
	 */
	private int head;
	/**
	 * 头像框id
	 * @mbg.generated
	 */
	private int headFrame;
	/**
	 * 形象
	 * @mbg.generated
	 */
	private int image;
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
	private long offlineTime;
	/**
	 * 跨月数据刷新日期
	 * @mbg.generated
	 */
	private int refreshMonth;
	/**
	 * 跨周数据刷新日期
	 * @mbg.generated
	 */
	private int refreshWeek;
	/**
	 * 跨天数据刷新日期 0点
	 * @mbg.generated
	 */
	private int refreshDay;
	/**
	 * 刷新每日5点的
	 * @mbg.generated
	 */
	private int refreshFiveDay;
	/**
	 * @mbg.generated
	 */
	private int title;
	/**
	 * 所在工会id
	 * @mbg.generated
	 */
	private long unionId;
	/**
	 * 所在工会名
	 * @mbg.generated
	 */
	private String unionName;
	/**
	 * 战斗力
	 * @mbg.generated
	 */
	private int combat;
	/**
	 * 游戏天数
	 * @mbg.generated
	 */
	private int day;
	/**
	 * 累计游戏时长（秒）
	 * @mbg.generated
	 */
	private int gameTime;
	/**
	 * 聊天框id
	 * @mbg.generated
	 */
	private int chatBox;
	/**
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
	public long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public long getUid() {
		return uid;
	}

	/**
	 * @mbg.generated
	 */
	public void setUid(long uid) {
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
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @mbg.generated
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @mbg.generated
	 */
	public String getAccountId() {
		return accountId;
	}

	/**
	 * @mbg.generated
	 */
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	/**
	 * @mbg.generated
	 */
	public boolean getIsGm() {
		return isGm;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsGm(boolean isGm) {
		this.isGm = isGm;
	}

	/**
	 * @mbg.generated
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * @mbg.generated
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * @mbg.generated
	 */
	public boolean getGender() {
		return gender;
	}

	/**
	 * @mbg.generated
	 */
	public void setGender(boolean gender) {
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
	public int getExp() {
		return exp;
	}

	/**
	 * @mbg.generated
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}

	/**
	 * @mbg.generated
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @mbg.generated
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @mbg.generated
	 */
	public int getVipExpTotal() {
		return vipExpTotal;
	}

	/**
	 * @mbg.generated
	 */
	public void setVipExpTotal(int vipExpTotal) {
		this.vipExpTotal = vipExpTotal;
	}

	/**
	 * @mbg.generated
	 */
	public int getVipLevel() {
		return vipLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	/**
	 * @mbg.generated
	 */
	public int getFightPower() {
		return fightPower;
	}

	/**
	 * @mbg.generated
	 */
	public void setFightPower(int fightPower) {
		this.fightPower = fightPower;
	}

	/**
	 * @mbg.generated
	 */
	public int getHead() {
		return head;
	}

	/**
	 * @mbg.generated
	 */
	public void setHead(int head) {
		this.head = head;
	}

	/**
	 * @mbg.generated
	 */
	public int getHeadFrame() {
		return headFrame;
	}

	/**
	 * @mbg.generated
	 */
	public void setHeadFrame(int headFrame) {
		this.headFrame = headFrame;
	}

	/**
	 * @mbg.generated
	 */
	public int getImage() {
		return image;
	}

	/**
	 * @mbg.generated
	 */
	public void setImage(int image) {
		this.image = image;
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
	public long getOfflineTime() {
		return offlineTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setOfflineTime(long offlineTime) {
		this.offlineTime = offlineTime;
	}

	/**
	 * @mbg.generated
	 */
	public int getRefreshMonth() {
		return refreshMonth;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshMonth(int refreshMonth) {
		this.refreshMonth = refreshMonth;
	}

	/**
	 * @mbg.generated
	 */
	public int getRefreshWeek() {
		return refreshWeek;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshWeek(int refreshWeek) {
		this.refreshWeek = refreshWeek;
	}

	/**
	 * @mbg.generated
	 */
	public int getRefreshDay() {
		return refreshDay;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshDay(int refreshDay) {
		this.refreshDay = refreshDay;
	}

	/**
	 * @mbg.generated
	 */
	public int getRefreshFiveDay() {
		return refreshFiveDay;
	}

	/**
	 * @mbg.generated
	 */
	public void setRefreshFiveDay(int refreshFiveDay) {
		this.refreshFiveDay = refreshFiveDay;
	}

	/**
	 * @mbg.generated
	 */
	public int getTitle() {
		return title;
	}

	/**
	 * @mbg.generated
	 */
	public void setTitle(int title) {
		this.title = title;
	}

	/**
	 * @mbg.generated
	 */
	public long getUnionId() {
		return unionId;
	}

	/**
	 * @mbg.generated
	 */
	public void setUnionId(long unionId) {
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
	public int getCombat() {
		return combat;
	}

	/**
	 * @mbg.generated
	 */
	public void setCombat(int combat) {
		this.combat = combat;
	}

	/**
	 * @mbg.generated
	 */
	public int getDay() {
		return day;
	}

	/**
	 * @mbg.generated
	 */
	public void setDay(int day) {
		this.day = day;
	}

	/**
	 * @mbg.generated
	 */
	public int getGameTime() {
		return gameTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
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

	/** 是否是新注册的玩家 */
	@JsonIgnore
	private transient boolean isNew;
	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

    public int getChatBox() {
        return chatBox;
    }

    public void setChatBox(int chatBox) {
        this.chatBox = chatBox;
    }
}