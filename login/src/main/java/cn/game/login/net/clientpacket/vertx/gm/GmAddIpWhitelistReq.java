package cn.game.login.net.clientpacket.vertx.gm;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * @ClassName GmAddIpWhitelistReq
 *
 * @description: GM 添加ip白名单接口
 * @author: ly
 * @create: 2024-09-19 16:12 @Version 1.0
 */
public class GmAddIpWhitelistReq implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext context) {
        HttpServerResponse response = context.response().putHeader("content-type", "application/json");
        JSONObject result = GmSelectOrderReq.getResultData();
        String ip = context.request().getParam("ip");
        Integer failTime = context.request().getParam("failTime") == null ? 0 : Integer.parseInt(context.request().getParam("failTime"));
        if (!isValidIPAddress(ip)) {
            result.put("result","ip check error");
            response.end(result.toString());
            return;
        }
        result.put("data", IpWhitelistManger.getInstance().addIpWhitelist(ip, failTime));
        response.end(result.toString());
    }
    private static final String IPV4_REGEX =
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValidIPAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        Matcher matcher = IPV4_PATTERN.matcher(ip);
        return matcher.matches();
    }
}
