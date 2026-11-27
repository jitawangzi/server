package cn.game.games.net.game.gm.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.game.core.exception.LogicException;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.GoodsModule;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.gm.GmAction;
import cn.game.games.net.game.gm.GmGroup;
import cn.game.games.net.game.gm.GmParam;
import cn.game.games.net.game.gm.GmScope;
import cn.game.games.net.game.gm.GmSecurityTier;
import cn.game.games.net.game.gm.RequestTraceService;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.module.item.AbstractItemIdModule;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.games.util.PbBuilder;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.BaseMsg.AssetInfo;
import cn.game.protocol.protobuf.RewardMsg.GoodsUpdatePush_55003501;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;
import io.vertx.core.Future;

/**
 * 道具与资源发放/扣除GM指令分组
 *
 * @author shuyongqiang
 */
@Component
@GmGroup(name = "item", description = "道具与资源调试指令")
public class ItemGm extends AbstractGm {

	private static final Logger log = LoggerFactory.getLogger(ItemGm.class);

	@GmAction(name = "item", description = "添加道具或资源", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void item(Player player,
			@GmParam(description = "道具或资源ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "数量", required = false, defaultValue = "0") int count) {
		List<RewardInfo> items = TestHelper.addItems(player, id, count);
		PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(items).build());
	}

	@GmAction(name = "itemdel", description = "扣除指定玩家道具或资源", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER)
	public void itemdel(Player player,
			@GmParam(description = "目标玩家ID(0代表当前玩家)", required = false, defaultValue = "0") long targetPlayerId,
			@GmParam(description = "道具或资源ID", required = false, defaultValue = "0") int id,
			@GmParam(description = "扣除数量", required = false, defaultValue = "0") int count) {
		long pid = targetPlayerId == 0 ? player.getPlayerId() : targetPlayerId;
		GameServerInterface playerProxy = ServerHelper.getPlayerProxy(pid);
		Future<?> future = playerProxy.delResources(pid, id, count);
		if (future != null) {
			try {
				future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.warn("等待 item.itemdel 远端扣除结果异常: playerId={}, itemId={}, count={}", pid, id, count, e);
				throw new LogicException(ErrorMsgEnum.unknown.ID, "扣除资源失败: " + e.getMessage());
			}
		}
	}

	/**
	 * 精确设置玩家指定货币或可堆叠道具的目标数量（方便边界值与余额不足测试）
	 *
	 * @param player 目标在线玩家
	 * @param id 货币或道具配置ID
	 * @param count 目标数量（必须大于等于0）
	 * @return 设置前后的数量变化摘要
	 */
	@GmAction(name = "set", aliases = {"itemset", "setresource"}, description = "精确设置指定货币或道具的目标数量", security = GmSecurityTier.DEV_TEST_ONLY, scope = GmScope.PLAYER, sideEffects = "直接修改玩家指定资源或道具数量为目标值", example = "item.set id=100001 count=99")
	public Map<String, Object> set(Player player,
			@GmParam(description = "货币或道具配置ID") int id,
			@GmParam(description = "目标数量(>=0)") long count) {
		if (player == null) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "目标玩家不存在");
		}
		if (id <= 0 || count < 0) {
			throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "资源ID必须大于0且目标数量不能为负数");
		}
		Asset asset = Asset.getNullable(id);
		long before;
		long after;
		if (asset != null) {
			if (asset.Type == 2) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID,
						"经验类资源(" + asset.Desc + "/" + id + ")不支持直接 item.set，请改用 player.setLevel 或 player.setVip 避免绕过升级逻辑");
			}
			before = player.getCurrencyModule().getCount(id);
			player.getCurrencyModule().setCount(id, count);
			after = player.getCurrencyModule().getCount(id);
			long delta = after - before;
			if (delta != 0) {
				RequestTraceService.recordResourceEvent(player.getPlayerId(), id, Math.abs(delta), delta > 0, OpType.Test.name());
				if (player.getGameClient() != null) {
					if (delta > 0) {
						RewardInfo info = RewardInfo.newBuilder()
								.setAsset(AssetInfo.newBuilder().setId(id).setCount(delta).build())
								.build();
						PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addRewards(info).build());
					} else {
						GoodsUpdatePush_55003501 updatePush = GoodsUpdatePush_55003501.newBuilder()
								.addUpdate(PbBuilder.buildGoodsInfo(id, after))
								.build();
						player.getGameClient().sendProtocol(updatePush);
					}
				}
			}
		} else {
			ItemConfig itemConfig = ItemManager.instance() != null ? ItemManager.instance().getNullable(id) : null;
			if (itemConfig != null && (itemConfig.ItemType == 5 || itemConfig.ItemType == 6 || itemConfig.ItemType == 7 || itemConfig.ItemType == 8)) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID,
						"道具 " + id + " 属于自动开启/折算类型(ItemType=" + itemConfig.ItemType + ")，不支持 item.set 精确设置数量，请改用 item.item 发放");
			}
			GoodsModule<?> goodsModule = player.getGoodsModule(id);
			if (goodsModule == null) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "未找到资源/道具所属模块: " + id);
			}
			before = goodsModule.getCount(id);
			long diff = count - before;
			if (goodsModule instanceof AbstractItemIdModule<?> && count > 1) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID,
						"唯一标识类物品(" + id + ")不支持设置超过 1 的数量");
			}
			if (diff < 0 && (goodsModule instanceof AbstractItemNoStackModule<?> || goodsModule instanceof AbstractItemIdModule<?>)) {
				throw new LogicException(ErrorMsgEnum.request_parameter_error.ID,
						"非堆叠/唯一ID物品(" + goodsModule.getClass().getSimpleName() + ", id=" + id + ")不支持通过配置ID扣减数量");
			}
			boolean hasClient = player.getGameClient() != null;
			if (diff > 0) {
				if (diff > Integer.MAX_VALUE) {
					throw new LogicException(ErrorMsgEnum.request_parameter_error.ID, "单次增加道具数量超出上限: " + diff);
				}
				List<RewardInfo> added = PlayerHelper.addResources(player, id, (int) diff, OpType.Test);
				if (added != null && !added.isEmpty() && hasClient) {
					PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(added).build());
				}
			} else if (diff < 0) {
				PlayerHelper.delResources(player, id, -diff, OpType.Test, hasClient);
			}
			after = goodsModule.getCount(id);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("id", id);
		result.put("name", asset != null ? asset.Desc : String.valueOf(id));
		result.put("before", before);
		result.put("after", after);
		result.put("delta", after - before);
		return result;
	}
}