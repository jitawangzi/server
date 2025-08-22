package cn.game.games.net.game.module.develop.attr;

import java.util.Map;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.module.develop.pet.Pet;
import cn.game.games.net.game.module.develop.pet.PetModule;
import cn.game.protocol.generated.config.SoulPetBookConfig;
import cn.game.protocol.generated.config.SoulPetConfig;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.SoulPetBookManager;
import cn.game.protocol.generated.manager.SoulPetManager;

public class PetAttrCalc extends PlayerAttrCalc {

	public PetAttrCalc(Player player) {
		super(player);
	}

	@Override
	public void calcAttr() {
		/*
		PetModule module = player.getPetModule();
		Pet pet = module.get(module.getBattlePetId());
		if (pet == null) {
		return;
		}
		SoulPetConfig petConfig = SoulPetManager.instance().get(pet.getConfigId());
		for (int i = 0; i < petConfig.InitialAttribute.length; i++) {
		attrMap.add(petConfig.InitialAttribute[i][0], petConfig.InitialAttribute[i][1] + (pet.getLevel() - 1) * petConfig.GrowthAttribute[i][1]);
		}
		Map<Integer, Integer> petBookMap = module.getPetBookMap();
		petBookMap.forEach((id, level) -> {
		SoulPetBookConfig soulPetBookConfig = SoulPetBookManager.instance().get(id);
		for (int i = soulPetBookConfig.SoulPetBookStar.length - 1; i >= 0; i--) {
		int[] bookStar = soulPetBookConfig.SoulPetBookStar[i];
		if (bookStar[0] <= level) {
		attrMap.add(bookStar[1], bookStar[2]);
		break;
		}
		}
		});
		*/}

	@Override
	public InitialUI getFunction() {
		return InitialUI.SoulPets;
	}

	@Override
	public AttrCalcType getAttrCalcType() {
		return AttrCalcType.Pet;
	}
}
