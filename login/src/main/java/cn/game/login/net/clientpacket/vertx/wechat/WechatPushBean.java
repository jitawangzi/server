package cn.game.login.net.clientpacket.vertx.wechat;

import com.alibaba.fastjson2.annotation.JSONField;

/**    
 * 微信发货消息参数
 * 2024年3月25日 上午11:11:16
 * @author SYQ
 */
public class WechatPushBean {
    public String ToUserName;
    public String FromUserName;
    public long CreateTime;
    public String MsgType;
    public String Event;
    public MiniGame MiniGame;

	public static class MiniGame {
		@JSONField(serialize = false)
        public String Payload;
        public Payload PayloadObj;
        public String PayEventSig;
        public boolean IsMock;
    }

    public static class WeChatPayInfo {
        public String MchOrderNo;
        public String TransactionId;
    }

    public static class GoodsInfo {
        public String ProductId;
        public int Quantity;
        public String ZoneId;
        public int OrigPrice;
        public int ActualPrice;
        public String Attach;
        public int OrderSource;
    }
    
    public class Payload {
        public String OpenId;
        public String OutTradeNo;
        public WeChatPayInfo WeChatPayInfo;
        public int Env;
        public GoodsInfo GoodsInfo;
        
    }

}
