package cn.game.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class UniqueValueEnum {
	private static final Map<Class<?>, Set<Integer>> CLASS_VALUES = new HashMap<>();

	// 私有构造方法防止实例化
	private UniqueValueEnum() {
	}

	public static void checkDuplicateValue(Class<?> enumClass, int value) {
		Set<Integer> values = CLASS_VALUES.computeIfAbsent(enumClass, k -> new HashSet<>());
		if (!values.add(value)) {
			throw new IllegalStateException("Duplicate value: " + value + " in enum " + enumClass.getSimpleName());
		}
	}
}