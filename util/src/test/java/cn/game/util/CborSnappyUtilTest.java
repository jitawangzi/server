package cn.game.util;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CborSnappyUtil 完备单元测试。
 * 覆盖往返序列化、多态对象、BitSet、压缩率测试与异常边界测试。
 *
 * @author shuyongqiang
 */
public class CborSnappyUtilTest {

	// 多态测试用基类与子类
	public static abstract class TestBaseModule {
		private String moduleName;
		public String getModuleName() { return moduleName; }
		public void setModuleName(String moduleName) { this.moduleName = moduleName; }
	}

	public static class HeroModule extends TestBaseModule {
		private int heroCount;
		private List<String> heroes = new ArrayList<>();
		public int getHeroCount() { return heroCount; }
		public void setHeroCount(int heroCount) { this.heroCount = heroCount; }
		public List<String> getHeroes() { return heroes; }
		public void setHeroes(List<String> heroes) { this.heroes = heroes; }
	}

	public static class BagModule extends TestBaseModule {
		private long capacity;
		private Map<Integer, Long> items = new HashMap<>();
		public long getCapacity() { return capacity; }
		public void setCapacity(long capacity) { this.capacity = capacity; }
		public Map<Integer, Long> getItems() { return items; }
		public void setItems(Map<Integer, Long> items) { this.items = items; }
	}

	public static class CompositeData {
		private String id;
		private BitSet bitSet;
		private LocalDateTime time;
		private Map<String, TestBaseModule> modules = new HashMap<>();
	}

	@Test
	@DisplayName("边界值与容错测试：null 与 empty")
	public void testNullAndEmptyBoundary() {
		assertNull(CborSnappyUtil.compress(null));
		assertNull(CborSnappyUtil.decompress(null));
		assertNull(CborSnappyUtil.decompress(new byte[0]));
		assertNull(CborSnappyUtil.decompress(null, Map.class));
		assertNull(CborSnappyUtil.decompress(new byte[0], Map.class));
		assertThrows(RuntimeException.class, () -> {
			CborSnappyUtil.decompress(new byte[] { 1, 2, 3, 4, 5 });
		});
	}

	@Test
	@DisplayName("多态对象往返序列化测试：准确恢复子模块具体类型")
	public void testPolymorphicModulesRoundTrip() {
		Map<String, TestBaseModule> modules = new HashMap<>();

		HeroModule heroModule = new HeroModule();
		heroModule.setModuleName("HeroModule");
		heroModule.setHeroCount(2);
		heroModule.getHeroes().add("Hero_1001");
		heroModule.getHeroes().add("Hero_1002");
		modules.put("hero", heroModule);

		BagModule bagModule = new BagModule();
		bagModule.setModuleName("BagModule");
		bagModule.setCapacity(500);
		bagModule.getItems().put(101, 99L);
		bagModule.getItems().put(102, 1000L);
		modules.put("bag", bagModule);

		byte[] compressed = CborSnappyUtil.compress(modules);
		assertNotNull(compressed);
		assertTrue(compressed.length > 0);

		Map<String, TestBaseModule> restored = CborSnappyUtil.decompress(compressed);
		assertNotNull(restored);
		assertEquals(2, restored.size());

		TestBaseModule restoredHero = restored.get("hero");
		assertInstanceOf(HeroModule.class, restoredHero);
		HeroModule castedHero = (HeroModule) restoredHero;
		assertEquals(2, castedHero.getHeroCount());
		assertEquals(List.of("Hero_1001", "Hero_1002"), castedHero.getHeroes());

		TestBaseModule restoredBag = restored.get("bag");
		assertInstanceOf(BagModule.class, restoredBag);
		BagModule castedBag = (BagModule) restoredBag;
		assertEquals(500, castedBag.getCapacity());
		assertEquals(99L, castedBag.getItems().get(101));
		assertEquals(1000L, castedBag.getItems().get(102));
	}

	@Test
	@DisplayName("BitSet 与时间类型序列化测试")
	public void testBitSetAndTime() {
		CompositeData composite = new CompositeData();
		composite.id = "player_123456";
		composite.bitSet = new BitSet();
		composite.bitSet.set(0);
		composite.bitSet.set(7);
		composite.bitSet.set(64);
		composite.bitSet.set(127);
		composite.time = LocalDateTime.of(2026, 11, 2, 12, 30, 45);

		byte[] compressed = CborSnappyUtil.compress(composite);
		assertNotNull(compressed);

		CompositeData restored = CborSnappyUtil.decompress(compressed, CompositeData.class);
		assertNotNull(restored);
		assertEquals("player_123456", restored.id);
		assertNotNull(restored.bitSet);
		assertTrue(restored.bitSet.get(0));
		assertTrue(restored.bitSet.get(7));
		assertTrue(restored.bitSet.get(64));
		assertTrue(restored.bitSet.get(127));
		assertFalse(restored.bitSet.get(1));
		assertFalse(restored.bitSet.get(63));
		assertEquals(LocalDateTime.of(2026, 11, 2, 12, 30, 45), restored.time);
	}

	@Test
	@DisplayName("高压缩率验证测试：对比 JSON 与 CBOR+Snappy")
	public void testCompressionRatio() {
		Map<String, Object> data = new HashMap<>();
		for (int i = 0; i < 2000; i++) {
			Map<String, Object> subMap = new HashMap<>();
			subMap.put("id", i);
			subMap.put("name", "Item_" + i);
			subMap.put("desc", "This is an item description for inventory testing and compression measurement.");
			subMap.put("count", 100);
			subMap.put("time", "2026-11-02 02:30:00");
			data.put("module_" + i, subMap);
		}

		String json = JsonUtil.toJsonStringWithType(data);
		byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

		byte[] compressed = CborSnappyUtil.compress(data);
		assertNotNull(compressed);

		// 压缩后体积应至少小于原始 JSON 体积的 35%（即压缩率超过 65%）
		double ratio = (double) compressed.length / jsonBytes.length;
		System.out.printf("JSON bytes: %d, CBOR+Snappy bytes: %d, Ratio: %.2f%%%n",
				jsonBytes.length, compressed.length, ratio * 100);
		assertTrue(ratio < 0.35, "Compressed size ratio should be < 35%, got " + ratio);

		Map<String, Object> restored = CborSnappyUtil.decompress(compressed);
		assertEquals(2000, restored.size());
	}
}