package cn.game.core.async;


/**    
 * 阻塞逻辑接口，可以代替jdk的Supplier，方便抛出异常，不用再try catch
 * 2024年11月6日 11:23:23
 * @author SYQ
 */
@FunctionalInterface
public interface BlockingCode<T> {
	T execute() throws Throwable;
}
