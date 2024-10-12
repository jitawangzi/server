package cn.game.login.net.clientpacket.vertx.wechat;

import cn.game.login.cache.entity.PayOrder;
import cn.game.protocol.protobuf.ServerMsg;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Android("Android"),
        IOS("IOS");
        String platform;

        PayOrderPlatformEnum(String platform) {
            this.platform = platform;
        }

        public String getPlatform() {
            return platform;
        }

        public boolean equals(String platform) {
            return this.platform.equalsIgnoreCase(platform);
        }
    }

}
