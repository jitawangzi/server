package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class HomePage implements Serializable, DbEntity {

	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 看板娘角色id
	 * @mbg.generated
	 */
	private Integer posterGirlId;
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
	public Integer getPosterGirlId() {
		return posterGirlId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPosterGirlId(Integer posterGirlId) {
		this.posterGirlId = posterGirlId;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.HomePageMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}

	/**
	 * 
	 * @param playerId
	 * @param posterGirlId
	 * @return
	 */
	public static HomePage valueOf(Long playerId,int posterGirlId) {
		HomePage homePage=new HomePage();
		homePage.setPlayerId(playerId);
		homePage.setPosterGirlId(posterGirlId);
		return homePage;
	}
}