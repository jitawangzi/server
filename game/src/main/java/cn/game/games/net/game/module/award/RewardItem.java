package cn.game.games.net.game.module.award;

import java.io.Serializable;

import cn.game.games.cache.entity.CoreUnit;
import cn.game.games.cache.entity.Equip;
import cn.game.games.cache.entity.Role;


public class RewardItem implements Serializable {

	/**  */
	private static final long serialVersionUID = 6490893115966751038L;
	private int id;
	private int count;
	private Role role;
	private int skin;
	private CoreUnit coreUnit;
	private Equip equip;
	private int strategyCard;

	public static RewardItem valueOf(int id, int count) {
		RewardItem item = new RewardItem();
		item.setId(id);
		item.setCount(count);
		return item;
	}
	public static RewardItem valueOf(Role role) {
		RewardItem item = new RewardItem();
		item.setRole(role);
		return item;
	}
	public static RewardItem valueOf(CoreUnit coreUnit) {
		RewardItem item = new RewardItem();
		item.setCoreUnit(coreUnit);
		return item;
	}
	public static RewardItem valueOf(int skin) {
		RewardItem item = new RewardItem();
		item.setSkin(skin);
		return item;
	}
	public static RewardItem valueOf(Equip equip) {
		RewardItem item = new RewardItem();
		item.setEquip(equip);
		return item;
	}

	public int getId() {
		return id;
	}

	public void setId(int configId) {
		this.id = configId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public int getSkin() {
		return skin;
	}

	public void setSkin(int skin) {
		this.skin = skin;
	}

	public CoreUnit getCoreUnit() {
		return coreUnit;
	}

	public void setCoreUnit(CoreUnit coreUnit) {
		this.coreUnit = coreUnit;
	}

	public Equip getEquip() {
		return equip;
	}

	public void setEquip(Equip equip) {
		this.equip = equip;
	}


	public int getStrategyCard() {
		return strategyCard;
	}

	public void setStrategyCard(int strategyCard) {
		this.strategyCard = strategyCard;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RewardItem other = (RewardItem) obj;
		if (id == other.id)
			return true;
		return false;
	}

}
