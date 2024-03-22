package cn.game.games.cache.entity;

import java.io.Serializable;

public class EquipAttr implements Serializable {

	/**  */
	private static final long serialVersionUID = -7168391647706426365L;

	private int id;

	private int level;

	private int init;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getInit() {
		return init;
	}

	public void setInit(int init) {
		this.init = init;
	}

}
