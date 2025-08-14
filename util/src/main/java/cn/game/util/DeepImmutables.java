package cn.game.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 深不可变工具：
 * - 递归将 Map/Collection/数组 转为不可变拷贝（Guava Immutable*）
 * - 保持输入的迭代顺序
 * - 不转换 Map 的 key，仅深转换 value
 * - 不允许 null（键/值/元素），遇到则抛出 NPE
 * - 检测循环引用，遇到则抛出 IllegalArgumentException
 */
public final class DeepImmutables {

	private DeepImmutables() {
	}

	public static <K, V> Map<K, V> map(Map<? extends K, ? extends V> input) {
		IdentityHashMap<Object, Object> seen = new IdentityHashMap<>();
		return mapInternal(input, seen);
	}

	public static <E> List<E> list(List<? extends E> input) {
		IdentityHashMap<Object, Object> seen = new IdentityHashMap<>();
		return listInternal(input, seen);
	}

	public static <E> Set<E> set(Set<? extends E> input) {
		IdentityHashMap<Object, Object> seen = new IdentityHashMap<>();
		return setInternal(input, seen);
	}

	public static <E> List<E> collection(Collection<? extends E> input) {
		IdentityHashMap<Object, Object> seen = new IdentityHashMap<>();
		return collectionInternal(input, seen);
	}

	/**
	 * 通用入口：对 Map/Collection/数组 递归深不可变化；其他类型原样返回。
	 */
	@SuppressWarnings("unchecked")
	public static <T> T any(T input) {
		if (input == null)
			return null;
		IdentityHashMap<Object, Object> seen = new IdentityHashMap<>();
		return (T) deepConvert(input, seen);
	}

	// ---------------- internal ----------------

	private static <K, V> Map<K, V> mapInternal(Map<? extends K, ? extends V> input, IdentityHashMap<Object, Object> seen) {
		if (input == null || input.isEmpty()) {
			return ImmutableMap.of();
		}
		detectCycleStart(input, seen, "Map");

		ImmutableMap.Builder<K, V> builder = ImmutableMap.builderWithExpectedSize(input.size());
		for (Map.Entry<? extends K, ? extends V> e : input.entrySet()) {
			K k = e.getKey();
			if (k == null) {
				throw new NullPointerException("Null key encountered in Map during deep immutability conversion");
			}
			V v = e.getValue();
			if (v == null) {
				throw new NullPointerException("Null value encountered in Map during deep immutability conversion");
			}
			@SuppressWarnings("unchecked")
			V converted = (V) deepConvert(v, seen); // 保持 V 的编译期类型（例如 List<T>/Map<K,V>）
			builder.put(k, converted);
		}
		Map<K, V> out = builder.build();
		detectCycleEnd(input, seen);
		return out;
	}

	private static <E> List<E> listInternal(List<? extends E> input, IdentityHashMap<Object, Object> seen) {
		if (input == null || input.isEmpty()) {
			return ImmutableList.of();
		}
		detectCycleStart(input, seen, "List");

		ImmutableList.Builder<E> builder = ImmutableList.builderWithExpectedSize(input.size());
		for (E e : input) {
			if (e == null) {
				throw new NullPointerException("Null element encountered in List during deep immutability conversion");
			}
			@SuppressWarnings("unchecked")
			E converted = (E) deepConvert(e, seen);
			builder.add(converted);
		}
		List<E> out = builder.build();
		detectCycleEnd(input, seen);
		return out;
	}

	private static <E> Set<E> setInternal(Set<? extends E> input, IdentityHashMap<Object, Object> seen) {
		if (input == null || input.isEmpty()) {
			return ImmutableSet.of();
		}
		detectCycleStart(input, seen, "Set");

		ImmutableSet.Builder<E> builder = ImmutableSet.builderWithExpectedSize(input.size());
		for (E e : input) {
			if (e == null) {
				throw new NullPointerException("Null element encountered in Set during deep immutability conversion");
			}
			@SuppressWarnings("unchecked")
			E converted = (E) deepConvert(e, seen);
			builder.add(converted);
		}
		Set<E> out = builder.build();
		detectCycleEnd(input, seen);
		return out;
	}

	private static <E> List<E> collectionInternal(Collection<? extends E> input, IdentityHashMap<Object, Object> seen) {
		if (input == null || input.isEmpty()) {
			return ImmutableList.of();
		}
		detectCycleStart(input, seen, "Collection");

		ImmutableList.Builder<E> builder = ImmutableList.builderWithExpectedSize(input.size());
		for (E e : input) {
			if (e == null) {
				throw new NullPointerException("Null element encountered in Collection during deep immutability conversion");
			}
			@SuppressWarnings("unchecked")
			E converted = (E) deepConvert(e, seen);
			builder.add(converted);
		}
		List<E> out = builder.build();
		detectCycleEnd(input, seen);
		return out;
	}

	@SuppressWarnings("unchecked")
	private static Object deepConvert(Object value, IdentityHashMap<Object, Object> seen) {
		if (value == null)
			return null; // 上层会拦截 null，这里留作安全网

		if (value instanceof Map<?, ?>) {
			return mapInternal((Map<?, ?>) value, seen);
		}
		if (value instanceof List<?>) {
			return listInternal((List<?>) value, seen);
		}
		if (value instanceof Set<?>) {
			return setInternal((Set<?>) value, seen);
		}
		if (value instanceof Collection<?>) {
			return collectionInternal((Collection<?>) value, seen);
		}
		Class<?> cls = value.getClass();
		if (cls.isArray()) {
			return arrayToImmutableList(value, seen);
		}
		// 非容器类型：按值传回（不可变性的责任由类型自身承担）
		return value;
	}

	@SuppressWarnings("unchecked")
	private static <E> List<E> arrayToImmutableList(Object array, IdentityHashMap<Object, Object> seen) {
		int len = Array.getLength(array);
		ImmutableList.Builder<E> builder = ImmutableList.builderWithExpectedSize(len);
		for (int i = 0; i < len; i++) {
			Object elem = Array.get(array, i);
			if (elem == null) {
				throw new NullPointerException("Null element encountered in array during deep immutability conversion");
			}
			E converted = (E) deepConvert(elem, seen);
			builder.add(converted);
		}
		return builder.build();
	}

	private static void detectCycleStart(Object obj, IdentityHashMap<Object, Object> seen, String kind) {
		if (seen.put(obj, Boolean.TRUE) != null) {
			throw new IllegalArgumentException("Cyclic reference detected while converting " + kind + " to deep immutable");
		}
	}

	private static void detectCycleEnd(Object obj, IdentityHashMap<Object, Object> seen) {
		seen.remove(obj);
	}
}