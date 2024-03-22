package cn.game.games.net.game.module.item;

public class BagGrid {
	/** 格子id */
	private int id;
	/** 道具id */
	private int itemId;
	/** 道具数量 */
	private int itemCount;
	/** 装备id */
	private long equipId;
	
	public BagGrid(int gridId, int itemId, int count) {
		this.id = gridId;
		this.itemId = itemId;
		this.itemCount = count;
	}
	
	public BagGrid(int itemId, int count) {
		this.itemId = itemId;
		this.itemCount = count;
	}
	
	public BagGrid(long equipId) {
		this.equipId = equipId;
	}

	public BagGrid() {
	}
	
	public boolean isItem() {
		return this.itemId != 0;
	}

	public boolean isEquip() {
		return equipId > 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public long getEquipId() {
		return equipId;
	}

	public void setEquipId(long equipId) {
		this.equipId = equipId;
	}
	
	public boolean isEmpty() {
		return this.itemId == 0 && this.equipId == 0;
	}

	/**
	 * 清空格子
	 */
	public void clear() {
		this.itemId = 0;
		this.itemCount = 0;
		this.equipId = 0;
	}
	


	
	
	
}
