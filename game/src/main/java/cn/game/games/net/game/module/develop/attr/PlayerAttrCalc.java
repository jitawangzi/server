package cn.game.games.net.game.module.develop.attr;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.util.IntMapWrapper;

public abstract class PlayerAttrCalc {
	protected Player player;
	protected IntMapWrapper attrMap = new IntMapWrapper();

	public PlayerAttrCalc() {

	}

	public PlayerAttrCalc(Player player) {
		this.player = player;
	}

	public void reCalcAttr() {
		if (!player.isFuncOpen(getFunction())) {
			return;
		}
		attrMap.clear();
		calcAttr();
	}
	public abstract void calcAttr();

	public abstract InitialUI getFunction();

	public abstract AttrCalcType getAttrCalcType();

	public IntMapWrapper getAttrMap() {
		return attrMap;
	}

}
