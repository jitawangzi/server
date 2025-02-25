package cn.game.login.net.clientpacket.vertx.wechat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.login.cache.entity.PayOrder;
import cn.game.protocol.protobuf.ServerMsg;
import io.vertx.core.Future;

/**
 * @ClassName BasePayOrderProcessor
 *
 * @description:
 * @author: ly
 * @create: 2024-09-24 14:53 @Version 1.0
 */
public abstract class BasePayOrderProcessor {
    protected static Logger log = LoggerFactory.getLogger(BasePayOrderProcessor.class);

    public final PayOrderPlatformEnum platform;

    public BasePayOrderProcessor(PayOrderPlatformEnum platform) {
        this.platform = platform;
    }

    public PayOrderPlatformEnum getPlatform() {
        return platform;
    }

    /**
     * 创建订单
     * @param request
     * @return 新生成的订单
     */
    public abstract Future<PayOrder> createPayOrder(ServerMsg.PaymentOrderCreateRequest_7d000020 request, ServerMsg.PaymentOrderCreateResponse_7d000021.Builder resp);

    public static enum  PayOrderPlatformEnum {
		// 1 IOS APP
		IOS_APP(1),
		// 2 安卓 APP
		ANDROID_APP(2),
		// 3 IOS小游戏
    	IOS_WECAHT(3),
		// 4 安卓小游戏
		ANDROID_WECAHT(4),
		// 5 window 微信小游戏
		WINDOWS_WECAHT(5),
		// 6 mac微信小游戏
		MAC_WECAHT(6),
		;
        int platform;

        PayOrderPlatformEnum(int platform) {
            this.platform = platform;
        }

        public int getPlatform() {
            return platform;
        }

        public boolean equals(int platform) {
            return this.platform == platform;
        }

		public boolean isIOS() {
			return this == IOS_APP || this == IOS_WECAHT;
		}
    }

}
