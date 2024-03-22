package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Notice implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * @mbg.generated
	 */
	private String title;
	/**
	 * @mbg.generated
	 */
	private String startDate;
	/**
	 * @mbg.generated
	 */
	private String startTime;
	/**
	 * @mbg.generated
	 */
	private String endDate;
	/**
	 * @mbg.generated
	 */
	private String endTime;
	/**
	 * @mbg.generated
	 */
	private String content;
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
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @mbg.generated
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @mbg.generated
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @mbg.generated
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @mbg.generated
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @mbg.generated
	 */
	public void setContent(String content) {
		this.content = content;
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