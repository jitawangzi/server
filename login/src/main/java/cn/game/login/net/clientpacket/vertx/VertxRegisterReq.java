package cn.game.login.net.clientpacket.vertx;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.game.core.cache.CacheType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.IdUtil;
import cn.game.login.cache.entity.User;
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

/**    
 * 这里账号没有考虑分表情况，或者先用redis把所有账号都存上并且持久化
 * 2024年3月26日 下午5:53:10
 * @author SYQ
 */
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
			VxHolder.vertx.executeBlocking(r -> {
				try {
					UserHelper.createUser(account, pwd, "official", account, "");
					response.end(Buffer.buffer(resp.build().toByteArray()));
				} catch (Exception e) {
					e.printStackTrace();
					HttpResult httpResult = HttpResult.newBuilder().setErrorMsg("账号已经存在")
							.setErrorCode(AccountErrorCode.ACCOUNT_EXIST).build();
					response.end(Buffer.buffer(resp.setResult(httpResult).build().toByteArray()));
				
				}
			}) ;
			
		});
	}
}
