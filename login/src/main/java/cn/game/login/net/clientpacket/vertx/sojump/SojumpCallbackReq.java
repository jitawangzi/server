package cn.game.login.net.clientpacket.vertx.sojump;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.net.clientpacket.vertx.UserHelper;
import cn.game.protocol.protobuf.ServerMsg.LoginGameQuestionnairePush_7d000090;
import cn.game.util.JsonUtil;
import cn.game.util.ServerType;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @ClassName SojumpCallbackReq
 *
 * @description: 问卷星回调接口
 * @author: ly
 * @create: 2024-09-13 20:21 @Version 1.0
 */
public class SojumpCallbackReq implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(SojumpCallbackReq.class);
    static final String SECURITY_KEY = "9359aa7c4ccf4dcf93a04de421482fe3";
    private static final String ALGO = "AES";
    private static final String ALGO_MODE = "AES/CBC/NoPadding";

  @Override
  public void handle(RoutingContext context) {
    HttpServerResponse response = context.response();
    String rawString = context.getBodyAsString();
    logger.info("sojump rawString: " + rawString);
    try {
      if (StringUtils.isNotEmpty(rawString)) {
        String jsonString = StringUtils.EMPTY;

        // 问卷星传过来的格式有两种, 一种有content=, 一种没有
        // 问卷星传过来的格式有两种, 一种有content=, 一种没有
        if (rawString.startsWith("content=")) {
          String content = rawString.substring(8);
            content = URLDecoder.decode(content, "UTF-8");
          if (StringUtils.isNotEmpty(content)) {
            jsonString = aesDecrypt(content, SECURITY_KEY);
          } else {
            logger.info("sojump content is empty.");
            response.end("fail");
            return;
          }
        } else {
          jsonString = aesDecrypt(rawString, SECURITY_KEY);
        }
        if (StringUtils.isNotBlank(jsonString)) {
          logger.info("sojump param json:{}", jsonString);
          JsonObject json = null;
            json = JsonUtil.parserJson(jsonString);
          if (json != null) {
            JsonElement answerStr = json.get("answer");
            JsonObject answer = answerStr.getAsJsonObject();
            long pid =  answer.get("sojumpparm").getAsLong();
            int type = answer.get("type").getAsInt();
            //TODO  通知gameServer 发放奖励
			String serverId = UserHelper.getServerId(pid);
			LoginGameQuestionnairePush_7d000090 loginGameQuestionnairePush_7d000090 = LoginGameQuestionnairePush_7d000090.newBuilder()
					.setPlayerId(pid)
					.setType(type)
					.build();
			if (StringUtils.isEmpty(serverId)) {
				VxHolder.sendRemoteServer(ServerType.Game, loginGameQuestionnairePush_7d000090);
			} else {
				VxHolder.sendRemoteServer(serverId, loginGameQuestionnairePush_7d000090);
			}
          } else {
            logger.info("sojump param json is empty.");
            response.end("fail");
          }
        } else {
          logger.info("sojump aes decrypt result is empty.");
            response.end("fail");
        }
      } else {
        logger.info("sojump not found content.");
          response.end("fail");
      }
      response.end("success");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

    static String aesDecrypt(String encryptedData, String securityKey) {
        try {
            byte[] data = Base64.getDecoder().decode(encryptedData);
            byte[] iv = Arrays.copyOfRange(data, 0, 16);
            Cipher cipher = Cipher.getInstance(ALGO_MODE);
            SecretKeySpec keySpec = new SecretKeySpec(securityKey.getBytes(StandardCharsets.UTF_8), ALGO);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] countent = Arrays.copyOfRange(data, 16, data.length);
            byte[] original = cipher.doFinal(countent);
            String originalString = new String(original);
            return originalString.trim();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

}
