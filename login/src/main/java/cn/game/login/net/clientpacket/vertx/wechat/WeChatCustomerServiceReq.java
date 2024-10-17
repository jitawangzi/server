package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.core.cache.CacheType;
import cn.game.core.net.vertx.VxHolder;
import cn.game.login.cache.entity.PayOrder;
import cn.game.login.mapper.PayOrderMapper;
import cn.game.util.*;
import com.google.gson.JsonObject;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.RedissonKeys;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName WeChatCustomerServiceReq
 *
 * @description: 微信客服消息处理
 * https://developers.weixin.qq.com/miniprogram/dev/framework/server-ability/message-push.html#%E5%BC%80%E5%8F%91%E8%80%85%E6%9C%8D%E5%8A%A1%E5%99%A8%E6%8E%A5%E6%94%B6%E6%B6%88%E6%81%AF%E6%8E%A8%E9%80%81
 * @author: ly
 * @create: 2024-09-29 10:25 @Version 1.0
 */
public class WeChatCustomerServiceReq implements Handler<RoutingContext> {
    protected static final Logger log = LoggerFactory.getLogger(WeChatCustomerServiceReq.class);
    public static String testContentTxt = "";

//    发起验证
//    点击“提交”后，微信服务器会对开发者服务器发起验证，请在提交前按以下方式开发： 微信服务器将发送GET请求到填写的服务器地址URL上， GET请求携带参数如下表所示：
//
//    参数	描述
//    signature	签名
//    timestamp	时间戳
//    nonce	随机数
//    echostr	随机字符串
//    其中，signature签名的生成方式是：
//
//    将Token、timestamp、nonce三个参数进行字典序排序。
//    将三个参数字符串拼接成一个字符串进行sha1计算签名，即可获得signature。 开发者需要校验signature是否正确，以判断请求是否来自微信服务器，验签通过后，请原样返回echostr字符串。
//    举例：假设填写的URL="https://www.qq.com/revice"， Token="AAAAA"。
//
//    推送的URL链接：https://www.qq.com/revice?signature=f464b24fc39322e44b38aa78f5edd27bd1441696&echostr=4375120948345356249&timestamp=1714036504&nonce=1514711492
//    将token、timestamp、nonce三个参数进行字典序排序，排序后结果为:["1514711492","1714036504","AAAAA"]。
//    将三个参数字符串拼接成一个字符串："15147114921714036504AAAAA"
//    进行sha1计算签名：f464b24fc39322e44b38aa78f5edd27bd1441696
//    与URL链接中的signature参数进行对比，相等说明请求来自微信服务器，合法。
//    构造回包返回微信，回包消息体内容为URL链接中的echostr参数4375120948345356249。

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    @Override
    public void handle(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        HttpServerResponse response = ctx.response();
        try{
            if (request.method() == HttpMethod.GET) {
                doGet(request,response);
            } else if (request.method() == HttpMethod.POST) {
                doPost(request,response,ctx);
            }
            else {
                response.end("error");
            }
        }catch (Exception e){
            e.printStackTrace();
            response.end("error");
        }
    }

    /**
     * GET 请求处理微信的签名
     */
    private void doGet(HttpServerRequest request, HttpServerResponse response) {
        request.getParam(":");
        log.info(String.format("doGet:%s", request.params().toString()));
        String signature = request.getParam("signature");
        String nonce = request.getParam("nonce");
        String timestamp = request.getParam("timestamp");
        String echostr = request.getParam("echostr");
        List<String> paramList = new ArrayList<>();
        paramList.add(IOSPayOrderProcessor.CustomerToken);
        paramList.add(nonce);
        paramList.add(timestamp);
        Collections.sort(paramList);
        String tmpStr = String.join("", paramList);
        String sign = DigestUtils.sha1Hex(tmpStr);
        if (sign.equals(signature)) {
            response.end(echostr);
        } else {
            log.info("customer_service,doGet,signerror," + sign + "," + signature);
            response.write("error");
        }
    }

    private void doPost(HttpServerRequest request, HttpServerResponse response, RoutingContext ctx) {
        String rst = "1";
        RLock lock = null;
        try{
            String postBodyStr = ctx.getBodyAsString();
            String msgSignature = request.getParam("msg_signature");
            String timestamp = request.getParam("timestamp");
            String nonceStr = request.getParam("nonce");
            WXBizMsgCrypt pc = new WXBizMsgCrypt(IOSPayOrderProcessor.CustomerToken, IOSPayOrderProcessor.encodingAesKey, Config.wechat_appid);
            log.info("customer_service,doPost," + replaceBlank(postBodyStr));
            //TODO 之后添加
            /*if (!WeChatManager.getInstance().accessTokenIsEffect()) {
                logger.error("TOKEN 失效 联系客服");
                return;
            }*/
            String result = pc.decryptMsg(msgSignature, timestamp, nonceStr, postBodyStr);
            Element element = XMLParse.loadDocumentElement(result);
            Node msgTypeNode = element.getElementsByTagName("MsgType").item(0);
            Node event = element.getElementsByTagName("Event").item(0);
            Node miniGame = element.getElementsByTagName("MiniGame").item(0);
            Node sessionFromNode = element.getElementsByTagName("SessionFrom").item(0);

            log.info("customer_service,doPost,result," + replaceBlank(result));
            String openId = element.getElementsByTagName("FromUserName").item(0).getTextContent();
            boolean isOrder = false;
            // 有这个节点表示用户点击了进入会话
            if (sessionFromNode != null) {
                String content = sessionFromNode.getTextContent();
                log.info("customer_service,content,before,json," + content);
                if (content == null || "".equals(content)) {
                    return;
                }
                lock =  tryLockOrderData(openId);
                JsonObject jsonObject = JsonUtil.parserJson(content);
                isOrder = jsonObject.has("orderId");
                if (isOrder) {
                    long playerId = jsonObject.get("playerId").getAsLong();
                    String orderId = jsonObject.get("orderId").getAsString();
//                    WeChatMsgManager.getInstance().addOrderMsg(openId, playerId, orderId);
                     saveOrderMsg(openId, orderId,playerId);
                }
            }
                if (msgTypeNode != null && !isOrder ) {
                    String msgType = msgTypeNode.getTextContent();
                    // 收到卡片消息 下发充值连接
                    if (msgType.equals("miniprogrampage")) {
                        PayOrder payOrder = getPayOrderInfo(openId);
                        if (payOrder != null) {
                            sendCustomer(payOrder, openId);
                        }
                    }else if (msgType.equals("text") || msgType.equals("image") || msgType.equals("voice")
                            || msgType.equals("video") || msgType.equals("music") || msgType.equals("link")) {
                        Node toUserNode = element.getElementsByTagName("ToUserName").item(0);
                        String toUserName = "";
                        if (toUserNode != null) {
                            toUserName = toUserNode.getTextContent();
                        }
                        if (!"".equals(toUserName)) {
                            rst = sendKFTest(msgType, openId, toUserName);
                        }
                    }
                }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        } finally{
            if (lock != null) lock.unlock();
            log.info("customer_service,doPost,response: " + rst);
            response.end(rst);
        }

    }

    private PayOrder getPayOrderInfo(String openId) {

            String key = CacheType.IOS_OPENID_ORDER_DATA.key(openId);
            String val = RedisUtil.get(key);
            if (val != null) {
                String[] strs = val.split("_");
                if (strs.length == 2) {
                    long pid = Long.parseLong(strs[0]);
                    String orderId = strs[1];
                    PayOrderMapper mapper = SpringContextLoader.getContext().getBean(PayOrderMapper.class);
                    PayOrder payOrder = mapper.selectByPrimaryKey(Long.parseLong(orderId));
                    return payOrder;
                }
            }
        return null;
    }


    public static String getRunOrderId(String openId){
        RLock lock = tryLockOrderData(openId);
        if (lock == null) return null;
        try{
            String key = CacheType.IOS_OPENID_ORDER_DATA.key(openId);
            String val = RedisUtil.get(key);
            if (val != null) {
                String[] strs = val.split("_");
                if (strs.length == 2) {
                    String orderId = strs[1];
                    return orderId;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            lock.unlock();
        }

        return null;
    }

    private static RLock tryLockOrderData(String openId) {
        RLock lock = LockUtil.tryLockNoWait(2,CacheType.IOS_OPENID_ORDER_DATA_LOCK.key(openId));
        if (lock == null){
            log.error(String.format("获取订单信息锁失败,openId = " + openId));
            return null;
        }
        return lock;
    }

    public static String delRunOrderData(String openId,String orderId){
        RLock lock = tryLockOrderData(openId);
        if (lock == null) return null;
        try{
            String key = CacheType.IOS_OPENID_ORDER_DATA.key(openId);
            String val = RedisUtil.get(key);
            if (val != null) {
                String[] strs = val.split("_");
                if (strs.length == 2) {
                    String findOrderId = strs[1];
                    if (findOrderId.equals(orderId)){
                        log.info("删除订单信息成功,openId = " + openId + ",orderId = " + orderId);
                    } else {
                        log.error("删除订单信息失败,openId = " + openId + ",orderId = " + orderId +", findOrderId = " + findOrderId);
                    }
                    return orderId;
                }
            }
            RedisUtil.delete(key);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            lock.unlock();
        }
        return null;
    }

    private void saveOrderMsg(String openId, String orderId,long pid) {
            String key = CacheType.IOS_OPENID_ORDER_DATA.key(openId);
            String val = String.format("%s_%s",pid,orderId);
            RedisUtil.set(key,val,5, TimeUnit.MINUTES);
    }

    private void sendCustomer(PayOrder receipt, String openId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("touser", openId);
        paramMap.put("msgtype", "link");
        Map<String, Object> linkMap = new HashMap<>();
        paramMap.put("link",linkMap);
        String thumb_url = "https://ydtj.the3.changyou.com/dev/wx_release_debug/webgl/Assets/PayImg/chongzhi-2.png";

//        linkMap.put("content","点我充值");
        linkMap.put("title", "点我充值");
        linkMap.put("description", "点我充值" + (double)receipt.getPrice() / 100 + "元");
        linkMap.put("url", String.format(Config.wechat_pay_page_url+"?orderId=%s", receipt.getId() +"&openId="+openId));
        //"https://ydxhxbjzmp.the3.changyou.com/release/Assets/PayImg/chongzhi-2.png";
        linkMap.put("thumb_url", thumb_url);
        paramMap.put("link", linkMap);
        Map<String, String> sendBuildMap = new HashMap<>();
        try {
//            IOSPayOrderProcessor.refreshOnceAccessToken(System.currentTimeMillis());
            sendBuildMap.put("access_token", IOSPayOrderProcessor.accessToken);
            String sendUrl = HttpUtil.buildUrl(IOSPayOrderProcessor.SEND_URL, sendBuildMap);
            log.info("sendCustomer,sendUrl: " + sendUrl);
            log.info("sendCustomer,paramMap: " +JsonUtil.toJsonStr(paramMap));
//            String result = HttpUtil.postJSON2(sendUrl,  JsonUtil.toJsonStr(paramMap),null);
             HttpHelp.postJSonUrl( sendUrl,   paramMap,
                    result -> {
                        try {
                            log.info("sendCustomer,result: " + result);
                            if (JsonUtil.parserJson(result).get("errcode").getAsInt() != 0) {
                                log.error("发送客服消息失败！result:{}", result);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error(e.getMessage());
                        }
                    },
                     e -> log.error("TEXT---发送客服消息失败！result:{}",e )
                    );
//            String result = HttpUtil.requestHttpWithPostReturnString(sendUrl, JsonUtil.map2Json(paramMap), CharsetEncoding.ENCODING_UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sendKFTest(String msgType, String fromUser, String toUser) {
        if (!"".equals(testContentTxt)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("touser", fromUser);
            paramMap.put("msgtype", "text");
            Map<String, Object> textMap = new HashMap<>();
            textMap.put("content", testContentTxt);
            paramMap.put("text", textMap);
            Map<String, String> sendBuildMap = new HashMap<>();
            sendBuildMap.put("access_token", IOSPayOrderProcessor.accessToken);
            String sendUrl = HttpUtil.buildUrl(IOSPayOrderProcessor.SEND_URL, sendBuildMap);

//            String sendUrl = HttpUtil.buildUrl(WeChatManager.SEND_URL, sendBuildMap);
            try {
                log.info("TEXT---sendCustomer,sendUrl: " + sendUrl);
                log.info("TEXT---sendCustomer,paramMap: " + JsonUtil.toJsonString(paramMap));
                HttpHelp.postJSonUrl(sendUrl,paramMap,
                        result -> {
                            log.info("TEXT---sendCustomer,result: " + result);
                            try {
                                if (JsonUtil.parserJson(result).get("errcode").getAsInt() != 0) {
                                    log.error("TEXT---发送客服消息失败！result:{}", result);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error(e.getMessage());
                            }
                        },
                        e -> log.error("TEXT---发送客服消息失败！result:{}",e )
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String rst = "<xml>" +
                "<ToUserName><![CDATA[" + fromUser + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + toUser + "]]></FromUserName>" +
                "<CreateTime>" + System.currentTimeMillis() / 1000 + "</CreateTime>" +
                "<MsgType><![CDATA[transfer_customer_service]]></MsgType>" +
                "</xml>";
        return rst;
    }
}
