package cn.game.login.net.clientpacket.vertx;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.core.cache.CacheType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.protocol.protobuf.Account.AccountErrorCode;
import cn.game.protocol.protobuf.Account.AccountRegister;
import cn.game.protocol.protobuf.Account.AccountRegisterResponse;
import cn.game.protocol.protobuf.Account.HttpResult;
import cn.game.util.RedisUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**    
 * 这里账号没有考虑分表情况，或者先用redis把所有账号都存上并且持久化
 * 2024年3月26日 下午5:53:10
 * @author SYQ
 */
@VertxRoute("/account/register")
public class VertxRegisterReq implements BaseVertxHandler {
	protected static final Logger log = LoggerFactory.getLogger(VertxRegisterReq.class);

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
			HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号不能为空").setErrorCode(AccountErrorCode.ACCOUNT_NOT_EXIST).build();
			response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
			return;
		}

		RedisUtil.getAndRunAsync(CacheType.F_USER_NAME_ID.key(account), ret -> {
			if (ret != null) {
				HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号已经存在").setErrorCode(AccountErrorCode.ACCOUNT_EXIST).build();
				response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
				return;
			}
			VxHolder.vertx.executeBlocking(promise -> {
				try {
					UserHelper.createUser(account, pwd, "official", account, "");
					promise.complete();
				} catch (Exception e) {
					// 捕获异常并传递到主线程
					promise.fail(e);
					log.error("createUser error ", e);
					HttpResult httpResult = HttpResult.newBuilder()
							.setErrorMsg("账号已经存在")
							.setErrorCode(AccountErrorCode.ACCOUNT_EXIST)
							.build();
					response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));

				}
			}, false).onComplete(ar -> {
				if (ar.succeeded()) {
					response.end(Buffer.buffer(resp.build().toByteArray()));
				} else {
					log.error("createUser error ", ar.cause());
					HttpResult httpResult = HttpResult.newBuilder()
							.setErrorMsg("账号已经存在")
							.setErrorCode(AccountErrorCode.ACCOUNT_EXIST)
							.build();
					response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
				}
				context.fail(ar.cause());
			});
		});
	}

//	@Override
//	public String getPath() {
//		return "/account/register";
//	}
}
