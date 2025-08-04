package cn.game.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.VersionFieldSerializer;
import com.google.protobuf.Message;

import cn.game.util.reflect.ClassHelper;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import de.javakaffee.kryoserializers.guava.ArrayListMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ArrayTableSerializer;
import de.javakaffee.kryoserializers.guava.HashBasedTableSerializer;
import de.javakaffee.kryoserializers.guava.HashMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableListSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;
import de.javakaffee.kryoserializers.guava.ImmutableTableSerializer;
import de.javakaffee.kryoserializers.guava.LinkedHashMultimapSerializer;
import de.javakaffee.kryoserializers.guava.LinkedListMultimapSerializer;
import de.javakaffee.kryoserializers.guava.ReverseListSerializer;
import de.javakaffee.kryoserializers.guava.TreeBasedTableSerializer;
import de.javakaffee.kryoserializers.guava.TreeMultimapSerializer;
import de.javakaffee.kryoserializers.guava.UnmodifiableNavigableSetSerializer;
import de.javakaffee.kryoserializers.protobuf.ProtobufSerializer;

/**
 * 代替protostuff，不要序列化匿名类
 * 支持JDK 21虚拟线程的Kryo工具类
 */
public class KryoUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(KryoUtils.class);

	// 为普通线程使用ThreadLocal
	private static final ThreadLocal<Kryo> standardKryoThreadLocal = ThreadLocal.withInitial(() -> {
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
		kryo.setReferences(true);
		registerSerializer(kryo);
		return kryo;
	});

	private static final ThreadLocal<Kryo> versionedKryoThreadLocal = ThreadLocal.withInitial(() -> {
		Kryo kryo = new Kryo();
		kryo.setRegistrationRequired(false);
		kryo.setReferences(true);
		kryo.setDefaultSerializer(VersionFieldSerializer.class);
		registerSerializer(kryo);
		return kryo;
	});

	// 为虚拟线程使用对象池
	private static final KryoPool standardKryoPool = new KryoPool(false);
	private static final KryoPool versionedKryoPool = new KryoPool(true);

	public static void init() {
		
		
	}
	
	/**
	 * Kryo 对象池实现
	 */
	private static class KryoPool {
		private final BlockingQueue<Kryo> pool;
		private final boolean withVersion;
		private final AtomicInteger created = new AtomicInteger(0);
		private final AtomicInteger borrowed = new AtomicInteger(0);
		private final AtomicInteger discarded = new AtomicInteger(0);
		private final int maxSize;

		public KryoPool(boolean withVersion) {
			// 默认池大小为处理器核心数的2倍
			int coreSize = Runtime.getRuntime().availableProcessors();
			int initialSize = coreSize;
			this.maxSize = coreSize * 4; // 最大大小为核心数的4倍
			this.withVersion = withVersion;
			pool = new ArrayBlockingQueue<>(maxSize);

			// 预热池，但只创建初始大小的实例
			for (int i = 0; i < initialSize; i++) {
				Kryo kryo = createKryo();
				pool.offer(kryo);
				created.incrementAndGet();
			}
		}

		private Kryo createKryo() {
			Kryo kryo = new Kryo();
			kryo.setRegistrationRequired(false);
			kryo.setReferences(true);

			if (withVersion) {
				kryo.setDefaultSerializer(VersionFieldSerializer.class);
			}

			registerSerializer(kryo);
			return kryo;
		}

		public Kryo borrow() {
			Kryo kryo = pool.poll(); // 非阻塞获取
			if (kryo == null) {
				// 如果已创建数量小于最大大小，创建新实例
				if (created.get() < maxSize) {
					kryo = createKryo();
					created.incrementAndGet();
				} else {
					// 尝试等待短时间
					try {
						kryo = pool.poll(50, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}

					// 如果仍然无法获取，创建一个临时实例
					if (kryo == null) {
						kryo = createKryo();
						// 不计入created数量，因为这是临时实例
					}
				}
			}
			borrowed.incrementAndGet();
			return kryo;
		}

		public void release(Kryo kryo) {
			if (kryo != null) {
				borrowed.decrementAndGet();
				// 尝试放回池中，如果池满则丢弃
				boolean returned = pool.offer(kryo);
				if (!returned) {
					discarded.incrementAndGet();
				}
			}
		}

		public String getStats() {
			return String.format("KryoPool[withVersion=%s, created=%d, borrowed=%d, available=%d, discarded=%d, maxSize=%d]", withVersion,
					created.get(), borrowed.get(), pool.size(), discarded.get(), maxSize);
		}
	}
	/**
	 * 判断当前是否在虚拟线程中执行
	 */
	private static boolean isVirtualThread() {
		return Thread.currentThread().isVirtual();
	}

	/**
	 * 获取标准Kryo实例
	 */
	private static Kryo getStandardKryo() {
		return isVirtualThread() ? standardKryoPool.borrow() : standardKryoThreadLocal.get();
	}

	/**
	 * 释放标准Kryo实例
	 */
	private static void releaseStandardKryo(Kryo kryo) {
		if (isVirtualThread()) {
			standardKryoPool.release(kryo);
		}
	}

	/**
	 * 获取带版本的Kryo实例
	 */
	private static Kryo getVersionedKryo() {
		return isVirtualThread() ? versionedKryoPool.borrow() : versionedKryoThreadLocal.get();
	}

	/**
	 * 释放带版本的Kryo实例
	 */
	private static void releaseVersionedKryo(Kryo kryo) {
		if (isVirtualThread()) {
			versionedKryoPool.release(kryo);
		}
	}

	/**
	 * 把指定对象序列化成字节数组，反序列化时不能修改对象
	 */
	public static <T> byte[] serialize(T obj) {
		Kryo kryo = getStandardKryo();
		try {
			Output output = new Output(32, -1);
			kryo.writeObject(output, obj);
			return output.getBuffer();
		} finally {
			releaseStandardKryo(kryo);
		}
	}

	public static byte[] serializeClassAndObject(Object obj) {
		Kryo kryo = getStandardKryo();
		try {
			Output output = new Output(32, -1);
			kryo.writeClassAndObject(output, obj);
			return output.getBuffer();
		} finally {
			releaseStandardKryo(kryo);
		}
	}

	public static byte[] serializeClassAndObjectWithVersion(Object obj) {
		Kryo kryo = getVersionedKryo();
		try {
			Output output = new Output(32, -1);
			kryo.writeClassAndObject(output, obj);
			return output.getBuffer();
		} finally {
			releaseVersionedKryo(kryo);
		}
	}

	/**
	 * 将字节数组反序列化成指定Class类型，反序列化时不能修改对象
	 */
	public static <T> T deserialize(byte[] data, Class<T> clazz) {
		Kryo kryo = getStandardKryo();
		try {
			Input input = new Input(data);
			return kryo.readObject(input, clazz);
		} finally {
			releaseStandardKryo(kryo);
		}
	}

	public static Object deserializeClassAndObject(byte[] data) {
		Kryo kryo = getStandardKryo();
		try {
			Input input = new Input(data);
			return kryo.readClassAndObject(input);
		} finally {
			releaseStandardKryo(kryo);
		}
	}

	public static Object deserializeClassAndObjectWithVersion(byte[] data) {
		Kryo kryo = getVersionedKryo();
		try {
			Input input = new Input(data);
			return kryo.readClassAndObject(input);
		} finally {
			releaseVersionedKryo(kryo);
		}
	}

	/**
	 * 把指定对象序列化成字节数组,带对象版本，不支持对象属性名更改、删除，可以新增
	 */
	public static <T> byte[] serializeWithVersion(T obj) {
		Kryo kryo = getVersionedKryo();
		try {
			Output output = new Output(32, -1);
			kryo.writeObject(output, obj);
			return output.getBuffer();
		} finally {
			releaseVersionedKryo(kryo);
		}
	}

	/**
	 * 将字节数组反序列化成指定Class类型,如果对象新增了字段，需要加@Since注解，value
	 * 需要是最新的版本
	 */
	public static <T> T deserializeWithVersion(byte[] data, Class<T> clazz) {
		Kryo kryo = getVersionedKryo();
		try {
			Input input = new Input(data);
			return kryo.readObject(input, clazz);
		} finally {
			releaseVersionedKryo(kryo);
		}
	}

	/**
	 * 获取池状态信息，用于监控
	 */
	public static String getPoolStats() {
		return "Standard: " + standardKryoPool.getStats() + "\nVersioned: " + versionedKryoPool.getStats();
	}

	// 注册序列化器
	private static void registerSerializer(Kryo kryo) {
		kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
		kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
		kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
		kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
		kryo.register(Collections.singletonList("").getClass(), new CollectionsSingletonListSerializer());
		kryo.register(Collections.singleton("").getClass(), new CollectionsSingletonSetSerializer());
		kryo.register(Collections.singletonMap("", "").getClass(), new CollectionsSingletonMapSerializer());
		kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
		kryo.register(InvocationHandler.class, new JdkProxySerializer());
		UnmodifiableCollectionsSerializer.registerSerializers(kryo);
		SynchronizedCollectionsSerializer.registerSerializers(kryo);
		
		// custom serializers for non-jdk libs
		ImmutableListSerializer.registerSerializers(kryo);
		ImmutableSetSerializer.registerSerializers(kryo);
		ImmutableMapSerializer.registerSerializers(kryo);
		ImmutableMultimapSerializer.registerSerializers(kryo);
		ImmutableTableSerializer.registerSerializers(kryo);
		ReverseListSerializer.registerSerializers(kryo);
		UnmodifiableNavigableSetSerializer.registerSerializers(kryo);
		ArrayListMultimapSerializer.registerSerializers(kryo);
		HashMultimapSerializer.registerSerializers(kryo);
		LinkedHashMultimapSerializer.registerSerializers(kryo);
		LinkedListMultimapSerializer.registerSerializers(kryo);
		TreeMultimapSerializer.registerSerializers(kryo);
		ArrayTableSerializer.registerSerializers(kryo);
		HashBasedTableSerializer.registerSerializers(kryo);
		TreeBasedTableSerializer.registerSerializers(kryo);
		
		// protobuf
		registerProtobufSerializers(kryo);
	}
	
	   // 使用Spring扫描并批量注册protobuf类
    private static void registerProtobufSerializers(Kryo kryo) {
    	long timeMillis = System.currentTimeMillis(); 
    	
        String basePackage = "cn.game.protocol.protobuf"; // 
        // Message的子类
        Set<Class<? extends Message>> subclasses = ClassHelper.findSubclasses(basePackage, Message.class); 
        
        Set<Class<?>> allMessageClasses = new HashSet<>();
        for (Class<? extends Message> outer : subclasses) {
            collectProtobufMessageTypes(outer, allMessageClasses, Message.class);
        }
        
        // 注意先排序，再注册，确保不同kryo实例，类的注册顺序是一致的
        List<Class<?>> sorted = new ArrayList<>(allMessageClasses);
        sorted.sort(Comparator.comparing(Class::getName));

        for (Class<?> clazz : sorted) {
            kryo.register(clazz, new ProtobufSerializer());
        }
        LOGGER.debug("注册protobuf类 耗时：{} ms, 共注册类数：{}",(System.currentTimeMillis() - timeMillis),allMessageClasses.size());
    }
    private static void collectProtobufMessageTypes(Class<?> clazz, Set<Class<?>> result, Class<?> messageSuperClass) {
        // 只收集非接口、非抽象、且 Message 类型
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
                && messageSuperClass.isAssignableFrom(clazz)) {
            result.add(clazz);
        }
        // 递归收集该类的所有 public static 内部类
        for (Class<?> inner : clazz.getDeclaredClasses()) {
            // 只收集 public static
            int mod = inner.getModifiers();
            if (Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
                collectProtobufMessageTypes(inner, result, messageSuperClass);
            }
        }
    }
    
}