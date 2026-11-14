package cn.game.testsupport;

import java.util.concurrent.atomic.AtomicInteger;

import cn.game.games.cache.entity.ItemNoStack;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.module.item.AbstractItemNoStackModule;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**
 * 玩家只读状态自省测试用辅助模块集合，置于testsupport包避免污染Player静态模块扫描
 *
 * @author shuyongqiang
 */
public final class InspectTestModules {

	private InspectTestModules() {
	}

	/**
	 * 只读探测哨兵模块，用于断言自省过程不会触发业务getter
	 *
	 * @author shuyongqiang
	 */
	public static class SpyGetterModule extends BasePlayerModule {

		/** 业务getter调用次数计数器 */
		public final AtomicInteger getterCalls = new AtomicInteger(0);

		/** 内部只读状态字段样例 */
		@SuppressWarnings("unused")
		private String internalMarker = "immutable-state";

		/**
		 * 模拟带副作用或懒初始化的业务getter，自省不得触发此方法
		 *
		 * @return 固定测试值
		 */
		public String getLazyBusinessValue() {
			getterCalls.incrementAndGet();
			return "mutated";
		}

		/**
		 * 构建玩家全量信息（测试空实现）
		 *
		 * @param builder 玩家全量信息构造器
		 */
		@Override
		public void buildPlayerAllInfo(Builder builder) {
		}

		/**
		 * 返回关注事件类型列表
		 *
		 * @return 空事件数组
		 */
		@Override
		public EventTypeEnum[] getEventTypes() {
			return new EventTypeEnum[0];
		}

		/**
		 * 处理玩家事件（测试空实现）
		 *
		 * @param event 玩家事件
		 */
		@Override
		public void handleEvent(PlayerEvent event) {
		}
	}

	/**
	 * 模拟inspectState抛异常的故障模块，用于验证单模块异常隔离
	 *
	 * @author shuyongqiang
	 */
	public static class FaultyInspectModule extends BasePlayerModule {

		/**
		 * 主动抛出受控异常以验证单模块自省异常隔离与日志告警
		 *
		 * @return 永不返回
		 */
		@Override
		public Object inspectState() {
			throw new IllegalStateException("simulated-inspect-fault");
		}

		/**
		 * 构建玩家全量信息（测试空实现）
		 *
		 * @param builder 玩家全量信息构造器
		 */
		@Override
		public void buildPlayerAllInfo(Builder builder) {
		}

		/**
		 * 返回关注事件类型列表
		 *
		 * @return 空事件数组
		 */
		@Override
		public EventTypeEnum[] getEventTypes() {
			return new EventTypeEnum[0];
		}

		/**
		 * 处理玩家事件（测试空实现）
		 *
		 * @param event 玩家事件
		 */
		@Override
		public void handleEvent(PlayerEvent event) {
		}
	}

	/**
	 * 包含循环引用字段的测试模块，用于验证祖先链环检测安全占位且不影响兄弟字段
	 *
	 * @author shuyongqiang
	 */
	public static class CircularRefModule extends BasePlayerModule {

		/** 构成自引用环的测试列表字段，验证祖先链环检测直接输出&lt;circular:...&gt;占位 */
		private final java.util.List<Object> circularNode;

		/** 同模块内正常可序列化的兄弟字段 */
		@SuppressWarnings("unused")
		private String healthySibling = "sibling-ok";

		/**
		 * 构造包含自引用环字段的测试模块
		 */
		public CircularRefModule() {
			java.util.List<Object> cycleList = new java.util.ArrayList<>();
			cycleList.add(cycleList);
			this.circularNode = cycleList;
		}

		/**
		 * 构建玩家全量信息（测试空实现）
		 *
		 * @param builder 玩家全量信息构造器
		 */
		@Override
		public void buildPlayerAllInfo(Builder builder) {
		}

		/**
		 * 返回关注事件类型列表
		 *
		 * @return 空事件数组
		 */
		@Override
		public EventTypeEnum[] getEventTypes() {
			return new EventTypeEnum[0];
		}

		/**
		 * 处理玩家事件（测试空实现）
		 *
		 * @param event 玩家事件
		 */
		@Override
		public void handleEvent(PlayerEvent event) {
		}
	}

	/**
	 * 不可堆叠物品测试模块，用于覆盖AbstractItemNoStackModule自省逻辑
	 *
	 * @author shuyongqiang
	 */
	public static class TestItemNoStackModule extends AbstractItemNoStackModule<ItemNoStack> {

		/**
		 * 构建玩家全量信息（测试空实现）
		 *
		 * @param builder 玩家全量信息构造器
		 */
		@Override
		public void buildPlayerAllInfo(Builder builder) {
		}

		/**
		 * 返回测试物品类型枚举
		 *
		 * @return 装备类型枚举
		 */
		@Override
		public GoodsTypeEnum getGoodsTypeEnum() {
			return GoodsTypeEnum.Equipment;
		}

		/**
		 * 配置校验（测试空实现）
		 *
		 * @param id 配置ID
		 */
		@Override
		public void checkConfig(int id) {
		}

		/**
		 * 创建不可堆叠物品实例
		 *
		 * @return 新建ItemNoStack对象
		 */
		@Override
		public ItemNoStack newInstance() {
			return new ItemNoStack();
		}

		/**
		 * 返回关注事件类型列表
		 *
		 * @return 空事件数组
		 */
		@Override
		public EventTypeEnum[] getEventTypes() {
			return new EventTypeEnum[0];
		}

		/**
		 * 处理玩家事件（测试空实现）
		 *
		 * @param event 玩家事件
		 */
		@Override
		public void handleEvent(PlayerEvent event) {
		}
	}

	/**
	 * DFS自省边界测试用共享叶子节点
	 *
	 * @author shuyongqiang
	 */
	public static class SharedLeaf {
		/** 共享节点标签 */
		private final String tag;
		/** 共享节点数值 */
		private final int count;

		/**
		 * 构造共享叶子节点
		 *
		 * @param tag 标签
		 * @param count 数值
		 */
		public SharedLeaf(String tag, int count) {
			this.tag = tag;
			this.count = count;
		}
	}

	/**
	 * DFS自省深度链节点
	 *
	 * @author shuyongqiang
	 */
	public static class DepthNode {
		/** 当前层级编号 */
		private final int level;
		/** 下一层级子节点 */
		private DepthNode next;

		/**
		 * 构造深度链节点
		 *
		 * @param level 层级编号
		 */
		public DepthNode(int level) {
			this.level = level;
		}
	}

	/**
	 * 验证共享引用展开、最大递归深度截断、容器元素上限、Temporal/UUID保留与JDK包安全降级的测试模块
	 *
	 * @author shuyongqiang
	 */
	public static class DfsBoundsTestModule extends BasePlayerModule {

		/** 非父子兄弟路径共享左引用 */
		private final SharedLeaf sharedLeft;
		/** 非父子兄弟路径共享右引用 */
		private final SharedLeaf sharedRight;
		/** 超过MAX_INSPECT_DEPTH的多层链式对象 */
		private final DepthNode deepChain;
		/** 超过MAX_INSPECT_ELEMENTS的大列表 */
		private final java.util.List<Integer> oversizedList;
		/** 超过MAX_INSPECT_ELEMENTS的大Map */
		private final java.util.Map<String, Integer> oversizedMap;
		/** JDK受限包类型实例，应安全降级为opaque占位 */
		private final java.io.ByteArrayInputStream opaqueJdkStream;
		/** Java时间类型字段 */
		private final java.time.LocalDate temporalDate;
		/** UUID字段 */
		private final java.util.UUID uuidValue;

		/**
		 * 构造包含共享引用、超深度、超元素数与JDK对象的边界测试模块
		 */
		public DfsBoundsTestModule() {
			SharedLeaf shared = new SharedLeaf("shared-ok", 7);
			this.sharedLeft = shared;
			this.sharedRight = shared;

			DepthNode head = new DepthNode(1);
			DepthNode cursor = head;
			for (int i = 2; i <= 11; i++) {
				DepthNode nextNode = new DepthNode(i);
				cursor.next = nextNode;
				cursor = nextNode;
			}
			this.deepChain = head;

			this.oversizedList = new java.util.ArrayList<>();
			this.oversizedMap = new java.util.LinkedHashMap<>();
			for (int i = 0; i < 220; i++) {
				this.oversizedList.add(i);
				this.oversizedMap.put("k" + i, i);
			}
			this.opaqueJdkStream = new java.io.ByteArrayInputStream(new byte[] { 1, 2, 3 });
			this.temporalDate = java.time.LocalDate.of(2026, 1, 15);
			this.uuidValue = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
		}

		/**
		 * 构建玩家全量信息（测试空实现）
		 *
		 * @param builder 玩家全量信息构造器
		 */
		@Override
		public void buildPlayerAllInfo(Builder builder) {
		}

		/**
		 * 返回关注事件类型列表
		 *
		 * @return 空事件数组
		 */
		@Override
		public EventTypeEnum[] getEventTypes() {
			return new EventTypeEnum[0];
		}

		/**
		 * 处理玩家事件（测试空实现）
		 *
		 * @param event 玩家事件
		 */
		@Override
		public void handleEvent(PlayerEvent event) {
		}
	}
}