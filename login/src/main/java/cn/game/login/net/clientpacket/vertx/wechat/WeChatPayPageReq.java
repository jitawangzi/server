package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.core.net.vertx.VxHolder;
import cn.game.login.LoginServer;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.util.Config;
import cn.game.util.LockUtil;
import cn.game.util.SpringContextLoader;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName WeChatPayPageReq
 *
 * @description: iOS 支付  wx_pay.ftl 跳转的页面的参数
 * @author: ly
 * @create: 2024-09-29 10:08 @Version 1.0
 */
public class WeChatPayPageReq implements Handler<RoutingContext> {
    protected static final Logger log = LoggerFactory.getLogger(WeChatPayPageReq.class);

    @Override
    public void handle(RoutingContext ctx) {
        HttpServerRequest req = ctx.request();
        String orderId = req.getParam("orderId");
        String openId = req.getParam("openId");
        PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
        PayOrder payOrder = mapper.selectByPrimaryKey(Long.parseLong(orderId));
        if (payOrder == null) {
            log.error(String.format("订单不存在 orderId = " + orderId));
            failPage(ctx, "订单不存在");
            return;
        }
        if (payOrder.getPayState() == 2){//该订单已经支付
            log.error(String.format("订单已经支付 orderId = " + orderId));
            failPage(ctx, "该订单已经完成支付");
            return;
        }
        String runOrderId = WeChatCustomerServiceReq.getRunOrderId(openId);
        if (runOrderId == null || !runOrderId.trim().equals(orderId.trim())){
            log.error(String.format("订单与orderId不匹配 orderId =%s openId =%s, findRunOrderId:%s " , orderId,openId,runOrderId));
            failPage(ctx, "该订单已经过期，请从新下单");
            return;
        }
        Map<String,String> sign = sign(req.absoluteURI(), UUID.randomUUID().toString().replace("-", ""), System.currentTimeMillis()/1000);
        FreeMarkerTemplateEngine engine = FreeMarkerTemplateEngine.create(VxHolder.vertx);
        Map<String,Object> data = new HashMap<>();
        data.put("timestamp", sign.get("timestamp"));
        data.put("nonceStr", sign.get("nonceStr"));
        data.put("prepay_id", payOrder.getThirdOrderId());
        data.put("paySign", genPaySign(sign.get("timestamp"), sign.get("nonceStr"), payOrder.getThirdOrderId()));
        data.put("jsApiList", new ArrayList<String>());
        data.put("appId", Config.wechat_appid);
        data.put("signature",sign.get("signature"));
        data.put("price", (double)payOrder.getPrice()/100);
        data.put("itemName", payOrder.getItemName());
        engine.render(data, "/template/wx_pay.ftl", res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });

    }

    private void failPage(RoutingContext ctx, String failMsg) {
        FreeMarkerTemplateEngine engine = FreeMarkerTemplateEngine.create(VxHolder.vertx);
        Map<String,Object> data = new HashMap<>();
        data.put("failMsg", failMsg);
        engine.render(data, "/template/pay_error.ftl", res -> {
            if (res.succeeded()) {
                ctx.response().end(res.result());
            } else {
                ctx.fail(res.cause());
            }
        });
    }

    /**
     * 生成paySign
     */
    public String genPaySign(String timestamp, String nonce_str, String prepay_id){
        String baseStr = Config.wechat_appid + "\n" +
                timestamp + "\n" +
                nonce_str + "\n" +
                "prepay_id=" +  prepay_id + "\n";
        return IOSPayOrderProcessor.config.createSigner().sign(baseStr).getSign();
    }

    public Map<String, String> sign(String url,String nonce_str, long timestamp) {
        Map<String, String> ret = new HashMap<>();
        String string1;
        String signature = "";
        String jsapi_ticket = IOSPayOrderProcessor.jsapiTicket;
        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        System.out.println(string1);
        signature = DigestUtils.sha1Hex(string1);

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp+"");
        ret.put("signature", signature);

        return ret;
    }

}
