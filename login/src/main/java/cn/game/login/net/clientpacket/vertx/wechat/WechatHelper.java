package cn.game.login.net.clientpacket.vertx.wechat;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson2.JSON;

import cn.game.login.net.clientpacket.vertx.wechat.WechatPushBean.Payload;
import cn.game.util.ByteHelp;
import cn.game.util.Config;

public class WechatHelper {
	public static final String WX_AUTH_URL_STRING = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

	public static WechatPushBean parseWechatPushBean(String arg) {

		WechatPushBean pushBean = JSON.parseObject(arg, WechatPushBean.class); 
		
		Payload payload = JSON.parseObject(pushBean.MiniGame.Payload, Payload.class); 
		pushBean.MiniGame.PayloadObj = payload ; 
		return pushBean ; 
	}
	
	
	public static String calcPaySig(String uri, String postBody, String appkey) {
        String needSignMsg = uri + "&" + postBody;
        return hmacSha256(appkey.getBytes(), needSignMsg.getBytes());
    }
	public static String calcPaymentGameItemPaySig(String signData) {
		String paySig = hmacSha256(Config.wechat_midas_AppKey.getBytes(),("requestMidasPaymentGameItem&"+ signData).getBytes()) ;
		return paySig;
	}

    /** 
     * 计算用户登录态签名
     * @param rawData
     * @param sessionKey
     * @return
     */
    public static String calcSignature(String rawData, String sessionKey) {
        return hmacSha256(sessionKey.getBytes(), rawData.getBytes());
    }

    private static String hmacSha256(byte[] keyBytes, byte[] messageBytes) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hmacBytes = hmacSha256.doFinal(messageBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String uri = "/wxa/game/getbalance";
        String appkey = "12345";
        String postBody = "{\"offer_id\": \"12345678\", \"openid\": \"oUrsfxxxxxxxxxx\", \"ts\": 1668136271, \"zone_id\": \"1\", \"env\": 0}";
        String sessionKey = "9hAb/NEYUlkaMBEsmFgzig==";

        String paySig = calcPaySig(uri, postBody, appkey);
        System.out.println("pay_sig: " + paySig);

        String signature = calcSignature(postBody, sessionKey);
        System.out.println("signature: " + signature);
    }
}