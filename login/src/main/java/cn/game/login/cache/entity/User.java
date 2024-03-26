package cn.game.login.cache.entity;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

	/**
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * @mbg.generated
	 */
	private String username;
	/**
	 * @mbg.generated
	 */
	private String pass;
	/**
	 * @mbg.generated
	 */
	private Boolean isGm;
	/**
	 * 1自有用户 2渠道用户
	 * @mbg.generated
	 */
	private Byte userType;
	/**
	 * 渠道编号
	 * @mbg.generated
	 */
	private String channelCode;
	/**
	 * 渠道名称
	 * @mbg.generated
	 */
	private String channelLabel;
	/**
	 * 渠道用户uid
	 * @mbg.generated
	 */
	private String thirdUid;
	/**
	 * 微信小游戏里面用户登陆后下发的
	 * @mbg.generated
	 */
	private String sessionKey;
	/**
	 * 子渠道
	 * @mbg.generated
	 */
	private String subChannelCode;
	/**
	 * 创建日期
	 * @mbg.generated
	 */
	private String createDate;
	/**
	 * 创建时间
	 * @mbg.generated
	 */
	private String createTime;
	/**
	 * 最后登陆日期
	 * @mbg.generated
	 */
	private String loginDate;
	/**
	 * 最后登陆时间
	 * @mbg.generated
	 */
	private String loginTime;
	/**
	 * @mbg.generated
	 */
	private String deviceUid;
	/**
	 * @mbg.generated
	 */
	private String servers;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @mbg.generated
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @mbg.generated
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @mbg.generated
	 */
	public void setPass(String pass) {
		this.pass = pass;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsGm() {
		return isGm;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsGm(Boolean isGm) {
		this.isGm = isGm;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getUserType() {
		return userType;
	}

	/**
	 * @mbg.generated
	 */
	public void setUserType(Byte userType) {
		this.userType = userType;
	}

	/**
	 * @mbg.generated
	 */
	public String getChannelCode() {
		return channelCode;
	}

	/**
	 * @mbg.generated
	 */
	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	/**
	 * @mbg.generated
	 */
	public String getChannelLabel() {
		return channelLabel;
	}

	/**
	 * @mbg.generated
	 */
	public void setChannelLabel(String channelLabel) {
		this.channelLabel = channelLabel;
	}

	/**
	 * @mbg.generated
	 */
	public String getThirdUid() {
		return thirdUid;
	}

	/**
	 * @mbg.generated
	 */
	public void setThirdUid(String thirdUid) {
		this.thirdUid = thirdUid;
	}

	/**
	 * @mbg.generated
	 */
	public String getSessionKey() {
		return sessionKey;
	}

	/**
	 * @mbg.generated
	 */
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	/**
	 * @mbg.generated
	 */
	public String getSubChannelCode() {
		return subChannelCode;
	}

	/**
	 * @mbg.generated
	 */
	public void setSubChannelCode(String subChannelCode) {
		this.subChannelCode = subChannelCode;
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
	public String getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
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
	public String getLoginTime() {
		return loginTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getDeviceUid() {
		return deviceUid;
	}

	/**
	 * @mbg.generated
	 */
	public void setDeviceUid(String deviceUid) {
		this.deviceUid = deviceUid;
	}

	/**
	 * @mbg.generated
	 */
	public String getServers() {
		return servers;
	}

	/**
	 * @mbg.generated
	 */
	public void setServers(String servers) {
		this.servers = servers;
	}

}