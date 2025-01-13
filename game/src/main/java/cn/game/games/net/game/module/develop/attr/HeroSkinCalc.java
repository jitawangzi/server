package cn.game.games.net.game.module.develop.attr;

import java.util.Set;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.protocol.generated.enume.InitialUI;

public class HeroSkinCalc extends PlayerAttrCalc {

	public HeroSkinCalc(Player player) {
		super(player);
	}
	@Override
	public void calcAttr() {
		Set<Integer> idsSet = player.getPlayerModule().getIdsSet(IdConstant.HERO_SKIN);
//		for (Integer integer : idsSet) {
//			HeroSkinConfig heroSkinConfig = HeroSkinManager.instance().get(integer);
//			attrMap.addAll(heroSkinConfig.AttributeVlalue);
//		}
	}

	@Override
	public InitialUI getFunction() {
		return InitialUI.Main;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.HeroSkin;
	}
}
