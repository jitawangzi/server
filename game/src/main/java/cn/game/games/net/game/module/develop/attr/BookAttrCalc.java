package cn.game.games.net.game.module.develop.attr;

import cn.game.games.cache.entity.Player;
import cn.game.protocol.generated.config.HeroBandBookConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.HeroBandBookManager;

public class BookAttrCalc extends PlayerAttrCalc {

	public BookAttrCalc(Player player) {
		super(player);
	}
	@Override
	public void calcAttr() {
		int level = player.getLevel(Asset.CatalogPoints);
		HeroBandBookConfig heroBandBookConfig = HeroBandBookManager.instance().getNullable(level);
		if (heroBandBookConfig != null) {
			attrMap.addAllInt(heroBandBookConfig.AttributeVlalue);
		}
	}

	@Override
	public InitialUI getFunction() {
		return InitialUI.CardBook;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.HeroBook;
	}
}
