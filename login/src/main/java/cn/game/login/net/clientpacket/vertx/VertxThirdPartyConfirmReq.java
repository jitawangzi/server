package cn.game.login.net.clientpacket.vertx;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.core.base.ServerContext;
import cn.game.core.cache.CacheType;
import cn.game.core.net.steam.SteamAPI;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.sdk.ChangYouSdk;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.User;
import cn.game.login.mapper.UserMapper;
import cn.game.login.net.clientpacket.vertx.wechat.WechatHelper;
import cn.game.login.util.PasswordUtil;
import cn.game.protocol.protobuf.Account.AccountChannelType;
import cn.game.protocol.protobuf.Account.AccountErrorCode;
import cn.game.protocol.protobuf.Account.AccountLogin;
import cn.game.protocol.protobuf.Account.AccountLoginResponse;
import cn.game.protocol.protobuf.Account.HttpResult;
import cn.game.util.Config;
import cn.game.util.DateUtil;
import cn.game.util.RedisUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@VertxRoute("/account/third_party_confirm")
public class VertxThirdPartyConfirmReq implements BaseVertxHandler {

	private static final Logger log = LoggerFactory.getLogger(VertxThirdPartyConfirmReq.class);
	private final Map<AccountChannelType, ThirdPartyAuthHandler> handlers = new EnumMap<>(AccountChannelType.class);
	private final UserMapper userMapper;

	public VertxThirdPartyConfirmReq() {
		this.userMapper = SpringContextLoader.getContext().getBean(UserMapper.class);
		initHandlers();
	}

	private void initHandlers() {
		handlers.put(AccountChannelType.OFFICIAL, new OfficialAuthHandler());
		handlers.put(AccountChannelType.WECHAT, new WechatAuthHandler());
		handlers.put(AccountChannelType.CHANGYOU, new ChangyouAuthHandler());
		handlers.put(AccountChannelType.NONE, new NoneAuthHandler());
	}

	@Override
	public void handle(RoutingContext context) {

//		JsonObject bodyAsJson = context.getBodyAsJson();
//		String platform = bodyAsJson.getString("platform");
//		String channel = bodyAsJson.getString("channel");
//		String token = bodyAsJson.getString("token");
		final AccountLogin request;
		try {
			request = AccountLogin.parseFrom(context.getBody().getBytes());
		} catch (InvalidProtocolBufferException e) {
			sendErrorResponse(context, "Invalid request format", AccountErrorCode.INVALID_REQUEST);
			return;
		}

		final AccountChannelType channel = request.getChannel();
		final String token = request.getToken();
		final HttpServerResponse response = context.response().putHeader("content-type", "text/json");

		log.info("VertxThirdPartyConfirmReq : Processing channel[{}] login request token[{}]", channel, token);

		ThirdPartyAuthHandler handler = handlers.get(channel);
		if (handler == null) {
			sendErrorResponse(context, "Unsupported channel", AccountErrorCode.CHANNEL_NOT_SUPPORT);
			return;
		}

		handler.handleAuth(context, token, (user, error) -> {
			if (error != null) {
				sendErrorResponse(context, error.getMessage(), error.getCode());
			} else {
				sendSuccessResponse(response, user);
			}
		});
	}

	private interface ThirdPartyAuthHandler {
		void handleAuth(RoutingContext context, String token, BiConsumer<User, AccountError> callback);
	}

	private class OfficialAuthHandler implements ThirdPartyAuthHandler {
		@Override
		public void handleAuth(RoutingContext context, String token, BiConsumer<User, AccountError> callback) {
			String[] credentials = token.split(" ");
			if (credentials.length == 0 || credentials.length > 2) {
				callback.accept(null, new AccountError("Invalid credentials", AccountErrorCode.INVALID_REQUEST));
				return;
			}

			String username = credentials[0];
			String password = credentials.length > 1 ? credentials[1] : "";

			RedisUtil.getAsync(CacheType.F_USER_NAME_ID.key(username)).onComplete((asyncResult, e) -> {
				if (e != null) {
					callback.accept(null, new AccountError("Cache load failed", AccountErrorCode.SYSTEM_ERROR));
					return;
				}

				User user = (User) asyncResult;
				if (user == null) {
					user = userMapper.selectByNameAndChannel(username, AccountChannelType.OFFICIAL.name().toLowerCase());
					if (user == null) {
						callback.accept(null, new AccountError("Account not exists", AccountErrorCode.ACCOUNT_NOT_EXIST));
						return;
					}
					cacheAndCreateSession(user);
				}

				if (!validatePassword(password, user.getPass())) {
					callback.accept(null, new AccountError("Password error", AccountErrorCode.PASSWORD_ERROR));
					return;
				}
				updateLoginInfo(user);
				callback.accept(user, null);
			});
		}
	}

	private class WechatAuthHandler implements ThirdPartyAuthHandler {
		@Override
		public void handleAuth(RoutingContext context, String token, BiConsumer<User, AccountError> callback) {
			String url = String.format(WechatHelper.WX_AUTH_URL_STRING, Config.wechat_appid, Config.wechat_secret, token);

			VxHolder.get(url, authResult -> {
				Integer errcode = authResult.getInteger("errcode");
				if (errcode != null && errcode != 0) {
					// -1 系统繁忙，此时请开发者稍候再试
//					0	请求成功	
//					40029	code 无效	
//					45011	频率限制，每个用户每分钟100次	
//					40226	高风险等级用户，小程序登录拦截 。风险等级详见用户安全解方案
					callback.accept(null, new AccountError("Wechat auth failed: " + errcode, AccountErrorCode.CHANNEL_CHECK_FAIL));
					return;
				}

				String openid = authResult.getString("openid");
				processThirdPartyAuth(context, openid, AccountChannelType.WECHAT, authResult.getString("session_key"),
						authResult.getString("unionid"), openid, callback);
			}, error -> handleChannelCommunicationError(error, callback));
		}
	}

	private class ChangyouAuthHandler implements ThirdPartyAuthHandler {
		@Override
		public void handleAuth(RoutingContext context, String token, BiConsumer<User, AccountError> callback) {
			JSONObject tokenObject = JSON.parseObject(token);
			String channelId = tokenObject.getString("channel_id");
			tokenObject.remove("channel_id");
			tokenObject.remove("opcode");

			ChangYouSdk.getInstance()
					.accountVerification(channelId, 0, tokenObject.toJSONString())
					.onSuccess(response -> processChangyouResponse(response, context, callback))
					.onFailure(error -> handleChannelCommunicationError(error, callback));
		}

		private void processChangyouResponse(String response, RoutingContext context, BiConsumer<User, AccountError> callback) {
			log.info("Changyou auth response: {}", response);
			JSONObject respObj = JSON.parseObject(response);
			if (!"200".equals(respObj.getString("state"))) {
				callback.accept(null, new AccountError("Changyou auth failed", AccountErrorCode.CHANNEL_CHECK_FAIL));
				return;
			}
			JSONObject data = respObj.getJSONObject("data");
			String dataStatus = data.getString("status");

			if (!dataStatus.equals("1")) {
				AccountErrorCode errorCode = AccountErrorCode.CHANNEL_CHECK_FAIL;
				if (dataStatus.equals("2")) {
					errorCode = AccountErrorCode.ACCOUNT_BANNED;
				}
				callback.accept(null, new AccountError("Changyou auth failed", errorCode));
				return;
			}

			processThirdPartyAuth(context, data.getString("userid"), AccountChannelType.CHANGYOU, "", data.getString("oid"),
					data.toJSONString(), callback);
		}
	}

	private class NoneAuthHandler implements ThirdPartyAuthHandler {
		@Override
		public void handleAuth(RoutingContext context, String token, BiConsumer<User, AccountError> callback) {
			SteamAPI.ISteamUserAuth.AuthenticateUserTicket(SteamAPI.KEY, SteamAPI.APP_ID, token,
					result -> processSteamResponse(result, callback), error -> handleChannelCommunicationError(error, callback));
		}

		private void processSteamResponse(JsonObject result, BiConsumer<User, AccountError> callback) {
			JsonObject params = result.getJsonObject("response").getJsonObject("params");
			String steamId = params.getString("steamid");
			processThirdPartyAuth(null, steamId, AccountChannelType.NONE, "", steamId, "", callback);
		}
	}

	private void processThirdPartyAuth(RoutingContext context, String username, AccountChannelType channel, String sessionKey,
			String unionId, String ext, BiConsumer<User, AccountError> callback) {
		RedisUtil.getAsync(CacheType.F_USER_NAME_ID.key(username)).onComplete((asyncResult, e) -> {
			User user = (User) asyncResult;
			if (user != null) {
				user.setExtInfo(ext);
				updateSessionIfNeeded(user, sessionKey);
				updateLoginInfo(user);
				callback.accept(user, null);
			} else {
				createOrLoadUser(username, channel, unionId, sessionKey, ext, callback);
			}
		});
	}

	private void createOrLoadUser(String username, AccountChannelType channel, String unionId, String sessionKey, String ext,
			BiConsumer<User, AccountError> callback) {
		VxHolder.vertx.executeBlocking(promise -> {
			User user = userMapper.selectByNameAndChannel(username, channel.name().toLowerCase());
			if (user == null) {
				user = UserHelper.createUser(username, "", channel.name().toLowerCase(), unionId, sessionKey);
			} else {
				cacheAndCreateSession(user);
			}
			user.setExtInfo(ext);
			updateLoginInfo(user);
			promise.complete(user);
		}, false)
				.onSuccess(user -> callback.accept((User) user, null))
				.onFailure(
						error -> callback.accept(null, new AccountError("Account creation failed", AccountErrorCode.ACCOUNT_CREATE_FAIL)));
	}

	private void updateSessionIfNeeded(User user, String newSessionKey) {
		if (!StringUtils.isEmpty(newSessionKey) && !StringUtils.equals(user.getSessionKey(), newSessionKey)) {
			UserHelper.removeUser(user.getSessionId());
			user.setSessionId(IdUtil.getId());
			user.setSessionKey(newSessionKey);
			UserHelper.setUserBySession(user);
		}
	}

	private void cacheAndCreateSession(User user) {
		long sessionId = IdUtil.getId();
		user.setSessionId(sessionId);
		UserHelper.setUserNewCache(user);
	}

	private void updateLoginInfo(User user) {
		user.setLoginDate(DateUtil.nowDateStr());
		user.setLoginTime(DateUtil.nowTimeStr());
		userMapper.updateByPrimaryKey(user);
	}

	private boolean validatePassword(String input, String stored) {
		return ServerContext.getInstance().getRunMode().isProduction() ? PasswordUtil.checkPassword(input, stored)
				: Objects.equals(input, stored);
	}

	private void sendErrorResponse(RoutingContext context, String message, AccountErrorCode code) {
		HttpResult result = HttpResult.newBuilder().setErrorMsg(message).setErrorCode(code).build();
		context.response().end(Buffer.buffer(AccountLoginResponse.newBuilder().setResult(result).build().toByteArray()));
	}

	private void sendSuccessResponse(HttpServerResponse response, User user) {
		AccountLoginResponse responseProto = AccountLoginResponse.newBuilder()
				.setPassportSessionId(String.valueOf(user.getSessionId()))
				.setUserId(String.valueOf(user.getId()))
				.setExt(user.getExtInfo() == null ? "" : user.getExtInfo())
				.build();
		response.end(Buffer.buffer(responseProto.toByteArray()));
	}

	private void handleChannelCommunicationError(Throwable error, BiConsumer<User, AccountError> callback) {
		log.error("Channel communication error", error);
		callback.accept(null, new AccountError("Channel communication error", AccountErrorCode.CHANNEL_CHECK_FAIL));
	}

	private static class AccountError {
		final String message;
		final AccountErrorCode code;

		AccountError(String message, AccountErrorCode code) {
			this.message = message;
			this.code = code;
		}

		String getMessage() {
			return message;
		}

		AccountErrorCode getCode() {
			return code;
		}
	}
}