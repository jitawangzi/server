package cn.game.games.net.game.module.develop.attr;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.generated.config.SpiritualQualityConfig;
import cn.game.protocol.generated.config.SpiritualRootConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.SpiritualQualityManager;
import cn.game.protocol.generated.manager.SpiritualRootManager;
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
				float value = (spiritual.getLevel() - 1) * attrGrowInfo.getGrowValue() + attrGrowInfo.getStartValue();
				SpiritualQualityConfig spiritualQualityConfig = SpiritualQualityManager.instance().get(spiritual.getQuality()); 
				value *= (spiritualQualityConfig.SpiritQualityaAffixPlus / 10000f);
				attrMap.add(attrGrowInfo.getId(), (int) value);
			}
		});
		builder.getSpiritualRootIdsList().forEach(root -> {
			SpiritualRootConfig spiritualRootConfig = SpiritualRootManager.instance().get(root);
			while (spiritualRootConfig != null) {
				attrMap.add(spiritualRootConfig.AttrPurple);
				spiritualRootConfig = SpiritualRootManager.instance().getNullable(spiritualRootConfig.preID);
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
