package cn.game.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class UniqueValueEnum {
	private UniqueValueEnum() {
	}

	public static <T extends Enum<T>> void checkDuplicateValues(Class<T> enumClass, Function<T, Integer> valueGetter) {
		T[] constants = enumClass.getEnumConstants();
		Set<Integer> values = new HashSet<>(constants.length);

		for (T constant : constants) {
			int value = valueGetter.apply(constant);
			if (!values.add(value)) {
				throw new IllegalStateException("Duplicate value: " + value + " in enum " + enumClass.getSimpleName());
			}
		}
	}
}