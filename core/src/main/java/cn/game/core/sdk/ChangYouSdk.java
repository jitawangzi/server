package cn.game.core.sdk;

import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.Config;
import cn.game.util.GameUtil;
import cn.game.util.Rnd;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class ChangYouSdk {

	// 单例模式
	private static ChangYouSdk instance = new ChangYouSdk();

	private final Vertx vertx = VxHolder.vertx;
	private final WebClient webClient = WebClient.create(vertx, new WebClientOptions());;

	private ChangYouSdk() {
	}

	public static ChangYouSdk getInstance() {
		return instance;
	}

	/**
	 * 账号验证接口：opcode: 10001
	 *
	 * @param data 请求 data 字段（JSON 字符串）
	 * @return Future 包含异步结果
	 */
	public Future<String> accountVerification(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "10001", tag, data);
	}

	/**
	 * 订单验证接口：opcode: 5003
	 *
	 * @param data 请求 data 字段（JSON 字符串）
	 * @return Future 包含异步结果
	 */
	public Future<String> orderVerification(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "5003", tag, data);
	}

	/**
	 * 订单更新接口：opcode: 5004
	 *
	 * @param data 请求 data 字段（JSON 字符串）
	 * @return Future 包含异步结果
	 */
	public Future<String> orderUpdate(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "5004", tag, data);
	}

	/**
	 * 礼包码接口：opcode: 7000
	 *
	 * @param data 请求 data 字段（JSON 字符串）
	 * @return Future 包含异步结果
	 */
	public Future<String> giftCode(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "7000", tag, data);
	}

	/**
	 * 通用请求发送方法
	 *
	 * 根据传入的 opcode、tag 以及 data 参数构造签名，
	 * 然后构造 urlencoded 格式的请求体，并使用 Vert.x WebClient 发送请求，
	 * 异步返回响应结果。
	 *
	 * @param opcode 接口编码
	 * @param tag    tag 值
	 * @param data   请求 data（JSON 字符串）
	 * @return Future 包含异步返回结果
	 */
	private Future<String> sendBillingRequest(String channelId, String opcode, int tag, String data) {
		Promise<String> promise = Promise.promise();
        try {
			if (tag == 0) {
				tag = Rnd.nextInt(99999999);
			}
            // 构造签名字符串：opcode + data + appkey + appsecret + tag + channelId
			String signStr = opcode + data + Config.CHANGYOU_SDK_APP_KEY + Config.CHANGYOU_SDK_APPSECRET + tag + channelId;
			// 计算 MD5 值并取出下标 8 到 24 的子串（16 个字符）
			String md5Hex = GameUtil.md5Hex(signStr);
            String sign = md5Hex.substring(8, 24);

			// 构造请求体（urlencoded 格式），只有一个参数 data
//            String body = "data=" + URLEncoder.encode(data, StandardCharsets.UTF_8.toString());

			// 使用 WebClient 发起 POST 请求
			String URL_BILLING = ServerContext.getInstance().getRunMode().isProduction() ? Config.CHANGYOU_SDK_URL_BILLING_PRODUCTION
					: Config.CHANGYOU_SDK_URL_BILLING_TEST;

			webClient.postAbs(URL_BILLING)
					.putHeader("appkey", Config.CHANGYOU_SDK_APP_KEY)
					.putHeader("tag", tag + "")
					.putHeader("opcode", opcode)
					.putHeader("channelId", channelId)
					.putHeader("sign", sign)
					.putHeader("Content-Type", "application/x-www-form-urlencoded")
					.sendBuffer(Buffer.buffer(data), ar -> {
						if (ar.succeeded()) {
							promise.complete(ar.result().bodyAsString());
						} else {
							promise.fail(ar.cause());
						}
					});
        } catch (Exception e) {
			promise.fail(e);
        }
		return promise.future();
    }
}