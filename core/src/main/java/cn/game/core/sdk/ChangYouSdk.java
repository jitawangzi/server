package cn.game.core.sdk;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.Config;
import cn.game.util.GameUtil;
import cn.game.util.JsonUtil;
import cn.game.util.Rnd;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * ChangYouSdk 用于集成畅游相关接口，包括：
 * 1. 登陆Token验证（opcode: 10001）
 * 2. 订单验证（opcode: 5003）
 * 3. 订单状态更新（opcode: 5004）
 * 4. 礼包码接口（opcode: 7000）
 *
 * 全局请求约定：
 * - HTTP 头中需包含 appkey、sign、tag、opcode、channelId。
 * - POST 参数为 URL Encoded 格式，参数名固定为 data，其值为 JSON 格式字符串，
 *   例如：{"validateInfo":"..."}、{"receipt":"..."}、{"orders":[{"orderedId":"..."}]}、{"userid":"...", "code":"..."}
 *
 * 签名算法（附录一）：sign = MD5(opcode+data+appkey+appsecret+tag+channelId)，并取下标 8 到 24 的子串（16字符）。
 */
public class ChangYouSdk {

	// 单例模式
	private static ChangYouSdk instance = new ChangYouSdk();

	private final Vertx vertx = VxHolder.vertx;
	private final WebClient webClient = WebClient.create(vertx, new WebClientOptions());

	private ChangYouSdk() {
	}

	public static ChangYouSdk getInstance() {
		return instance;
	}

	/**
	 * 账号验证接口：opcode: 10001, 固定
	 *
	 * 2.1 登陆Token验证说明：
	 * - 接口描述：集成畅游移动SDK登陆成功后，客户端通过游戏服务器向Billing服务器验证登陆token的有效性。
	 * - 接口URL：测试环境（http://tmobilebilling.changyou.com/billing）、正式环境（http://mobilebilling.changyou.com/billing）
	 * - 请求方式：HTTP POST（Content-Type：application/x-www-form-urlencoded）
	 * - 请求参数：
	 *      * HTTP Header：
	 *          - appkey：应用唯一标识，由 Config.CHANGYOU_SDK_APP_KEY 提供
	 *          - sign：数字签名，计算方法见附录一
	 *          - tag：事务唯一标识，int类型随机数（不大于8位）
	 *          - opcode：固定为 "10001"
	 *          - channelId：渠道标识，由客户端传入
	 *      * POST 参数：data 参数为 JSON 协议，如 {"validateInfo":"..."}
	 *
	 * @param channelId 渠道标识，由客户端传入
	 * @param tag       事务唯一标识，如果为 0 则自动生成随机数
	 * @param data      请求 data 字段（JSON 字符串），例如 {"validateInfo":"..."}
	 * @return Future 包含异步结果
	 */
	public Future<String> accountVerification(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "10001", tag, data);
	}

	/**
	 * 订单验证接口：opcode: 5003, 固定
	 *
	 * 2.2 订单验证说明：
	 * - 接口描述：游戏服务器收到Billing的支付通知后，需要向Billing服务器请求验证支付订单后才可对玩家进行发放元宝操作。
	 * - 请求参数中，POST参数 data 用于传递 receipt 参数，格式示例：{"receipt":"[base64编码后的订单信息]"}
	 *
	 * @param channelId 渠道标识，由客户端传入
	 * @param tag       事务唯一标识，如果为 0 则自动生成随机数
	 * @param data      请求 data 字段（JSON 字符串），例如 {"receipt":"..."}
	 * @return Future 包含异步结果
	 */
	public Future<String> orderVerification(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "5003", tag, data);
	}

	/**
	 * 订单更新接口：opcode: 5004, 固定
	 *
	 * 2.2.3 订单状态更新说明：
	 * - 接口描述：游戏服务器订单验证成功后，调用Billing服务器接口改变已加钱订单状态。
	 * - 请求参数中，POST参数 data 用于传递订单号，格式示例：
	 *     {"orders":[{"orderedId":"订单编号"}]}
	 *
	 * @param channelId 渠道标识，由客户端传入
	 * @param tag       事务唯一标识，如果为 0 则自动生成随机数
	 * @param data      请求 data 字段（JSON 字符串），例如 {"orders":[{"orderedId":"..."}]}
	 * @return Future 包含异步结果
	 */
	public Future<String> orderUpdate(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "5004", tag, data);
	}

	/**
	 * 礼包码接口：opcode: 7000, 固定
	 *
	 * 2.3 礼包码接口说明：
	 * - 接口描述：通过Billing后台生成激活码后，游戏请求Billing获取激活码对应的物品信息。
	 * - 请求参数中，POST参数 data 用于传递礼包码相关信息，格式示例：
	 *     {"userid":"用户唯一标识", "code":"礼包码", "use":0, ...}
	 *
	 * @param channelId 渠道标识，由客户端传入
	 * @param tag       事务唯一标识，如果为 0 则自动生成随机数
	 * @param data      请求 data 字段（JSON 字符串），例如 {"userid":"...","code":"...","use":0,...}
	 * @return Future 包含异步结果
	 */
	public Future<String> giftCode(String channelId, int tag, String data) {
		return sendBillingRequest(channelId, "7000", tag, data);
	}

	/**
	 * 解析 billing 推送的支付通知请求。
	 *
	 * 注意：如果研发侧服务器端是自行处理 HTTP 协议，
	 * 则 POST 请求的 body 格式为：receipt=$receipt&sign=$sign&pushInfo=$pushInfo
	 * 本方法假设请求参数均为 key=value 格式，并使用 "&" 分隔。
	 *
	 * 解析步骤：
	 * 1. 解析出 receipt、pushInfo 和 sign 参数（均进行 URL 解码）。
	 * 2. 根据 "pushInfo=<pushInfo>&receipt=<receipt>" 拼接字符串并计算 MD5 签名，
	 *    与传入的 sign 参数比对，签名不符则抛出异常。
	 * 3. 对 receipt 进行 Base64 解码还原为原始 JSON 字符串，
	 *    然后将该 JSON 解析为 PaymentNotification 对象。
	 *
	 * @param bodyStr POST 请求体字符串，例如 "receipt=xxx&sign=xxx&pushInfo=xxx"
	 * @return PaymentNotification 对象，包含支付通知中订单及相关信息
	 * @throws Exception 若解析失败或签名校验不通过时抛出异常
	 */
	public PaymentNotification parsePaymentNotification(String bodyStr) throws Exception {
		// 按 & 分隔参数，要求格式为 key=value&key=value
		String[] pairs = bodyStr.split("&");
		String receiptParam = null;
		String pushInfoParam = null;
		String signParam = null;

		for (String pair : pairs) {
			// 使用 split("=", 2) 确保 value 部分中可能存在 "=" 号时不会被拆分多次
			String[] kv = pair.split("=", 2);
			if (kv.length == 2) {
				String key = kv[0];
				String value = kv[1];
				if ("receipt".equals(key)) {
					// 如果接收到的参数未经过解码，则使用 URLDecoder 解码
					receiptParam = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
				} else if ("pushInfo".equals(key)) {
					pushInfoParam = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
				} else if ("sign".equals(key)) {
					signParam = value;
				}
			}
		}
		if (receiptParam == null || pushInfoParam == null || signParam == null) {
			throw new Exception("支付通知参数不完整，缺少 receipt、pushInfo 或 sign");
		}

		// 签名校验：按照文档要求，对参数进行排序并拼接为 "pushInfo=<pushInfo>&receipt=<receipt>"
		String signStr = "pushInfo=" + pushInfoParam + "&receipt=" + receiptParam;
		String computedSign = GameUtil.md5Hex(signStr);
		if (!computedSign.equals(signParam)) {
			throw new Exception("支付通知签名校验失败");
		}

		// 对 receipt 进行 Base64 解码还原 JSON 字符串
		byte[] decodedBytes = java.util.Base64.getDecoder().decode(receiptParam);
		String receiptJson = new String(decodedBytes, StandardCharsets.UTF_8);

		// 解析 receiptJson 为 PaymentNotification 对象，
		PaymentNotification notification = JsonUtil.parseObject(receiptJson, PaymentNotification.class);
		// 可将 pushInfo 也保存到 notification 中，便于后续使用
		notification.setPushInfo(pushInfoParam);
		return notification;
	}

	/**
	 * 通用请求发送方法
	 *
	 * 根据传入的 opcode、tag 以及 data 参数构造签名，
	 * 并构造 urlencoded 格式的请求体（POST 参数为 data），
	 * 然后使用 Vert.x WebClient 发送 HTTP POST 请求，
	 * 异步返回响应结果。
	 *
	 * 全局约定：
	 *  - HTTP Header 中必须包含：appkey、sign、tag、opcode、channelId
	 *  - POST 请求体内容为：data=<URLEncoder.encode(data)>（application/x-www-form-urlencoded）
	 *  - 签名算法：sign = MD5(opcode+data+appkey+appsecret+tag+channelId)，取结果中的下标 8 至 24（共16字符）
	 *
	 * @param channelId 渠道标识，由客户端传入
	 * @param opcode    接口编码，如 "10001", "5003", "5004", "7000"
	 * @param tag       事务唯一标识，如果为 0 则自动生成随机数（8位以内）
	 * @param data      请求 data 字段（JSON 字符串）
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
			String body = "data=" + URLEncoder.encode(data, StandardCharsets.UTF_8.toString());

			// 根据运行模式选择 URL：测试环境或者正式环境
			String URL_BILLING = ServerContext.getInstance().getRunMode().isProduction() ? Config.CHANGYOU_SDK_URL_BILLING_PRODUCTION
					: Config.CHANGYOU_SDK_URL_BILLING_TEST;

			// 使用 WebClient 发起 HTTP POST 请求
			webClient.postAbs(URL_BILLING)
					.putHeader("appkey", Config.CHANGYOU_SDK_APP_KEY)
					.putHeader("tag", String.valueOf(tag))
					.putHeader("opcode", opcode)
					.putHeader("channelId", channelId)
					.putHeader("sign", sign)
					.putHeader("Content-Type", "application/x-www-form-urlencoded")
					.sendBuffer(Buffer.buffer(body), ar -> {
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