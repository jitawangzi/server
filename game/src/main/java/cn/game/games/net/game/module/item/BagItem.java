package cn.game.games.net.game.module.item;
/** 
* 
* @date 2022年12月10日 上午11:28:28 
* @author YYB 
*/
public class BagItem {
	private int gridId;
	private int itemId;
	
	public BagItem(int grid, int itemId) {
		this.gridId = grid;
		this.itemId = itemId;
	}

	public int getGridId() {
		return gridId;
	}

	public void setGridId(int gridId) {
		this.gridId = gridId;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	


}
