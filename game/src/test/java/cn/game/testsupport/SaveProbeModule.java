package cn.game.testsupport;

import java.util.ArrayList;
import java.util.List;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo;

/** 保存测试的可变模块，放在业务模块扫描范围之外，仍通过生产 JSON 类型恢复。 */
public final class SaveProbeModule extends BasePlayerModule {
    /** 本次应完整保存的嵌套可变值。 */
    public List<String> values = new ArrayList<>(List.of("first module"));
    /** 独立表实体沿用独立存储排除约定。 */
    public transient List<DbEntity> entities = new ArrayList<>();
    /** 加载后的业务hook测试开关，不参与聚合序列化。 */
    public transient boolean failOnInit;
    public transient int initCalls;
    @Override public void initFromDbAfter() {
        initCalls++; values.add("initialized");
        if (failOnInit) { throw new IllegalStateException("initialization failed after mutation"); }
    }
    /** 公开构造供带类型 JSON 恢复使用。 */
    public SaveProbeModule() {}
    @Override
    public void buildPlayerAllInfo(PlayerAllInfo.Builder builder) {}
    @Override
    public EventTypeEnum[] getEventTypes() { return new EventTypeEnum[0]; }
    @Override
    public void handleEvent(PlayerEvent event) {}
    @Override
    public void autoSaveTasks(List<DbEntity> target) { target.addAll(entities); }
}
