package cn.game.games.net.game.module.develop.attr;

import java.util.Collection;

import com.google.common.collect.Multimap;

import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.protocol.generated.config.AttributeVlalueConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.AttributeVlalueManager;

public class BookAttrCalc extends PlayerAttrCalc {

	public BookAttrCalc(Player player) {
		super(player);
	}
	@Override
	public void calcAttr() {

		HeroModule heroModule = player.getHeroModule();
		Multimap<Integer, Hero> id_items = heroModule.getId_items();
		for (Integer id : id_items.keySet()) {
			Hero hero = getMaxQualityHero(id_items.get(id));
			Integer attrId = GlobalConst.HeroBookStar.get(hero.getQuality());
			if (attrId == null) {
				continue;
			}
			AttributeVlalueConfig attributeVlalueConfig = AttributeVlalueManager.instance().get(attrId);
			attributeVlalueConfig.AttributeVlalue.forEach((k, v) -> {
				attrMap.add(k, v * hero.getStar());
			});
		}
	}

	private Hero getMaxQualityHero(Collection<Hero> heros) {
		Hero ret = null;
		for (Hero hero : heros) {
			if (ret == null) {
				ret = hero;
			} else {
				if (ret.getQuality() < hero.getQuality()) {
					ret = hero;
				} else if (ret.getQuality() == hero.getQuality()) {
					if (ret.getStar() < hero.getStar()) {
						ret = hero;
					}
				}
			}
		}
		return ret;

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
