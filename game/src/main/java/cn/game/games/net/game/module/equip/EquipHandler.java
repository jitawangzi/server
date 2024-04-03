package cn.game.games.net.game.module.equip;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.base.PlayerCacheFactory;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.op.impl.PropertyOp;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.RoleConfig;
import cn.game.protocol.generated.manager.RoleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.EquipMsg;

/**
 * 装备
 */

@Component
public class EquipHandler extends BaseHandler {

	@Override
	protected int getModule() {

		return 0x09;
	}

	@Override
	protected void inititialize() {

//		putInvoker(PbProtocol.EquipmentWearRequest_09000001, this::wear);
//
//		putInvoker(PbProtocol.EquipmentTeardownRequest_09000003, this::teardown);
//
//		putInvoker(PbProtocol.EquipmentStrengthRequest_09000007, this::equipStrength);
	}


	/**
	 * 卸下
	 * @param client
	 * @param message
	 */
	protected void teardown(NetClient client, Object message) {
		EquipMsg.EquipmentTeardownRequest_09000003 req = (EquipMsg.EquipmentTeardownRequest_09000003) message;
		EquipMsg.EquipmentTeardownResponse_09000004.Builder resp = EquipMsg.EquipmentTeardownResponse_09000004.newBuilder();
		long playerId = client.getPlayerId();

		int roleId = req.getRoleId();
		int slot = req.getSlot();
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		if (roleConfig == null) {
			client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		List<Integer> equipmentSlot = roleConfig.getEquipmentSlot();
		if (slot <= 0 || slot > equipmentSlot.size()) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (equipmentSlot.get(slot - 1) == GameConstants.FIXED_EQUIP) {
			client.sendProtocol(resp, ErrorMsgEnum.illegal_request.getId());
			return;
		}
//		EquipOp equipOp = PlayerCacheFactory.getCache(playerId, EquipOp.class);
//
//		if (!equipOp.teardown(roleId, slot)) {
//			client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
//			return ;
//		}

		PropertyOp propertyOp = PlayerCacheFactory.getCache(playerId, PropertyOp.class);
		propertyOp.initPropertyById(roleId);

		client.sendProtocol(resp.build());
	}

	/**
	 * 装备/更换
	 * @param client
	 * @param message
	 */
	protected void wear(NetClient client, Object message) {
		EquipMsg.EquipmentWearRequest_09000001 request = (EquipMsg.EquipmentWearRequest_09000001) message;
		EquipMsg.EquipmentWearResponse_09000002.Builder response = EquipMsg.EquipmentWearResponse_09000002.newBuilder();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		String uid = request.getUid();
		if (uid.isEmpty()) {
			client.sendProtocol(response, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		int roleId = request.getRoleId();
		int slot = request.getSlot();
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		if (roleConfig == null) {
			client.sendProtocol(response, ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		List<Integer> equipmentSlot = roleConfig.getEquipmentSlot();
		if (slot <= 0 || slot > equipmentSlot.size()) {
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (equipmentSlot.get(slot - 1) == GameConstants.FIXED_EQUIP) {
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		EquipModule equipOp = player.getModule(EquipModule.class);
		int code = equipOp.equip(Long.parseLong(uid), roleId, slot);
		if (code == ErrorMsgEnum.ok.getId()) {
			PropertyOp propertyOp = PlayerCacheFactory.getCache(playerId, PropertyOp.class);
			propertyOp.initPropertyById(roleId);
			//触发事件
//			EventHelper.handleEvent(playerId, new GameEvent(EventTypeEnum.EquipmentOrReplace, Long.parseLong(uid)));
		}
		client.sendProtocol(response, code);
	}

	private void equipStrength(NetClient client, Object message) {
		EquipMsg.EquipmentStrengthRequest_09000007 request = (EquipMsg.EquipmentStrengthRequest_09000007) message;
		EquipMsg.EquipmentStrengthResponse_09000008.Builder response = EquipMsg.EquipmentStrengthResponse_09000008.newBuilder();
		long playerId = client.getPlayerId();
		Player player = PlayerManager.getInstance().getPlayer(playerId);

		int roleId = request.getRoleId();
		int slot = request.getSlot();
		RoleConfig roleConfig = RoleManager.getInstance().getRoleConfig(roleId);
		if (roleConfig == null) {
			client.sendProtocol(response, ErrorMsgEnum.config_data_not_found.getId());
			return;
		}
		List<Integer> equipmentSlot = roleConfig.getEquipmentSlot();
		if (slot <= 0 || slot > equipmentSlot.size()) {
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (equipmentSlot.get(slot - 1) == GameConstants.DECORATION_EQUIP) {
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		EquipModule equipOp = player.getModule(EquipModule.class);
//		int code = equipOp.equipStrength(roleId, slot);
//		if (code == ErrorMsgEnum.ok.getId()) {
//			PropertyOp propertyOp = PlayerCacheFactory.getCache(playerId, PropertyOp.class);
//			propertyOp.initPropertyById(roleId);
//		}
		client.sendProtocol(response, 1);
	}

	/*private void equipStrength(NetClient client, Object message) {
		EquipMsg.EquipmentStrengthRequest_09000007 request = (EquipMsg.EquipmentStrengthRequest_09000007) message;
		EquipMsg.EquipmentStrengthResponse_09000008.Builder response = EquipMsg.EquipmentStrengthResponse_09000008.newBuilder();
		long playerId = client.getPlayerId();
		ExploreOp exploreOp = PlayerCacheFactory.getCache(playerId, ExploreOp.class);
		if (exploreOp.isInExploreLevel()) { //探索中禁用
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		long equipId = Long.parseLong(request.getUid());
		ProtocolStringList uidList = request.getConsumeUidsList();
		if (uidList.size() == 0) {
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		EquipOp equipOp = PlayerCacheFactory.getCache(playerId, EquipOp.class);
		if (!equipOp.getEquips().containsKey(equipId)) {
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		Equip equip = equipOp.getEquips().get(equipId);
		EquipmentConfig equipmentConfig = EquipmentManager.getInstance().getEquipmentConfig(equip.getDictId());
		if (equipmentConfig.getStrengthenLevel() == 0) {//不需要强化
			client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
			return;
		}
		if (equip.getStrength() >= (byte)equipmentConfig.getStrengthenLevel()) {//达到强化最大等级
			client.sendProtocol(response, ErrorMsgEnum.level_reach_limit.getId());
			return;
		}
		EquipmentStrengthenCostConfig costConfig = EquipmentStrengthenCostManager.getInstance().getEquipmentStrengthenCostConfig(equip.getStrength() + 1);
		//强化所需经验
		List<Map.Entry<Integer, Integer>> levelupCost = null;
		//金币消耗
		List<Integer> equipmentCost = null;
		if (exploreOp.isInExplore() && exploreOp.isInExploreCamp()) {//在局间
			if (equipmentConfig.getKind() != GameConstants.EXPLORE_EQUIP) {
				client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
				return;
			}
			levelupCost = costConfig.getExploreExp();
			equipmentCost = GlobalConst.exploreEquipmentCost;
		} else {
			if (equipmentConfig.getKind() == GameConstants.EXPLORE_EQUIP) {
				client.sendProtocol(response, ErrorMsgEnum.illegal_request.getId());
				return;
			}
			levelupCost = costConfig.getStandardExp();
			equipmentCost = GlobalConst.standardEquipmentCost;
		}
		if (levelupCost == null || equipmentCost == null) {
			client.sendProtocol(response, ErrorMsgEnum.player_check_error.getId());
			return;

		int exp = equip.getExp();
		if (exp >= levelupCost.get(0).getValue()) {
			client.sendProtocol(response, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		List<Long> list = uidList.stream().map(e -> Long.parseLong(e)).collect(Collectors.toList());
		int sumChange = equipOp.checkStrengthenChangeList(equip, levelupCost, list);
		if (sumChange == 0 || sumChange == exp) {//经验值无变化
			client.sendProtocol(response, ErrorMsgEnum.player_check_error.getId());
			return;
		}
		//检查金币消耗
		int count = equipmentCost.get(1) * (sumChange - exp);
		if (!PlayerHelper.delResources(playerId, equipmentCost.get(0), count, ResourceConsumeEnum.EquipmentStrengthen)) {
			client.sendProtocol(response, ErrorMsgEnum.resource_not_enough.getId());
			return;
		}
		//吃装备
		equipOp.deleteByIds(list);
		//强化
		if (sumChange < levelupCost.get(0).getValue()) {
			equip.setExp(sumChange); //只加经验
		} else {
			equip.setStrength((byte) (equip.getStrength() + 1));
			equip.setExp(sumChange - levelupCost.get(0).getValue());
		}
		//存库
		Equip update = new Equip();
		update.setId(equipId);
		update.setStrength(equip.getStrength());
		update.setExp(equip.getExp());
		update.setPlayerId(playerId);
		DAO.updateSelective(EquipMapper.class, update);
		PropertyOp propertyOp = PlayerCacheFactory.getCache(playerId, PropertyOp.class);
		propertyOp.init();
		client.sendProtocol(response.build());
	}*/


}
