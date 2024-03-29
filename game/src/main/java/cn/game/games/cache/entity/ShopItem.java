package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
import cn.game.protocol.protobuf.ShopMsg.ShopItemProto;

/**
 * 商品表

 * @mbg.generated
 */
public class ShopItem implements Serializable, DbEntity {

	/**
	 * 唯一id
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 商品组id
	 * @mbg.generated
	 */
	private Integer groupId;
	/**
	 * 商品id
	 * @mbg.generated
	 */
	private Integer itemId;
	/**
	 * 商品购买次数
	 * @mbg.generated
	 */
	private Integer itemBuyTimes;
	/**
	 * 商品折扣，2=随机折扣类型的商品会随机这个折扣
	 * @mbg.generated
	 */
	private Integer itemDiscount;
	/**
	 * 那天创建的商品，重置时用到
	 * @mbg.generated
	 */
	private Integer createDay;
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
	public Integer getGroupId() {
		return groupId;
	}

	/**
	 * @mbg.generated
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getItemId() {
		return itemId;
	}

	/**
	 * @mbg.generated
	 */
	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getItemBuyTimes() {
		return itemBuyTimes;
	}

	/**
	 * @mbg.generated
	 */
	public void setItemBuyTimes(Integer itemBuyTimes) {
		this.itemBuyTimes = itemBuyTimes;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getItemDiscount() {
		return itemDiscount;
	}

	/**
	 * @mbg.generated
	 */
	public void setItemDiscount(Integer itemDiscount) {
		this.itemDiscount = itemDiscount;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCreateDay() {
		return createDay;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateDay(Integer createDay) {
		this.createDay = createDay;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ShopItemMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	public ShopItemProto toProto() {
		ShopItemProto.Builder builder = ShopItemProto.newBuilder();
		builder.setBuyTimes(itemBuyTimes);
		builder.setConfigId(itemId);
		builder.setDiscount(itemDiscount);
		builder.setId(id+"");
		return builder.build();
	}
}