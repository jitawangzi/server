package cn.game.games.net.game.module.develop.pet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.SoulPetBookConfig;
import cn.game.protocol.generated.config.SoulPetConfig;
import cn.game.protocol.generated.config.SoulPetSkillConfig;
import cn.game.protocol.generated.config.SoulPetlLvupConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.SoulPetBookManager;
import cn.game.protocol.generated.manager.SoulPetManager;
import cn.game.protocol.generated.manager.SoulPetSkillManager;
import cn.game.protocol.generated.manager.SoulPetlLvupManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.PetMsg.PetBattleRequest_19000011;
import cn.game.protocol.protobuf.PetMsg.PetBattleResponse_19000012;
import cn.game.protocol.protobuf.PetMsg.PetBondsActivateRequest_19000013;
import cn.game.protocol.protobuf.PetMsg.PetBondsActivateResponse_19000014;
import cn.game.protocol.protobuf.PetMsg.PetBondsUpLevelRequest_19000015;
import cn.game.protocol.protobuf.PetMsg.PetBondsUpLevelResponse_19000016;
import cn.game.protocol.protobuf.PetMsg.PetBreakUpRequest_19000005;
import cn.game.protocol.protobuf.PetMsg.PetBreakUpResponse_19000006;
import cn.game.protocol.protobuf.PetMsg.PetCompositeRequest_19000001;
import cn.game.protocol.protobuf.PetMsg.PetCompositeResponse_19000002;
import cn.game.protocol.protobuf.PetMsg.PetRefineRequest_19000007;
import cn.game.protocol.protobuf.PetMsg.PetRefineResponse_19000008;
import cn.game.protocol.protobuf.PetMsg.PetRefineSaveRequest_19000009;
import cn.game.protocol.protobuf.PetMsg.PetRefineSaveResponse_1900000a;
import cn.game.protocol.protobuf.PetMsg.PetUpLevelRequest_19000003;
import cn.game.protocol.protobuf.PetMsg.PetUpLevelResponse_19000004;
import cn.game.util.Rnd;

@Component
public class PetHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x19;
    }

	@Override
	protected InitialUI getInitialUI() {
		return InitialUI.Pet;
	}

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.PetCompositeRequest_19000001, this::composite);
        putInvoker(PbProtocol.PetUpLevelRequest_19000003, this::upLevel);
        putInvoker(PbProtocol.PetBreakUpRequest_19000005, this::breakUp);
        putInvoker(PbProtocol.PetRefineRequest_19000007, this::refine);
        putInvoker(PbProtocol.PetRefineSaveRequest_19000009, this::refineSave);
        putInvoker(PbProtocol.PetBattleRequest_19000011, this::battle);
        putInvoker(PbProtocol.PetBondsActivateRequest_19000013, this::bondsActivate);
        putInvoker(PbProtocol.PetBondsUpLevelRequest_19000015, this::bondsUpLevel);
    }

    private void composite(NetClient client, Object message) {
        PetCompositeRequest_19000001 req = (PetCompositeRequest_19000001) message;
        int id = req.getId();
        PetCompositeResponse_19000002 defaultInstance = PetCompositeResponse_19000002.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
		PetModule petModule = player.getPetModule();
		Pet pet = petModule.get(id);
		if (pet != null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		SoulPetlLvupConfig soulPetlLvupConfig = SoulPetlLvupManager.instance().get(0);
		SoulPetConfig soulPetConfig = SoulPetManager.instance().get(id);
		PlayerHelper.delResources(player, soulPetConfig.PieceID, soulPetlLvupConfig.LvConsumeItem, OpType.SoulPet);
		petModule.add(id, OpType.SoulPet);

        client.sendProtocol(defaultInstance);
    }

    private void upLevel(NetClient client, Object message) {
        PetUpLevelRequest_19000003 req = (PetUpLevelRequest_19000003) message;
        int id = req.getId();
        PetUpLevelResponse_19000004 defaultInstance = PetUpLevelResponse_19000004.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }

		PetModule petModule = player.getPetModule();
		Pet pet = petModule.get(id);
		if (pet == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		SoulPetlLvupConfig soulPetlLvupConfig = SoulPetlLvupManager.instance().get(pet.getLevel());
		SoulPetlLvupConfig soulPetlLvupConfigNext = SoulPetlLvupManager.instance().getNullable(pet.getLevel() + 1);
		if (soulPetlLvupConfigNext == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.level_limit.getId());
			return;
		}
		SoulPetConfig soulPetConfig = SoulPetManager.instance().get(id);
		int[][] breakConsumeItem = soulPetConfig.BreakConsumeItem;
		for (int[] is : breakConsumeItem) {
			if (is[0] == pet.getLevel() && pet.getBreakLevel() != pet.getLevel()) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.illegal_request.getId());
				return;
			}
		}
		int[] cost = new int[4] ; 
		cost[0] = soulPetConfig.PieceID ;
		cost[1] = soulPetlLvupConfig.LvConsumeItem ;
		cost[2] = Asset.gold.ID ;
		cost[3] = soulPetlLvupConfig.LvConsumeMoney ;
		
		PlayerHelper.delResources(player, cost, OpType.SoulPet);
		pet.setLevel(pet.getLevel() + 1);

        client.sendProtocol(defaultInstance);
    }

    private void breakUp(NetClient client, Object message) {
        PetBreakUpRequest_19000005 req = (PetBreakUpRequest_19000005) message;
        int id = req.getId();
        PetBreakUpResponse_19000006 defaultInstance = PetBreakUpResponse_19000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
		PetModule petModule = player.getPetModule();
		Pet pet = petModule.get(id);
		if (pet == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		SoulPetConfig soulPetConfig = SoulPetManager.instance().get(id);
		int[][] breakConsumeItem = soulPetConfig.BreakConsumeItem;
		boolean canBreak = false ; 
		int itemCount = 0;
		for (int[] is : breakConsumeItem) {
			if (is[0] == pet.getLevel()) {
				canBreak = true;
				itemCount = is[1];
				break;
			}
		}
		if (!canBreak) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (pet.getBreakLevel() == pet.getLevel()) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.getId());
			return;
		}
		PlayerHelper.delResources(player, 205060, itemCount, OpType.SoulPet);
		pet.setBreakLevel(pet.getLevel());

        client.sendProtocol(defaultInstance);
    }

    private void refine(NetClient client, Object message) {
        PetRefineRequest_19000007 req = (PetRefineRequest_19000007) message;
        int id = req.getId();
        PetRefineResponse_19000008 defaultInstance = PetRefineResponse_19000008.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
		PetModule petModule = player.getPetModule();
		Pet pet = petModule.get(id);
		if (pet == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		SoulPetConfig soulPetConfig = SoulPetManager.instance().get(id);

		PlayerHelper.delResources(player, 205030, soulPetConfig.SkillResetConsume, OpType.SoulPet);
		int randomIndex = Rnd.randomIndex(soulPetConfig.SkillNumProbability);
		int skillCount = soulPetConfig.SkillNum[randomIndex];
		List<Integer> skillsList = new ArrayList<>();
		List<Integer> skillsGroupList = new ArrayList<>();
		for (int i = 0; i < skillCount; i++) {
			SoulPetSkillConfig randomElement = Rnd.randomElement(SoulPetSkillManager.instance().list(), r -> r.Probability);
			if (skillsGroupList.contains(randomElement.IndexId)) {
				i--;
				continue;
			}
			skillsGroupList.add(randomElement.IndexId);
			skillsList.add(randomElement.ID);
		}

        PetRefineResponse_19000008.Builder resp = PetRefineResponse_19000008.newBuilder();
		resp.addAllSkills(skillsList);
		pet.setSkillsToSaveList(skillsList);

        client.sendProtocol(resp.build());
    }

    private void refineSave(NetClient client, Object message) {
        PetRefineSaveRequest_19000009 req = (PetRefineSaveRequest_19000009) message;
		int id = req.getId();
        PetRefineSaveResponse_1900000a defaultInstance = PetRefineSaveResponse_1900000a.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
		PetModule petModule = player.getPetModule();
		Pet pet = petModule.get(id);
		if (pet == null) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		if (pet.getSkillsToSaveList().isEmpty()) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
			return;
		}
		pet.getSkillsList().clear(); 
		pet.getSkillsList().addAll(pet.getSkillsToSaveList());
		pet.getSkillsToSaveList().clear();

        client.sendProtocol(defaultInstance);
    }

    private void battle(NetClient client, Object message) {
        PetBattleRequest_19000011 req = (PetBattleRequest_19000011) message;
        int id = req.getId();
        PetBattleResponse_19000012 defaultInstance = PetBattleResponse_19000012.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
		PetModule petModule = player.getPetModule();
		if (id > 0) {
			Pet pet = petModule.get(id);
			if (pet == null) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.getId());
				return;
			}
		}
		petModule.setBattlePetId(id);

        client.sendProtocol(defaultInstance);
    }

    private void bondsActivate(NetClient client, Object message) {
        PetBondsActivateRequest_19000013 req = (PetBondsActivateRequest_19000013) message;
        int id = req.getId();
        PetBondsActivateResponse_19000014 defaultInstance = PetBondsActivateResponse_19000014.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }
		PetModule petModule = player.getPetModule();
		Map<Integer, Integer> petBookMap = petModule.getPetBookMap();
		if (petBookMap.containsKey(id)) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.getId());
			return;
		}
        SoulPetBookConfig soulPetBookConfig = SoulPetBookManager.instance().get(id); 
		int[] soulPetBookCardIdGroup = soulPetBookConfig.SoulPetBookCardIdGroup;
		for (int i : soulPetBookCardIdGroup) {
			if (!petModule.has(i)) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.illegal_request.getId());
				return;
			}
		}
		PlayerHelper.addResources(player, Asset.diamond.ID, soulPetBookConfig.SoulPetBookAward);

		petBookMap.put(id, 1);
        client.sendProtocol(defaultInstance);
    }

    private void bondsUpLevel(NetClient client, Object message) {
        PetBondsUpLevelRequest_19000015 req = (PetBondsUpLevelRequest_19000015) message;
        int id = req.getId();
        PetBondsUpLevelResponse_19000016 defaultInstance = PetBondsUpLevelResponse_19000016.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
        if (!player.isFuncOpen(InitialUI.Pet)) {
            client.sendProtocol(defaultInstance, ErrorMsgEnum.func_not_open.getId());
            return;
        }

		PetModule petModule = player.getPetModule();
		Map<Integer, Integer> petBookMap = petModule.getPetBookMap();
		if (!petBookMap.containsKey(id)) {
			client.sendProtocol(defaultInstance, ErrorMsgEnum.illegal_request.getId());
			return;
		}

		SoulPetBookConfig soulPetBookConfig = SoulPetBookManager.instance().get(id);

		int level = soulPetBookConfig.SoulPetBookLvCondition.get(petBookMap.get(id) + 1);
		int[] soulPetBookCardIdGroup = soulPetBookConfig.SoulPetBookCardIdGroup;
		for (int i : soulPetBookCardIdGroup) {
			Pet pet = petModule.get(i);
			if (pet == null || pet.getLevel() < level) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.level_not_enough.getId());
				return;
			}
		}
		PlayerHelper.addResources(player, Asset.diamond.ID, soulPetBookConfig.SoulPetBookAward);

		petBookMap.put(id, petBookMap.get(id) + 1);
        client.sendProtocol(defaultInstance);
    }
}
