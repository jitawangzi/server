package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Group implements Serializable, DbEntity {

	/**
	 * 群组id
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 群组名字
	 * @mbg.generated
	 */
	private String name;
	/**
	 * 群主id
	 * @mbg.generated
	 */
	private Long managerId;
	/**
	 * 群组头像
	 * @mbg.generated
	 */
	private Integer headIcon;
	/**
	 * 群组公告
	 * @mbg.generated
	 */
	private String notice;
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
	public Long getManagerId() {
		return managerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getHeadIcon() {
		return headIcon;
	}

	/**
	 * @mbg.generated
	 */
	public void setHeadIcon(Integer headIcon) {
		this.headIcon = headIcon;
	}

	/**
	 * @mbg.generated
	 */
	public String getNotice() {
		return notice;
	}

	/**
	 * @mbg.generated
	 */
	public void setNotice(String notice) {
		this.notice = notice;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.GroupMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public int getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.onlineCount = onlineCount;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/** 提供给显示，或者网络传输使用*/ 
	private int memberCount ; 
	private int onlineCount ; 
	private String serverId ; 
	
	
}