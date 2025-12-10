package cn.game.login.net.remote;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.game.core.id.IdUtil;
import cn.game.login.cache.entity.User;
import cn.game.login.cache.entity.UserServer;
import cn.game.login.mapper.UserMapper;
import cn.game.login.mapper.UserServerMapper;
import cn.game.util.DateUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Future;

@Component
public class LoginServerImpl implements LoginServerInterface {
	/**  */
	private static final Logger log = LoggerFactory.getLogger(LoginServerImpl.class);
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserServerMapper userServerMapper;

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public long getUid(String passportSessionId) {
		log.info("get uid");

//		String userString = CacheManager.getInstance().loadSync(CacheType.PASSPORT_SESSION, passportSessionId);
//		if (!StringUtils.isEmpty(userString)) {
//			User user = JSON.parseObject(userString, User.class);
//			return user.getId();
//		}
		return 2;
	}
	@Override
	public Future<Long> getUid2(String passportSessionId) {
		log.info("get uid , passportSessionId[{}]", passportSessionId);
		return Future.succeededFuture(666L);
	}

//	@Override
//	public User getUser(long uid) {
//		// System.out.println("基本类型的getUser方法被调用");
//		return userMapper.selectByPrimaryKey(uid);
//	}
//
//	@Override
//	public User getUser(Long uid) {
//		// System.out.println("对象类型的getUser方法被调用");
//		return userMapper.selectByPrimaryKey(uid);
//	}

	@Override
	public long getUidByName(String name) {
		UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class);

//		User userByName = UserManager.getInstance().getUserByName(name);
//		if (userByName == null) {
//			userByName = mapper.selectByName(name);
//		}
//		if (userByName != null) {
//			return userByName.getId();
//		}
		User user = new User();
		user.setUserType((byte) 1);
		user.setUsername(name);
		user.setChannelCode("A");
		user.setPass("");
		user.setCreateDate(DateUtil.nowDateStr());
		user.setCreateTime(DateUtil.nowTimeStr());
		user.setIsGm(false);
		user.setLoginDate(DateUtil.nowDateStr());
		user.setLoginTime(DateUtil.nowTimeStr());
		user.setDeviceUid("");
		mapper.insert(user);

		return user.getId();
	}

	@Override
	public Future<Void> updateUserServer(String serverId, long userId,long playerId,String name,int level) {
		UserServer userServer = new UserServer(); 
		userServer.setId(IdUtil.getId());
		userServer.setPlayerId(playerId);
		userServer.setServerId(serverId);
		userServer.setUserId(userId);
		userServer.setPlayerName(name);
		userServer.setPlayerLevel(level);
		userServer.setUpdatedAt( new Date());
		
		userServerMapper.insertOrUpdate(userServer); 
		
		return Future.succeededFuture();
	}
}
