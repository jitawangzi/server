package cn.game.games.net.game.module.develop.gem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.develop.equip.EquipModule;
import cn.game.games.net.game.module.develop.equip.EquipPart;
import cn.game.protocol.generated.config.GemConfig;
import cn.game.protocol.generated.manager.GemManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.GemMsg.GemComposeRequest_10000007;
import cn.game.protocol.protobuf.GemMsg.GemComposeResponse_10000008;
import cn.game.protocol.protobuf.GemMsg.GemLockRequest_10000005;
import cn.game.protocol.protobuf.GemMsg.GemLockResponse_10000006;
import cn.game.protocol.protobuf.GemMsg.GemTeardownRequest_10000003;
import cn.game.protocol.protobuf.GemMsg.GemTeardownResponse_10000004;
import cn.game.protocol.protobuf.GemMsg.GemWearRequest_10000001;
import cn.game.protocol.protobuf.GemMsg.GemWearResponse_10000002;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

@Component
public class GemHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x10;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.GemWearRequest_10000001, this::wear);
        putInvoker(PbProtocol.GemTeardownRequest_10000003, this::teardown);
        putInvoker(PbProtocol.GemLockRequest_10000005, this::lock);
        putInvoker(PbProtocol.GemComposeRequest_10000007, this::compose);
    }

    private void wear(NetClient client, Object message) {
        GemWearRequest_10000001 req = (GemWearRequest_10000001) message;
		long uid = Long.parseLong(req.getUid());
        int pos = req.getPos();
        GemWearResponse_10000002 defaultInstance = GemWearResponse_10000002.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		GemModule gemModule = player.getModule(GemModule.class);
		EquipModule equipModule = player.getModule(EquipModule.class);

		Gem gem = gemModule.get(uid);
		if (gem == null) {
			// 宝石不存在
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.ID);
			return;
		}
		GemConfig gemConfig = GemManager.instance().get(gem.getConfigId());

		EquipPart equipPart = equipModule.getEquipPart(gemConfig.pos);
		Map<Long, Integer> gemPosMap = equipPart.getGemPosMap();
		if (gemPosMap.containsKey(uid)) {
			// 宝石已经镶嵌
			client.sendProtocol(defaultInstance, ErrorMsgEnum.repeat_request.ID);
			return;
		}
		// 同属性词条宝石只能上一个
		Set<Integer> keySet = gem.getGemAttrs().keySet();
		gemPosMap.forEach((k, v) -> {
			Gem gemWeared = gemModule.get(k);
			for (Integer attrId : keySet) {
				if (gemWeared.getGemAttrs().containsKey(attrId)) {
					player.fail(ErrorMsgEnum.request_parameter_error) ; 
					return;
				}
			}
		});
		Iterator<Entry<Long, Integer>> iterator = gemPosMap.entrySet().iterator(); 
		while (iterator.hasNext()) {
			Map.Entry<java.lang.Long, java.lang.Integer> entry = (Map.Entry<java.lang.Long, java.lang.Integer>) iterator.next();
			if (entry.getValue() == pos) {
                // 该位置已经有宝石
				iterator.remove(); // 移除掉
				break ; 
            }
		}
		gemPosMap.put(uid, pos);

        client.sendProtocol(defaultInstance);
    }

    private void teardown(NetClient client, Object message) {
        GemTeardownRequest_10000003 req = (GemTeardownRequest_10000003) message;
		long uid = Long.parseLong(req.getUid());
        GemTeardownResponse_10000004 defaultInstance = GemTeardownResponse_10000004.getDefaultInstance();

        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		GemModule gemModule = player.getModule(GemModule.class);
		EquipModule equipModule = player.getModule(EquipModule.class);

		Gem gem = gemModule.get(uid);
		if (gem == null) {
			// 宝石不存在
			client.sendProtocol(defaultInstance, ErrorMsgEnum.player_data_not_found.ID);
			return;
		}

		GemConfig gemConfig = GemManager.instance().get(gem.getConfigId());
		EquipPart equipPart = equipModule.getEquipPart(gemConfig.pos);

		Map<Long, Integer> gemPosMap = equipPart.getGemPosMap();
		gemPosMap.remove(uid);

        client.sendProtocol(defaultInstance);
    }

    private void lock(NetClient client, Object message) {
        GemLockRequest_10000005 req = (GemLockRequest_10000005) message;
		List<String> uids = req.getUidList();
        boolean lock = req.getLock();
        GemLockResponse_10000006 defaultInstance = GemLockResponse_10000006.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		GemModule gemModule = player.getModule(GemModule.class);

		for (String string : uids) {
			Gem gem = gemModule.get(Long.parseLong(string));
			if (gem != null) {
				gem.setLock(lock);
			}
		}
        client.sendProtocol(defaultInstance);
    }

    private void compose(NetClient client, Object message) {
        GemComposeRequest_10000007 req = (GemComposeRequest_10000007) message;
        List<String> uidsList = req.getUidsList();
        GemComposeResponse_10000008 defaultInstance = GemComposeResponse_10000008.getDefaultInstance();
        GemComposeResponse_10000008.Builder resp = GemComposeResponse_10000008.newBuilder();

        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		GemModule gemModule = player.getModule(GemModule.class);

		// 按同位置，同品质的进行分组，后续合成
		Map<Integer, Map<Integer, List<Long>>> posQualityMap = new HashMap<Integer, Map<Integer, List<Long>>>();

		for (String string : uidsList) {
			Gem gem = gemModule.get(Long.parseLong(string));
			if (gem == null) {
				continue;
			}
			GemConfig gemConfig = GemManager.instance().get(gem.getConfigId());

			Map<Integer, List<Long>> qualityMap = posQualityMap.computeIfAbsent(gemConfig.pos, k -> new HashMap<Integer, List<Long>>());
			qualityMap.computeIfAbsent(gemConfig.quality, k -> new java.util.ArrayList<Long>()).add(gem.getId());
		}

		// 遍历分组，进行合成
		for (Entry<Integer, Map<Integer, List<Long>>> posEntry : posQualityMap.entrySet()) {

			int pos = posEntry.getKey();
			Map<Integer, List<Long>> qualityMap = posEntry.getValue();
			for (Entry<Integer, List<Long>> qualityEntry : qualityMap.entrySet()) {
				int quality = qualityEntry.getKey();
				int nextQuality = quality + 1; // 下一个品质
				GemConfig nextGemConfig = GemManager.instance().getUIPosquality(pos, nextQuality);
				GemConfig curGemConfig = GemManager.instance().getUIPosquality(pos, quality);
				if (nextGemConfig == null) {
					// 没有下一个品质的宝石配置，不能合成
					continue;
				}

				List<Long> gemIds = qualityEntry.getValue();
				int addGemCount = gemIds.size() / curGemConfig.composeCount;
				if (addGemCount <= 0) {
					// 宝石不足，不能合成
					continue;
				}
				// 删除原有宝石
				gemModule.delBatch(gemIds,OpType.GemCompose);
				// 添加新宝石
				List<RewardInfo> reward = PlayerHelper.addResources(player, nextGemConfig.ID,addGemCount, OpType.GemCompose);
				resp.addAllRewards(reward);
			}
		}
        client.sendProtocol(resp.build());
    }
}
