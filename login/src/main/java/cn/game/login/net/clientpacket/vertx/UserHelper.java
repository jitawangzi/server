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

	public static User createUser(String account, String pwd, String channelLabel, String thirdUid,
			String sessionKey,long sessionId) {
		int[] createUID = GlobalConst.CreateUID;
		long playerId = IdUtil.getIdAutoIncrease(IdType.PLAYER);
		playerId = playerId - 1 + createUID[0] + createUID[1];

		UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class);

		User user = new User();
		user.setId(playerId);
		user.setUserType((byte) 1);
		user.setUsername(account);
		user.setChannelLabel(channelLabel);
		user.setThirdUid(thirdUid);
		user.setSessionKey(sessionKey);
//			user.setChannelCode(channel);
		user.setPass(pwd);
		user.setCreateDate(DateUtil.nowDateStr());
		user.setCreateTime(DateUtil.nowTimeStr());
		user.setIsGm(false);
		user.setLoginDate(DateUtil.nowDateStr());
		user.setLoginTime(DateUtil.nowTimeStr());
		user.setSessionId(sessionId);
//			user.setDeviceUid(device);
		mapper.insert(user);
		setUserNewCache(user); 
		return user ; 
	}
	
	
	public static void setUserNewCache(User user) {
		setUserByName(user);
		setUserBySession(user);
	}
	public static void setUserByName(User user) {
		RedissonUtil.setAsync(CacheType.F_USER_NAME_ID.key(user.getUsername()), user, 30, TimeUnit.DAYS);
	}
	public static void setUserBySession(User user) {
		RedissonUtil.setAsync(CacheType.PASSPORT_SESSION.key(user.getSessionId()), user, 30, TimeUnit.DAYS);
	}
	public static String getSessionKey(String sessionId) {
		User user = RedissonUtil.get(CacheType.PASSPORT_SESSION.key(sessionId));
		return user == null? null : user.getSessionKey(); 
	}
	public static User getUserBySessionId(String sessionId) {
		return RedissonUtil.get(CacheType.PASSPORT_SESSION.key(sessionId));
	}
	public static User getUserByName(String username) {
		return RedissonUtil.get(CacheType.F_USER_NAME_ID.key(username));
	}
	public static String getServerId(long playerId) {
		return RedissonUtil.get(CacheType.PLAYER_SERVER_ID.key(playerId));
	}
	
	public static void removeUser(long sessionId) {
		RedissonUtil.deleteAsync(CacheType.PASSPORT_SESSION.key(sessionId)); 
	}

}
