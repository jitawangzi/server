package cn.game.login.cache.entity;

import java.io.Serializable;

public class User implements Serializable {
	/**
	 */
	private Long id;

	/**
	 */
	private String username;

	/**
	 */
	private String pass;

	/**
	 */
	private Integer isGm;

	/**
	 * 1棱镜用户 2渠道用户 3自有用户 4易接
	 */
	private Byte userType;

	/**
	 * 渠道编号
	 */
	private String channelCode;

	/**
	 * 渠道名称
	 */
	private String channelLabel;

	/**
	 * 棱镜、渠道用户uid
	 */
	private String thirdUid;

	/**
	 * 子渠道
	 */
	private String subChannelCode;

	/**
	 * 创建日期
	 */
	private String createDate;

	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 最后登陆日期
	 */
	private String loginDate;

	/**
	 * 最后登陆时间
	 */
	private String loginTime;

	/**
	 */
	private String deviceUid;

	/**
	 */
	private String servers;
	private String sessionKey;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public Integer getIsGm() {
		return isGm;
	}

	public void setIsGm(Integer isGm) {
		this.isGm = isGm;
	}

	public Byte getUserType() {
		return userType;
	}

	public void setUserType(Byte userType) {
		this.userType = userType;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public String getChannelLabel() {
		return channelLabel;
	}

	public void setChannelLabel(String channelLabel) {
		this.channelLabel = channelLabel;
	}

	public String getThirdUid() {
		return thirdUid;
	}

	public void setThirdUid(String thirdUid) {
		this.thirdUid = thirdUid;
	}

	public String getSubChannelCode() {
		return subChannelCode;
	}

	public void setSubChannelCode(String subChannelCode) {
		this.subChannelCode = subChannelCode;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(String loginDate) {
		this.loginDate = loginDate;
	}

	public String getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(String loginTime) {
		this.loginTime = loginTime;
	}

	public String getDeviceUid() {
		return deviceUid;
	}

	public void setDeviceUid(String deviceUid) {
		this.deviceUid = deviceUid;
	}

	public String getServers() {
		return servers;
	}

	public void setServers(String servers) {
		this.servers = servers;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

}