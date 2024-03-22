package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;

public class UserTag implements Serializable {

	/**
	 * @mbg.generated
	 */
	private Long uid;
	/**
	 * @mbg.generated
	 */
	private Integer tagId;
	/**
	 * 创建时间
	 * @mbg.generated
	 */
	private Date createTime;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

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
	public Integer getTagId() {
		return tagId;
	}

	/**
	 * @mbg.generated
	 */
	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	/**
	 * @mbg.generated
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}