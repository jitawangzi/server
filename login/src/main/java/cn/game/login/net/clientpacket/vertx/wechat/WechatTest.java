package cn.game.login.net.clientpacket.vertx.wechat;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;

import cn.game.util.Config;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class WechatTest implements Handler<RoutingContext> {

	String token = "token" ; 
	
	protected static final Logger log = LoggerFactory.getLogger(WechatTest.class);

	@Override
	public void handle(RoutingContext context) {
		HttpServerRequest request = context.request();
		HttpServerResponse response = context.response().putHeader("content-type", "text/json");

		String signature = request.getParam("signature"); 
		String timestamp= request.getParam("timestamp"); 
		String nonce= request.getParam("nonce"); 
		String echostr= request.getParam("echostr"); 
		
		List<String> list = new ArrayList<>();
		list.add(timestamp); 
		list.add(nonce); 
		list.add(Config.wechat_push_token); 
		Collections.sort(list);
		StringBuilder builder = new StringBuilder(); 
		for (String string : list) {
			builder.append(string)	;
		}
		
	   String combinedString = builder.toString();
	   String signature2 = null; 

        // 对拼接后的字符串进行 SHA-1 加密
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(combinedString.getBytes());
            signature2 = bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject(); 
        if (signature.equals(signature2)) {
        	jsonObject.put("ErrCode", 0) ; 
        	jsonObject.put("ErrMsg", "Success") ; 
        	jsonObject.put("echostr", echostr) ; 
        	response.end(jsonObject.toString()); 
		}else {
			log.error("微信测试失败") ;
		}

	}
	
	 // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
