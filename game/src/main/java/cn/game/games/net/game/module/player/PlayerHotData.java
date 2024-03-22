package cn.game.games.net.game.module.player;

import cn.game.protocol.generated.enume.Money;
import cn.game.util.MapWrapper;

/**    
 * 玩家的热点数据
 * 2024年3月19日 下午6:38:13
 * @author SYQ
 */
public class PlayerHotData {

	/** 货币,key:  {@link Money}*/
	private MapWrapper currencyMap = new MapWrapper();
	/** 等级数据，key: {@link Money} ,似乎也不太热，先放这吧，省事*/
	private MapWrapper levelMap = new MapWrapper();

	public MapWrapper getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(MapWrapper currencyMap) {
		this.currencyMap = currencyMap;
	}

	public MapWrapper getLevelMap() {
		return levelMap;
	}

	public void setLevelMap(MapWrapper levelMap) {
		this.levelMap = levelMap;
	}


}
