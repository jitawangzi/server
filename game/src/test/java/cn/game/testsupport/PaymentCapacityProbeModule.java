package cn.game.testsupport;

import java.util.concurrent.atomic.AtomicInteger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.game.games.net.game.module.player.PlayerModule;

/** 保存探针夹具置于业务模块扫描范围外；保留真实订单集合和序列化行为。 */
public final class PaymentCapacityProbeModule extends PlayerModule {
    /** 数量读取次数，不进入持久 JSON。 */
    @JsonIgnore public final AtomicInteger countReads = new AtomicInteger();
    /** 模拟容量探针失败，不改变正常序列化和订单存储。 */
    @JsonIgnore public boolean failCount;
    /** 公开无参构造用于真实带类型 JSON 往返。 */
    public PaymentCapacityProbeModule() {}
    @Override public int retainedPaymentOrderCount() {
        countReads.incrementAndGet();
        if (failCount) { throw new IllegalStateException("probe failure"); }
        return super.retainedPaymentOrderCount();
    }
}
