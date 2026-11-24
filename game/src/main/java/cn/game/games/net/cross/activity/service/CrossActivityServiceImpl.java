package cn.game.games.net.cross.activity.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import cn.game.core.net.remote.RemoteProxy;
import cn.game.games.net.cross.activity.dto.CrossActivityActionResult;
import cn.game.games.net.cross.activity.dto.CrossActivityCreateResult;
import cn.game.games.net.cross.activity.dto.CrossActivitySnapshot;

/**
 * 跨服活动 RPC 服务实现
 * 严格对齐 GuildService 模式，提供 Spring Bean 单例保证
 * 
 * @author shuyongqiang
 */
@Component
public class CrossActivityServiceImpl implements RemoteProxy, CrossActivityServiceInterface {

	private static final Logger log = LoggerFactory.getLogger(CrossActivityServiceImpl.class);
	private static final CrossActivityServiceInterface INSTANCE = new CrossActivityServiceImpl();

	public static CrossActivityServiceInterface getInstance() {
		return INSTANCE;
	}

	@Bean
	@Primary
	public static CrossActivityServiceInterface crossActivityService() {
		return INSTANCE;
	}

	@Override
	public CrossActivityCreateResult createOrEnsureActivity(long suggestedUid, int configId, String scopeKey, String serverId) {
		return CrossActivityService.getInstance().createOrEnsureActivity(suggestedUid, configId, scopeKey, serverId);
	}

	@Override
	public CrossActivitySnapshot queryActivitySnapshot(long activityUid, long playerId) {
		return CrossActivityService.getInstance().queryActivitySnapshot(activityUid, playerId);
	}

	@Override
	public CrossActivityActionResult executePlayerAction(long activityUid, long playerId, String actionType, String payloadJson) {
		return CrossActivityService.getInstance().executePlayerAction(activityUid, playerId, actionType, payloadJson);
	}

	@Override
	public CrossActivitySnapshot joinActivity(long activityUid, long playerId, String serverId, Map<String, Object> extraData) {
		return CrossActivityService.getInstance().joinActivity(activityUid, playerId, serverId, extraData);
	}
}
