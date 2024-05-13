public class Example {

	public static void main(String[] args) {
		printAllValues(EnumType1.class);
		printAllValues(EnumType2.class);
	}

	public static <T extends Enum<T>> void printAllValues(Class<T> enumClass) {
		T[] values = enumClass.getEnumConstants();
		System.out.println("All values of " + enumClass.getSimpleName() + ":");
		for (T value : values) {
			System.out.println(value);
		}
	}

	// Define multiple enum types
	public enum EnumType1 {
		VALUE1, VALUE2, VALUE3;
	}

	public enum EnumType2 {
		VALUE1, VALUE2, VALUE3;
	}
}
