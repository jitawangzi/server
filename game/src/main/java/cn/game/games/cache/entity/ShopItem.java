package cn.game.games.cache.entity;

import java.io.Serializable;

import cn.game.games.cache.base.DbEntity;
//import cn.game.protocol.protobuf.ShopMsg.ShopItemProto;

/**
 * 商品表

 * @mbg.generated
 */
public class ShopItem implements Serializable, DbEntity {

	private long id;
	/**
	 * 商品id
	 */
	private int itemId;
	/**
	 * 商品购买次数
	 * @mbg.generated
	 */
	private int itemBuyTimes;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	public ShopItem() {
	};

	public ShopItem(int itemId) {
		this.itemId = itemId;
	};

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemBuyTimes() {
		return itemBuyTimes;
	}

	public void setItemBuyTimes(int itemBuyTimes) {
		this.itemBuyTimes = itemBuyTimes;
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

//	public ShopItemProto toProto() {
//		ShopItemProto.Builder builder = ShopItemProto.newBuilder();
//		builder.setBuyTimes(itemBuyTimes);
//		builder.setItemId(itemId);
////		builder.setConfigId(itemId);
////		builder.setDiscount(itemDiscount);
////		builder.setId(id+"");
//		return builder.build();
//	}
}