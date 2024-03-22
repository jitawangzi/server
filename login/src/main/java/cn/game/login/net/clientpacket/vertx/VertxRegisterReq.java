package cn.game.login.net.clientpacket.vertx;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.core.cache.CacheType;
import cn.game.protocol.protobuf.Account.AccountErrorCode;
import cn.game.protocol.protobuf.Account.AccountRegister;
import cn.game.protocol.protobuf.Account.AccountRegisterResponse;
import cn.game.protocol.protobuf.Account.HttpResult;
import cn.game.util.RedissonUtil;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class VertxRegisterReq implements Handler<RoutingContext> {

	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest request = context.request(); 
		byte[] bytes = context.getBody().getBytes();
		AccountRegister from = null;
		try {
			from = AccountRegister.parseFrom(bytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		String account = from.getAccount();
		String pwd = from.getPwd();
//		System.err.println(account);
//		System.err.println(pwd);
//		JsonObject bodyAsJson = context.getBodyAsJson();
//		String account = bodyAsJson.getString("account");
//		String pwd = bodyAsJson.getString("pwd");
		HttpServerResponse response = context.response().putHeader("content-type", "application/octet-stream");
		AccountRegisterResponse.Builder resp = AccountRegisterResponse.newBuilder();
//		ServerListResp resp = new ServerListResp();

		if (StringUtils.isEmpty(account)) {
			HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号不能为空")
					.setErrorCode(AccountErrorCode.ACCOUNT_NOT_EXIST).build();
			response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
			return;
		}

		RedissonUtil.getAndRunAsync(CacheType.F_USER_NAME_ID.key(account), ret -> {
			if (!StringUtils.isEmpty((String) ret)) {
				HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号已经存在")
						.setErrorCode(AccountErrorCode.ACCOUNT_EXIST).build();
				response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
				return;
			}
			Future<Object> user = UserHelper.createUser(account, pwd, "official", account, "");
			user.onSuccess(r -> {
				response.end(Buffer.buffer(resp.build().toByteArray()));
			}).onFailure(e -> {
				HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号已经存在")
						.setErrorCode(AccountErrorCode.ACCOUNT_EXIST).build();
				response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
			});
		});
	}
}
