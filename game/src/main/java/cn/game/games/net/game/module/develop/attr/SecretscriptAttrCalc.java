package cn.game.games.net.game.module.develop.attr;

import java.util.Collection;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.secretscript.Secretscript;
import cn.game.games.net.game.module.develop.secretscript.SecretscriptModule;
import cn.game.protocol.generated.config.SecretscriptBookConfig;
import cn.game.protocol.generated.config.SecretscriptConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.SecretscriptBookManager;
import cn.game.protocol.generated.manager.SecretscriptManager;

public class SecretscriptAttrCalc extends PlayerAttrCalc {

	public SecretscriptAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		SecretscriptModule module = player.getModule(SecretscriptModule.class);
		Collection<Secretscript> list = module.list();
		for (Secretscript secretscript : list) {
			SecretscriptConfig config = SecretscriptManager
					.instance()
					.getUISecretscriptMarkSecretscriptStar(secretscript.getConfigId(), secretscript.getStar());

			int attrValue = config.SecretscriptBase[1] + (secretscript.getLevel() - 1) * config.SecretscriptGrow[1];
			attrMap.add(config.SecretscriptBase[0], attrValue);
		}

		Collection<SecretscriptBookConfig> books = SecretscriptBookManager.instance().list();
		bookLoop: for (SecretscriptBookConfig secretscriptBookConfig : books) {
			int minLevel = Integer.MAX_VALUE;
			for (int id : secretscriptBookConfig.SecretscriptBookCardIdGroup) {
				Secretscript secretscript = module.get(id);
				if (secretscript == null) {
					continue bookLoop;
				}
				if (secretscript.getLevel() < minLevel) {
					minLevel = secretscript.getLevel();
				}
			}
			int bookLevel = 0;
			for (int[] lvCondition : secretscriptBookConfig.SecretscriptBookLvCondition) {
				if (minLevel >= lvCondition[1]) {
					bookLevel = lvCondition[0];
				}
			}
			if (bookLevel > 0) {
				for (int[] attr : secretscriptBookConfig.SecretscriptBookStar) {
					attrMap.add(attr[0], attr[1] * bookLevel);
				}
			}
		}
	}
	@Override
	public InitialUI getFunction() {
		return InitialUI.Avatar;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.Secretscript;
	}
}
