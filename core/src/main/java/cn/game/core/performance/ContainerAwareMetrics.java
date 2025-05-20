package cn.game.core.performance;

public class ContainerAwareMetrics {
	private static final long MEM_LIMIT = readMemoryLimit();
	private static final int CPU_LIMIT = readCpuLimit();

	private static long readMemoryLimit() {
		String limit = System.getenv("CONTAINER_MEM_LIMIT");
		return limit != null ? Long.parseLong(limit) : -1;
	}

	private static int readCpuLimit() {
		String quota = System.getenv("CONTAINER_CPU_QUOTA");
		return quota != null ? Integer.parseInt(quota) : Runtime.getRuntime().availableProcessors();
	}

	public static double getContainerCpuUsage(double rawUsage) {
		return CPU_LIMIT > 0 ? rawUsage / CPU_LIMIT : rawUsage;
	}

	public static double getContainerMemUsage(long used) {
		return MEM_LIMIT > 0 ? used / (double) MEM_LIMIT : -1;
	}
}