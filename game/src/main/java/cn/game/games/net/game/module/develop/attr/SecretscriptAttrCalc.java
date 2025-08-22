package cn.game.games.net.game.module.develop.attr;

import java.util.Collection;
import java.util.List;

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
		/*
		SecretscriptModule module = player.getModule(SecretscriptModule.class);
		Collection<Secretscript> list = module.list();
		for (Secretscript secretscript : list) {
		SecretscriptConfig config = SecretscriptManager.instance()
		.getUISecretscriptMarkSecretscriptStar(secretscript.getConfigId(), secretscript.getStar());
		
		for (int i = 0; i < config.SecretscriptBase.length; i++) {
		int[] base = config.SecretscriptBase[i];
		int[] baseGrow = config.SecretscriptGrow[i];
		int attrValue = base[1] + (secretscript.getLevel() - 1) * baseGrow[1];
		attrMap.add(base[0], attrValue);
		}
		}
		
		Collection<List<SecretscriptBookConfig>> booksList = SecretscriptBookManager.instance().list();
		for (List<SecretscriptBookConfig> books : booksList) {
		SecretscriptBookConfig levelConfig = books.get(0);
		int minLevel = Integer.MAX_VALUE;
		for (int id : levelConfig.SecretscriptBookCardIdGroup) {
		Secretscript secretscript = module.get(id);
		if (secretscript == null) {
		continue;
		}
		if (secretscript.getLevel() < minLevel) {
		minLevel = secretscript.getLevel();
		}
		}
		if (minLevel == Integer.MAX_VALUE) {
		continue;
		}
		int bookLevel = 0;
		SecretscriptBookConfig attrConfig = null;
		for (SecretscriptBookConfig secretscriptBookConfig : books) {
		if (minLevel >= secretscriptBookConfig.SecretscriptBookLvCondition) {
		bookLevel = secretscriptBookConfig.SecretscriptBookLv;
		attrConfig = secretscriptBookConfig;
		}
		}
		if (bookLevel == 0) {
		continue;
		}
		for (int[] attr : attrConfig.SecretscriptBookStar) {
		attrMap.add(attr[0], attr[1]);
		}
		}
		
		*/}

	@Override
	public InitialUI getFunction() {
		return InitialUI.Avatar;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.Secretscript;
	}
}
