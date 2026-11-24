package cn.game.games.net.cross.activity.service;

import java.util.Map;

import cn.game.core.net.remote.RemoteCrossServerInterface;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossActivityCreateResult;
import cn.game.games.net.cross.activity.dto.CrossActivitySnapshot;

/**
 * 跨服活动远程 RPC 接口
 * 
 * @author shuyongqiang
 */
public interface CrossActivityServiceInterface extends RemoteCrossServerInterface {

	CrossActivityCreateResult createOrEnsureActivity(long suggestedUid, int configId, String scopeKey, String serverId);

	CrossActivitySnapshot queryActivitySnapshot(long activityUid, long playerId);

	CrossActivityActionResult executePlayerAction(long activityUid, long playerId, String actionType, String payloadJson);

	CrossActivitySnapshot joinActivity(long activityUid, long playerId, String serverId, Map<String, Object> extraData);
}
