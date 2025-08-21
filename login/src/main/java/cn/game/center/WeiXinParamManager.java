package cn.game.center;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import cn.game.login.net.clientpacket.vertx.wechat.IOSPayOrderProcessor;
import cn.game.util.DateUtil;
import cn.game.util.HttpUtil;
import cn.game.util.JsonUtil;

/**
 * @ClassName WeiXinParamManager
 *
 * @description: 微信相关参数请求
 * @author: ly
 * @create: 2025-01-10 16:35 @Version 1.0
 */
public class WeiXinParamManager {
	protected static Logger log = LoggerFactory.getLogger(WeiXinParamManager.class);
	private final static WeiXinParamManager Instance = new WeiXinParamManager();

	// 一个商户号只能初始化一个配置，否则会因为重复的下载任务报错
	/***https://developers.weixin.qq.com/miniprogram/dev/framework/server-ability/message-push.html#%E5%BC%80%E5%8F%91%E8%80%85%E6%9C%8D%E5%8A%A1%E5%99%A8%E6%8E%A5%E6%94%B6%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81*/
	public String accessToken = "ggsaPOLW05QpMfA1w5SotegFUQgpMb";
	public long accessTokenExpiresTimer = 0;

	long lastRefreshTimer = 0;

	private WeiXinParamManager() {
	}

	private boolean canRefresh(long now) {
		long difTimer = 2 * DateUtil.MINUTE_MILLIS;
		boolean res = now - lastRefreshTimer >= difTimer;
		if (res) {
			lastRefreshTimer = now;
		}
		return res;
	}

	private boolean refreshAccessToken(long now) throws Exception {

		String url = String.format(IOSPayOrderProcessor.tokenUrl, cn.game.util.Config.wechat_appid, cn.game.util.Config.wechat_secret);
		log.info("startRefreshAccessTokenTask url:" + url);
		String result = HttpUtil.get(url);
		log.info(String.format("result:%s", result));
		if (result != null) {
			JsonObject jsonObject = JsonUtil.parserJson(result);
			String accessToken = jsonObject.get("access_token").getAsString();
			String expires_in = jsonObject.get("expires_in").getAsString();
			this.accessToken = accessToken;
			this.accessTokenExpiresTimer = now + Long.parseLong(expires_in) * DateUtil.SECOND_MILLIS;
		}
		return true;
	}

	public static WeiXinParamManager getInstance() {
		return Instance;
	}

	public synchronized String getAccessToken() {
		long now = System.currentTimeMillis();
		if (canRefresh(now)) {
			try {
				refreshAccessToken(now);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return accessToken;
	}

	public long getAccessTokenExpiresTimer() {
		return accessTokenExpiresTimer;
	}
}
