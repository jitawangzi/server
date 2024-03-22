package cn.game.login.net.clientpacket;

import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.PassportSession;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.UserMapper;
import cn.game.util.DateUtil;
import cn.game.util.HttpUtil;
import cn.game.util.SpringContextLoader;


public class Sdk1LoginConfirmReq {

	private String app; 			//我们在易接上的产品ID
	private String channelCode;		//渠道编号
	private String channelUserId;	//渠道用户id
	private String channelUsername;	//渠道用户名
	private String channelToken;	//客户端登录渠道后返回的token
	private String deviceUid;       //客户端手机设备唯一标识符
	
//	@Override
	protected void readImpl() {		
//		this.app = params.get("app");
//		this.channelCode = params.get("channelCode");
//		this.channelUserId = params.get("channelUserId");
//		this.channelUsername = params.get("channelUsername");
//		this.channelToken = params.get("channelToken");
//		this.deviceUid = params.get("deviceUid");
	}

	private static class Sdk1LoginConfirmResp {
	}

//	@Override
	protected void runImpl() {
		
		Sdk1LoginConfirmResp resp = new Sdk1LoginConfirmResp() ; 

		//访问易接服务器，验证用户
		String result = this.httpConfirm();
		if(result == null || !result.equals("0")){
//			resp.setRet(-100, "账号验证失败");
//			sendPacket(resp) ; 
			return  ; 
		}
		
		
		//cache查询用户
		
		UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class) ; 
		User user = mapper.selectByNameAndChannel(channelUsername, "");
		
		//用户不存在，创建新用户
		if(user == null){			
			user = new User();
			user.setUserType((byte)3) ; 
			user.setUsername(channelUsername);
			user.setChannelCode(channelCode);
			user.setCreateDate(DateUtil.nowDateStr());
			user.setCreateTime(DateUtil.nowTimeStr());
			user.setIsGm(0) ; 
			user.setDeviceUid(deviceUid) ; 
			mapper.insert(user) ;
			//放入缓存
			//userCache.set(u);
		}
		
		//放入缓存			
//		userCache.set(u);			
	
		
		//更新登录时间
		user.setLoginDate(DateUtil.nowDateStr());
		user.setLoginTime(DateUtil.nowTimeStr());
//		userCache.setEntity(u);
//		dao.update(u);
		mapper.updateByPrimaryKey(user); 
		
		//创建session
//		PassportSessionCache c = CacheFactory.getCache(PassportSessionCache.class);
		PassportSession session = new PassportSession() ; 
		session.setUid(user.getId()) ; 
		session.setSessionId(IdUtil.getId());
//		CacheManager.getInstance().put(user, CacheType.PASSPORT_SESSION, session.getSessionId()) ; 
//		CacheManager.getInstance().addUser(session.getSessionId(), user);
		
	}
	/**
	 * 
	 * @param req
	 * @return true验证成功
	 */
	private String httpConfirm(){
		try{
			String sess = this.channelToken;
			StringBuilder builder = new StringBuilder();
			builder.append("http://sync.1sdk.cn/login/check.html?")
				   .append("app=").append(this.app)
				   .append("&sdk=").append(channelCode)
				   .append("&uin=").append(channelUserId)
				   .append("&sess=").append(sess);			
//			log.debug("易接验证URL：{}", builder.toString());
			return HttpUtil.get(builder.toString());
			
		}catch(Exception e){
			throw new RuntimeException("http访问易接, 验证账号失败");
		}
	}
}
