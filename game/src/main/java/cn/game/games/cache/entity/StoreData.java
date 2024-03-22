package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

public class StoreData implements Serializable, DbEntity {

	/**
	 * 玩家id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 主城道具商店刷新时间
	 * @mbg.generated
	 */
	private Integer cityItemRefreshTime;
	/**
	 * 主城装备商店刷新时间
	 * @mbg.generated
	 */
	private Integer cityEquipRefreshTime;
	/**
	 * 主城道具商店数据
	 * @mbg.generated
	 */
	private byte[] cityItem;
	/**
	 * 主城装备商店数据
	 * @mbg.generated
	 */
	private byte[] cityEquip;
	/**
	 * 探索局间道具商店数据
	 * @mbg.generated
	 */
	private byte[] exploreRoomItem;
	/**
	 * 探索局间装备商店数据
	 * @mbg.generated
	 */
	private byte[] exploreRoomEquip;
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
	public Integer getCityItemRefreshTime() {
		return cityItemRefreshTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCityItemRefreshTime(Integer cityItemRefreshTime) {
		this.cityItemRefreshTime = cityItemRefreshTime;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getCityEquipRefreshTime() {
		return cityEquipRefreshTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCityEquipRefreshTime(Integer cityEquipRefreshTime) {
		this.cityEquipRefreshTime = cityEquipRefreshTime;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getCityItem() {
		return cityItem;
	}

	/**
	 * @mbg.generated
	 */
	public void setCityItem(byte[] cityItem) {
		this.cityItem = cityItem;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getCityEquip() {
		return cityEquip;
	}

	/**
	 * @mbg.generated
	 */
	public void setCityEquip(byte[] cityEquip) {
		this.cityEquip = cityEquip;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getExploreRoomItem() {
		return exploreRoomItem;
	}

	/**
	 * @mbg.generated
	 */
	public void setExploreRoomItem(byte[] exploreRoomItem) {
		this.exploreRoomItem = exploreRoomItem;
	}

	/**
	 * @mbg.generated
	 */
	public byte[] getExploreRoomEquip() {
		return exploreRoomEquip;
	}

	/**
	 * @mbg.generated
	 */
	public void setExploreRoomEquip(byte[] exploreRoomEquip) {
		this.exploreRoomEquip = exploreRoomEquip;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.StoreDataMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return playerId;
	}
}