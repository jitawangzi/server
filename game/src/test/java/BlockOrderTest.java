import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**    
 * 同一个线程提交的阻塞任务，如果是有序，则在同一个线程执行。 
 * 如果不同线程提交的阻塞任务，如果是有序，也是在各自的线程顺序执行。 
 * 2024年4月7日 下午5:49:47
 * @author SYQ
 */
public class BlockOrderTest {

	
	public static void main(String[] args) throws Exception {
		Vertx vertx = Vertx.vertx();

		Supplier<Integer> s1 = () -> {
			System.out.println("s1 开始执行 : " + Thread.currentThread().getName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("s1 执行结束" + Thread.currentThread().getName());
			return 1;
		};
		Supplier<Integer> s2 = () -> {
			System.out.println("s2 开始执行" + Thread.currentThread().getName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("s2 执行结束" + Thread.currentThread().getName());
			return 2;
		};
		Supplier<Integer> s3 = () -> {
			System.out.println("s3 开始执行" + Thread.currentThread().getName());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("s3 执行结束" + Thread.currentThread().getName());
			return 3;
		};

		Supplier<Integer> s4 = () -> {
			System.out.println("s4 开始执行 : " + Thread.currentThread().getName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("s4 执行结束" + Thread.currentThread().getName());
			return 4;
		};
		Supplier<Integer> s5 = () -> {
			System.out.println("s5 开始执行" + Thread.currentThread().getName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("s5 执行结束" + Thread.currentThread().getName());
			return 5;
		};
		Supplier<Integer> s6 = () -> {
			System.out.println("s6 开始执行" + Thread.currentThread().getName());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("s6 执行结束" + Thread.currentThread().getName());
			return 6;
		};

		List<Supplier<Integer>> list = new ArrayList<>();
		list.add(s1);
		list.add(s2);
		list.add(s3);
		List<Supplier<Integer>> list2 = new ArrayList<>();
		list2.add(s4);
		list2.add(s5);
		list2.add(s6);

		Future<List<Integer>> future = vertx.executeBlocking(() -> {
			List<Integer> ret = new ArrayList<>();

			for (Supplier<Integer> supplier : list) {
				ret.add(supplier.get());
			}
			return ret;
		}, true);

		new Thread(() -> {
			List<Integer> ret = new ArrayList<>();
			for (Supplier<Integer> supplier : list2) {
				vertx.executeBlocking(() -> {
					ret.add(supplier.get());
//					}
					return ret;
				}, true);
			}
			System.out.println("结束" + Thread.currentThread().getName());

		}).start();

		/*		List<Integer> ret = new ArrayList<>();
		//		for (Supplier<Integer> supplier : list2) {
					vertx.executeBlocking(promise -> {
					for (Supplier<Integer> supplier : list2) {
		//				System.out.println("aaa: " + Thread.currentThread().getName());
						ret.add(supplier.get());
					}
						promise.complete(ret);
					}, true);
		//		}
		*/
		Thread.currentThread().join();
	}

}
