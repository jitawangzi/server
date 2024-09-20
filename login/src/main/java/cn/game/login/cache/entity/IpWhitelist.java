package cn.game.login.cache.entity;

import java.io.Serializable;
import java.util.Date;

public class IpWhitelist implements Serializable {

	/**
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * 白名单IP
	 * @mbg.generated
	 */
	private String ip;
	/**
	 * @mbg.generated
	 */
	private Date createTimer;
	/**
	 * 过期时间
	 * @mbg.generated
	 */
	private Date failTimer;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @mbg.generated
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @mbg.generated
	 */
	public Date getCreateTimer() {
		return createTimer;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTimer(Date createTimer) {
		this.createTimer = createTimer;
	}

	/**
	 * @mbg.generated
	 */
	public Date getFailTimer() {
		return failTimer;
	}

	/**
	 * @mbg.generated
	 */
	public void setFailTimer(Date failTimer) {
		this.failTimer = failTimer;
	}

}