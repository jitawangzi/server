package cn.game.games.net.game.module.develop.attr;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.generated.enume.InitialUI;

@Deprecated
public class SpiritualRootAttrCalc extends PlayerAttrCalc {

	public SpiritualRootAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		DevelopModule developModule = player.getDevelopModule();

	}
	@Override
	public InitialUI getFunction() {
		return InitialUI.WenJuan;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.NONE;
	}
}
