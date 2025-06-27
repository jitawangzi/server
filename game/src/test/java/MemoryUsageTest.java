import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.game.core.execute.ActorMailbox;

public class MemoryUsageTest {
	public static void main(String[] args) {
		long memIncrease = measureMemoryIncrease(() -> {
			ConcurrentMap<Long, ActorMailbox> mailboxes = new ConcurrentHashMap<>();

//			int[] arr = new int[10_000_000];
			for (long i = 0; i < 100000; i++) {
				mailboxes.put((long) i, new ActorMailbox((long) i, 1000));
			}

		});
		System.out.println("内存增加：" + memIncrease + " bytes ，" + (memIncrease / (1024 * 1024)) + " MB");
	}

	private static long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	public static long measureMemoryIncrease(Runnable codeBlock) {
		long before = getUsedMemory();
		codeBlock.run();
		long after = getUsedMemory();
		return after - before;
	}
}