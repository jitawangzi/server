package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;
import java.util.Date;

public class Notice implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * 创建时间戳
	 * @mbg.generated
	 */
	private Date createTime;
	/**
	 * 排序
	 * @mbg.generated
	 */
	private Integer ordernum;
	/**
	 * 标签页
	 * @mbg.generated
	 */
	private String tab;
	/**
	 * 标题
	 * @mbg.generated
	 */
	private String title;
	/**
	 * 展示开始时间
	 * @mbg.generated
	 */
	private Date showStartTimer;
	/**
	 * 展示结束时间
	 * @mbg.generated
	 */
	private Date showEndTimer;
	/**
	 * 正文
	 * @mbg.generated
	 */
	private String text;
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
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getOrdernum() {
		return ordernum;
	}

	/**
	 * @mbg.generated
	 */
	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

	/**
	 * @mbg.generated
	 */
	public String getTab() {
		return tab;
	}

	/**
	 * @mbg.generated
	 */
	public void setTab(String tab) {
		this.tab = tab;
	}

	/**
	 * @mbg.generated
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @mbg.generated
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @mbg.generated
	 */
	public Date getShowStartTimer() {
		return showStartTimer;
	}

	/**
	 * @mbg.generated
	 */
	public void setShowStartTimer(Date showStartTimer) {
		this.showStartTimer = showStartTimer;
	}

	/**
	 * @mbg.generated
	 */
	public Date getShowEndTimer() {
		return showEndTimer;
	}

	/**
	 * @mbg.generated
	 */
	public void setShowEndTimer(Date showEndTimer) {
		this.showEndTimer = showEndTimer;
	}

	/**
	 * @mbg.generated
	 */
	public String getText() {
		return text;
	}

	/**
	 * @mbg.generated
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.NoticeMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}