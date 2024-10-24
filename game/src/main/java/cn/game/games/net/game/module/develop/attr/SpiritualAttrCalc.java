package cn.game.games.net.game.module.develop.attr;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.protobuf.BaseMsg.AttrGrowInfo;
import cn.game.protocol.protobuf.DevelopMsg.QianKunMirrorInfo;

public class SpiritualAttrCalc extends PlayerAttrCalc {

	public SpiritualAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		DevelopModule developModule = player.getDevelopModule();
		QianKunMirrorInfo.Builder builder = developModule.getQiankunMirrorBuilder();
		builder.getSpiritualList().forEach(spiritual -> {
			for (AttrGrowInfo attrGrowInfo : spiritual.getAttsList()) {
				attrMap.add(attrGrowInfo.getId(), (spiritual.getLevel() - 1) * attrGrowInfo.getGrowValue() + attrGrowInfo.getStartValue());
			}
		});
	}
	@Override
	public InitialUI getFunction() {
		return InitialUI.QiankunMirror;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.QiankunMirror;
	}
}
