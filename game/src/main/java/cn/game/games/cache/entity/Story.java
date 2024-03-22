package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Story implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 剧情id
	 * @mbg.generated
	 */
	private Integer story;
	/**
	 * 剧情开启条件数量
	 * @mbg.generated
	 */
	private Integer startConditionCount;
	/**
	 * 剧情是否已完成
	 * @mbg.generated
	 */
	private Boolean finish;
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
	public Integer getStory() {
		return story;
	}

	/**
	 * @mbg.generated
	 */
	public void setStory(Integer story) {
		this.story = story;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getStartConditionCount() {
		return startConditionCount;
	}

	/**
	 * @mbg.generated
	 */
	public void setStartConditionCount(Integer startConditionCount) {
		this.startConditionCount = startConditionCount;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getFinish() {
		return finish;
	}

	/**
	 * @mbg.generated
	 */
	public void setFinish(Boolean finish) {
		this.finish = finish;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.StoryMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, story };
	}

	public static Story valueOf(long playerId, int story) {
		Story s = new Story();
		s.setPlayerId(playerId);
		s.setStory(story);
		return s;
	}
}