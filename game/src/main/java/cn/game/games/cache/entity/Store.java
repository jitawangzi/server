package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class Store implements Serializable, DbEntity {

	/**
	 * 用户id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 商品id
	 * @mbg.generated
	 */
	private Integer goodsId;
	/**
	 * 购买次数
	 * @mbg.generated
	 */
	private Integer count;
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
	public Integer getGoodsId() {
		return goodsId;
	}

	/**
	 * @mbg.generated
	 */
	public void setGoodsId(Integer goodsId) {
		this.goodsId = goodsId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @mbg.generated
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.StoreMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return new Object[] { playerId, goodsId };
	}

	public static Store valueOf(long playerId, int goodsId, int cnt) {
		Store s=new Store();
		s.setPlayerId(playerId);
		s.setGoodsId(goodsId);
		s.setCount(cnt);
		return s;
	}
}