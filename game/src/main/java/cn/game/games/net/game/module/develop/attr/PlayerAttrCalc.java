package cn.game.games.net.game.module.develop.attr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.util.FloatMapWrapper;

public abstract class PlayerAttrCalc {
	private static final Logger logger = LoggerFactory.getLogger(PlayerAttrCalc.class);

	protected Player player;
	protected FloatMapWrapper attrMap = new FloatMapWrapper();

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
		logger.debug("开始计算玩家属性: {}", getClass().getSimpleName());
		calcAttr();
		logger.debug("计算玩家属性结束: {}", attrMap);
	}
	public abstract void calcAttr();

	public abstract InitialUI getFunction();

	public abstract AttrCalcType getAttrCalcType();

	public FloatMapWrapper getAttrMap() {
		return attrMap;
	}

}
