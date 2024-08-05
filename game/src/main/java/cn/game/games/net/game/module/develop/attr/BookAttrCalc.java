package cn.game.games.net.game.module.develop.attr;

import java.util.Collection;

import cn.game.games.cache.entity.Hero;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.hero.HeroModule;
import cn.game.protocol.generated.config.AttributeVlalueConfig;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.HeroBookConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.AttributeVlalueManager;
import cn.game.protocol.generated.manager.HeroBookManager;

public class BookAttrCalc extends PlayerAttrCalc {

	public BookAttrCalc(Player player) {
		super(player);
	}
	@Override
	public void calcAttr() {

		Collection<HeroBookConfig> list = HeroBookManager.instance().list();
		HeroModule heroModule = player.getHeroModule();
		for (HeroBookConfig heroBookConfig : list) {

			boolean active = true;
			for (int id : heroBookConfig.HeroBookCardIdGroup) {
				Collection<Hero> heros = heroModule.getByConfigId(id);
				if (heros.isEmpty()) {
					active = false;
					break;
				}
			}
			if (active) {
				for (int id : heroBookConfig.HeroBookCardIdGroup) {
					Collection<Hero> heros = heroModule.getByConfigId(id);
					Hero hero = getMaxQualityHero(heros);
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
