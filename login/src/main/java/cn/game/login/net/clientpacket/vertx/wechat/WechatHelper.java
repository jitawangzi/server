package cn.game.login.net.clientpacket.vertx.wechat;

import com.alibaba.fastjson2.JSON;

import cn.game.login.net.clientpacket.vertx.wechat.WechatPushBean.Payload;

public class WechatHelper {
	public static final String WX_AUTH_URL_STRING = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

	public static WechatPushBean parseWechatPushBean(String arg) {

		WechatPushBean pushBean = JSON.parseObject(arg, WechatPushBean.class); 
		
		Payload payload = JSON.parseObject(pushBean.MiniGame.Payload, Payload.class); 
		pushBean.MiniGame.PayloadObj = payload ; 
		return pushBean ; 
	}
}