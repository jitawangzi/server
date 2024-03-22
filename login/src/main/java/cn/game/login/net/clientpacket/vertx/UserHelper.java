package cn.game.login.net.clientpacket.vertx;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;

import cn.game.core.cache.CacheType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.core.util.IdUtil.IdType;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.UserMapper;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.util.DateUtil;
import cn.game.util.RedissonUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

public class UserHelper {

	public static Future<Object> createUser(String account, String pwd, String channelLabel, String thirdUid,
			String sessionKey) {
		int[] createUID = GlobalConst.CreateUID;
		long playerId = IdUtil.getIdAutoIncrease(IdType.PLAYER);
		playerId = playerId - 1 + createUID[0] + createUID[1];

		UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class);

		User user = new User();
		user.setId(playerId);
		user.setUserType((byte) 3);
		user.setUsername(account);
		user.setChannelLabel(channelLabel);
		user.setThirdUid(thirdUid);
		user.setSessionKey(sessionKey);
//			user.setChannelCode(channel);
		user.setPass(pwd);
		user.setCreateDate(DateUtil.nowDateStr());
		user.setCreateTime(DateUtil.nowTimeStr());
		user.setIsGm(0);
		user.setLoginDate(DateUtil.nowDateStr());
		user.setLoginTime(DateUtil.nowTimeStr());
//			user.setDeviceUid(device);
		return VxHolder.vertx.executeBlocking(fut -> {
			mapper.insert(user);
			setUserCache(user); 
			fut.complete();
		});
	}
	
	public static void setUserCache(User user) {
		RedissonUtil.setAsync(CacheType.F_USER_NAME_ID.key(user.getUsername()), JSON.toJSONString(user), 7, TimeUnit.DAYS);
	}
	public static String getSessionKey(String sessionId) {
		User user = RedissonUtil.get(CacheType.PASSPORT_SESSION.key(sessionId),User.class);
		return user == null? null : user.getSessionKey(); 
	}
}
