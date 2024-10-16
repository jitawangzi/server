package cn.game.login.net.clientpacket.vertx;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.core.cache.CacheType;
import cn.game.core.net.steam.SteamAPI;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.UserMapper;
import cn.game.login.net.clientpacket.vertx.wechat.WechatHelper;
import cn.game.protocol.protobuf.Account.AccountChannelType;
import cn.game.protocol.protobuf.Account.AccountErrorCode;
import cn.game.protocol.protobuf.Account.AccountLogin;
import cn.game.protocol.protobuf.Account.AccountLoginResponse;
import cn.game.protocol.protobuf.Account.HttpResult;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class VertxThirdPartyConfirmReq implements Handler<RoutingContext> {

	protected static final Logger log = LoggerFactory.getLogger(VertxThirdPartyConfirmReq.class);

	@Override
	public void handle(RoutingContext context) {

//		JsonObject bodyAsJson = context.getBodyAsJson();
//		String platform = bodyAsJson.getString("platform");
//		String channel = bodyAsJson.getString("channel");
//		String token = bodyAsJson.getString("token");
//		
		byte[] bytes = context.getBody().getBytes();
		AccountLogin from = null;
		try {
			from = AccountLogin.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		AccountChannelType channel = from.getChannel();
		String token = from.getToken();
		String platform = "";

		log.info("VertxThirdPartyConfirmReq platform[{}]channel[{}]token[{}]", platform, channel, token);
		HttpServerResponse response = context.response().putHeader("content-type", "text/json");

//		ThirdPartyConfirmResp resp = new ThirdPartyConfirmResp();
		AccountLoginResponse.Builder resp = AccountLoginResponse.newBuilder();

		switch (channel) {
		case OFFICIAL: {
			String[] split = token.split(" ");
			String username = split[0];
			String pwd = split[1];

			RFuture<User> future = RedisUtil.getAsync(CacheType.F_USER_NAME_ID.key(username));
			future.onComplete((v, throwable) -> {
				if (throwable != null) {
					log.error("load cache error, {} {} ", CacheType.F_USER_NAME_ID, username);
				} else {
					VxHolder.vertx.executeBlocking(fut -> {
//							String ret = r.result() == null ? null : r.result().toString();
						UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class);
						User user = v;
						if (user == null) {
							user = mapper.selectByNameAndChannel(username, channel.name().toLowerCase());
							if (user == null) {
								HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号不存在")
										.setErrorCode(AccountErrorCode.ACCOUNT_NOT_EXIST).build();
								response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
								return;
							}
							// 创建session
							long sessionId = IdUtil.getId();
							user.setSessionId(sessionId);
							UserHelper.setUserNewCache(user); 
						}

						if (pwd != null && !pwd.equals(user.getPass())) {
							HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("密码错误")
									.setErrorCode(AccountErrorCode.PASSWORD_ERROR).build();
							response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
							return;
						}

						// 更新登录时间
						user.setLoginDate(DateUtil.nowDateStr());
						user.setLoginTime(DateUtil.nowTimeStr());
						mapper.updateByPrimaryKey(user);

						byte[] byteArray = resp.setPassportSessionId(user.getSessionId() + "").setUserId(user.getId() + "")
								.build().toByteArray();
						Buffer data = Buffer.buffer(byteArray);
						response.end(data);
						return;
//							resp.setPassport_session_id(sessionId + "");
//							resp.setUserId(user.getId());
//							response.end(resp.toJSON().toString());
					}, false);
				}
			});
			break;
		}
		case WECHAT: {
			String code = token;
//			String grant_type = "authorization_code";
			String url = String.format(WechatHelper.WX_AUTH_URL_STRING, Config.wechat_appid, Config.wechat_secret, code);
			VxHolder.get(url, r -> {
				String errorcodestring = r.getString("errcode");
				int errcode = Integer.parseInt(errorcodestring == null ? "0" : errorcodestring);
				String errmsg = r.getString("errmsg");
				log.debug("wechat login errcode {} ", errcode);
				log.debug("wechat login errmsg {}", errmsg);
				if (errcode == 0) { // 微信账号校验成功，执行后续本地账号逻辑
					String openid = r.getString("openid");
					resp.setExt(openid);
					String session_key = r.getString("session_key");
					String unionid = r.getString("unionid") == null ? openid : r.getString("unionid");
					String username = openid;
					RFuture<User> future = RedisUtil.getAsync(CacheType.F_USER_NAME_ID.key(username));
					future.onComplete((v, throwable) -> {
						if (throwable != null) {
							log.error("load cache error, {} {} ", CacheType.F_USER_NAME_ID, username);
						} else {
							VxHolder.vertx.executeBlocking(fut -> {
								UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class);
								User user = v;
								if (user != null) {
									if (!session_key.equals(user.getSessionKey())) {
										user.setSessionKey(session_key);
										UserHelper.removeUser(user.getSessionId());
										user.setSessionId(IdUtil.getId());
										UserHelper.setUserBySession(user);
									}
									user.setLoginDate(DateUtil.nowDateStr());
									user.setLoginTime(DateUtil.nowTimeStr());
									mapper.updateByPrimaryKey(user);
								} else {
									// 从数据库中查询，如果没有账号需要直接创建
									user = mapper.selectByNameAndChannel(username, channel.name().toLowerCase());
									if (user == null) {
										user = UserHelper.createUser(username, "",
												AccountChannelType.WECHAT.name().toLowerCase(), unionid, session_key);
										// 先不用了。
//										resp.setIsNew(true);
									} else {
										user.setSessionId(IdUtil.getId());
										UserHelper.setUserNewCache(user);
										// 更新登录时间
										user.setLoginDate(DateUtil.nowDateStr());
										user.setLoginTime(DateUtil.nowTimeStr());
										user.setSessionKey(session_key);
										mapper.updateByPrimaryKey(user);
									}
								}
								byte[] byteArray = resp.setPassportSessionId(user.getSessionId() + "")
										.setUserId(user.getId() + "").build().toByteArray();
								Buffer data = Buffer.buffer(byteArray);
								response.end(data);
								return;
							}, false).onFailure(e -> {
								log.error("", e);
								HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("可能是账号创建失败")
										.setErrorCode(AccountErrorCode.ACCOUNT_CREATE_FAIL).build();
								response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
							});
						}
					});
				} else {
					log.warn("wechat login fail , token {} ,errcode {} ", token, errcode);
					// -1 系统繁忙，此时请开发者稍候再试
//					0	请求成功	
//					40029	code 无效	
//					45011	频率限制，每个用户每分钟100次	
//					40226	高风险等级用户，小程序登录拦截 。风险等级详见用户安全解方案

					HttpResult httpResult = HttpResult.newBuilder().setErrorMsg(errcode + " : " + errmsg)
							.setErrorCode(AccountErrorCode.CHANNEL_CHECK_FAIL).build();
					response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
					return;
				}
			}, e -> {
				HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("渠道通信错误")
						.setErrorCode(AccountErrorCode.CHANNEL_CHECK_FAIL).build();
				response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
				log.error("", e);
				return;
			});
			break;
		}
		case NONE: {
			SteamAPI.ISteamUserAuth.AuthenticateUserTicket(SteamAPI.KEY, SteamAPI.APP_ID, token, rr -> {

				log.info(rr.toString());
				JsonObject jsonObject = rr.getJsonObject("response");
				JsonObject paramsObject = jsonObject.getJsonObject("params");
				String username = paramsObject == null ? null : paramsObject.getString("steamid");
				if (StringUtils.isEmpty(username)) {
//						resp.setRet(-1, "steam 校验失败 " + rr);
//						response.end(resp.toJSON().toString());
					return;
				}
				RFuture<Object> future = RedisUtil.getAsync(CacheType.F_USER_NAME_ID.key(username));

				future.onComplete((v, throwable) -> {
					if (throwable != null) {
						log.error("load cache error, {} {} ", CacheType.F_USER_NAME_ID, username);
					} else {

						VxHolder.vertx.executeBlocking(fut -> {
							String ret = (String) v;
							UserMapper mapper = SpringContextLoader.getContext().getBean(UserMapper.class);
							User user = null;
							if (!StringUtils.isEmpty(ret)) {
								user = JSON.parseObject(ret, User.class);
							} else {
								try {
									user = mapper.selectByNameAndChannel(username, channel.name().toLowerCase());
								} catch (Exception e1) {
									e1.printStackTrace();
								}
								if (user == null) {
									// 创建账号
									user = new User();
									user.setUserType((byte) 1);
									user.setUsername(username);
									user.setChannelLabel(channel.name().toLowerCase());
//										user.setChannelCode(channel);
//										user.setPass(pwd);
									user.setCreateDate(DateUtil.nowDateStr());
									user.setCreateTime(DateUtil.nowTimeStr());
									user.setIsGm(false);
									user.setLoginDate(DateUtil.nowDateStr());
									user.setLoginTime(DateUtil.nowTimeStr());
									mapper.insert(user);
								} else {
									// 更新登录时间
									user.setLoginDate(DateUtil.nowDateStr());
									user.setLoginTime(DateUtil.nowTimeStr());
									mapper.updateByPrimaryKey(user);
								}
								RedisUtil.setAsync(CacheType.F_USER_NAME_ID.key(username), JSON.toJSONString(user));
							}

							// 创建session
							long sessionId = IdUtil.getId();
							user.setSessionId(sessionId);
							UserHelper.setUserBySession(user);
							response.end(Buffer.buffer(resp.setPassportSessionId(sessionId + "")
									.setUserId(user.getId() + "").build().toByteArray()));
							return;

						}, false);

					}
				});
			}, e -> {
//					resp.setRet(-1, "steam 校验失败");
//					response.end(resp.toJSON().toString());
				return;
			});

			break;
		}
		default:

			HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("不支持的渠道")
					.setErrorCode(AccountErrorCode.CHANNEL_NOT_SUPPORT).build();
			response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
			break;
		}

	}
}
