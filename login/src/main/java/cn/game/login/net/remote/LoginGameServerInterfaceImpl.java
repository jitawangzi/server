package cn.game.login.net.remote;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;

import cn.game.core.cache.CacheType;
import cn.game.core.net.remote.LoginGameServerInterface;
import cn.game.login.cache.CacheManager;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.UserMapper;
import cn.game.util.DateUtil;
import cn.game.util.SpringContextLoader;
import io.reactivex.rxjava3.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.redis.client.Response;

public class LoginGameServerInterfaceImpl implements LoginGameServerInterface {

	/**  */
	private static final Logger log = LoggerFactory.getLogger(LoginGameServerInterfaceImpl.class);
	@Autowired
	private UserMapper userMapper;

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public long getUid(String passportSessionId) {
		log.info("get uid");

		String userString = CacheManager.getInstance().loadSync(CacheType.PASSPORT_SESSION, passportSessionId);
		if (!StringUtils.isEmpty(userString)) {
			User user = JSON.parseObject(userString, User.class);
			return user.getId();
		}
		return 0;
	}
	@Override
	public Future<Long> getUid2(String passportSessionId) {
		log.info("get uid , passportSessionId[{}]", passportSessionId);

		Future<@Nullable Response> future = CacheManager.getInstance().loadAsync(CacheType.PASSPORT_SESSION, passportSessionId);
		return future.map(t -> {
			User user = JSON.parseObject(t.toString(), User.class);
			return user.getId();
		});
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
	public void addUserServer(String serverId, long passportSessionId, long uid) {

		UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class);
		User user = mapper.selectByPrimaryKey(uid);
		user.setServers(user.getServers() == null ? serverId : user.getServers() + "," + serverId);
		// HashMap<String, String> map = new HashMap<>() ;
		// map.put("id", user.getId()+"") ;
		// map.put("servers", user.getServers()) ;
		User upUser = new User();
		upUser.setId(user.getId());
		upUser.setServers(user.getServers());
		mapper.updateByPrimaryKeySelective(upUser);
	}

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
		user.setUserType((byte) 3);
		user.setUsername(name);
		user.setChannelCode("A");
		user.setPass("");
		user.setCreateDate(DateUtil.nowDateStr());
		user.setCreateTime(DateUtil.nowTimeStr());
		user.setIsGm(0);
		user.setLoginDate(DateUtil.nowDateStr());
		user.setLoginTime(DateUtil.nowTimeStr());
		user.setDeviceUid("");
		mapper.insert(user);

		return user.getId();
	}
}
