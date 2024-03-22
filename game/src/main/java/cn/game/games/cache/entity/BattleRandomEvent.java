package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class BattleRandomEvent implements Serializable, DbEntity {

	/**
	 * 唯一id
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 随机事件配置表id
	 * @mbg.generated
	 */
	private Integer randomId;
	/**
	 * 哪个章节产生的事件
	 * @mbg.generated
	 */
	private Integer chapterId;
	/**
	 * 随机事件所在地图位置，编号，从0开始
	 * @mbg.generated
	 */
	private Byte pos;
	/**
	 * 创建时间
	 * @mbg.generated
	 */
	private Long createTime;
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
	public Integer getRandomId() {
		return randomId;
	}

	/**
	 * @mbg.generated
	 */
	public void setRandomId(Integer randomId) {
		this.randomId = randomId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getChapterId() {
		return chapterId;
	}

	/**
	 * @mbg.generated
	 */
	public void setChapterId(Integer chapterId) {
		this.chapterId = chapterId;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getPos() {
		return pos;
	}

	/**
	 * @mbg.generated
	 */
	public void setPos(Byte pos) {
		this.pos = pos;
	}

	/**
	 * @mbg.generated
	 */
	public Long getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.BattleRandomEventMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}