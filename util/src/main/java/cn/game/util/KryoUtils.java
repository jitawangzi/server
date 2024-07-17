package cn.game.util;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.VersionFieldSerializer;

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

/**
 * @Description 代替protostuff，不要序列化匿名类
 * 2020年11月13日 下午4:48:30
 * @author SYQ
 */
public class KryoUtils {

	private static final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {

		@Override
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			// configure kryo instance, customize settings
			kryo.setRegistrationRequired(false); // 关闭注册行为
			kryo.setReferences(true); // 支持循环引用,关闭可以提升性能
			registerSerializer(kryo);
//			kryo.register(ArrayListMultimap.class, new JavaSerializer());
//			kryo.register(ArrayListMultimap.class, new ArrayListMultimapSerializer());

//			kryo.register(Object[].class);
//			kryo.register(Class.class);
//			kryo.register(SerializedLambda.class);
//			kryo.register(ClosureSerializer.Closure.class, new ClosureSerializer());
//			kryo.register(CapturingClass.class);

			//Fix the NPE bug when deserializing Collections. ? 
//			((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(
//					new StdInstantiatorStrategy());
			return kryo;
		};
	};
	private static final ThreadLocal<Kryo> kryosWithVersion = new ThreadLocal<Kryo>() {

		@Override
		protected Kryo initialValue() {
//			com.esotericsoftware.minlog.Log.set(1);
			Kryo kryo = new Kryo();
			// configure kryo instance, customize settings
			kryo.setRegistrationRequired(false); // 关闭注册行为
			kryo.setReferences(true); // 支持循环引用,关闭可以提升性能

			// 序列化增加版本控制
			kryo.setDefaultSerializer(VersionFieldSerializer.class);
			registerSerializer(kryo);
//			kryo.register(ArrayListMultimap.class, new JavaSerializer());
//			kryo.register(ArrayListMultimap.class, new ArrayListMultimapSerializer());
			//Fix the NPE bug when deserializing Collections. ? 
//			((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(
//					new StdInstantiatorStrategy());
			return kryo;
		};
	};

	/**
	 * @Description 把指定对象序列化成字节数组，反序列化时不能修改对象
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> byte[] serialize(T obj) {
		Output output = new Output(32, -1);
		kryos.get().writeObject(output, obj);
//		output.close();
		return output.getBuffer();
	}
	public static byte[] serializeClassAndObject(Object obj) {
		Output output = new Output(32, -1);
		kryos.get().writeClassAndObject(output, obj);
//		output.close();
		return output.getBuffer();
	}
	public static byte[] serializeClassAndObjectWithVersion(Object obj) {
		Output output = new Output(32, -1);
		kryosWithVersion.get().writeClassAndObject(output, obj);
//		output.close();
		return output.getBuffer();
	}

	/**
	 * @Description 将字节数组反序列化成指定Class类型，反序列化时不能修改对象
	 * @param data
	 * @param clazz
	 * @return
	 */
	public static <T> T deserialize(byte[] data, Class<T> clazz) {

		Input input = new Input(data);
		return kryos.get().readObject(input, clazz);
	}
	public static Object deserializeClassAndObject(byte[] data) {

		Input input = new Input(data);
		return kryos.get().readClassAndObject(input);
	}
	public static Object deserializeClassAndObjectWithVersion(byte[] data) {

		Input input = new Input(data);
		return kryosWithVersion.get().readClassAndObject(input);
	}
	/**
	 * @Description 把指定对象序列化成字节数组,带对象版本，不支持对象属性名更改、删除，可以新增
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> byte[] serializeWithVersion(T obj) {
		Output output = new Output(32, -1);
		kryosWithVersion.get().writeObject(output, obj);
		return output.getBuffer();
	}

	/**
	 * @Description 将字节数组反序列化成指定Class类型,如果对象新增了字段，需要加@Since注解，value
	 *              需要是最新的版本
	 * @param data
	 * @param clazz
	 * @return
	 */
	public static <T> T deserializeWithVersion(byte[] data, Class<T> clazz) {

		Input input = new Input(data);
		return kryosWithVersion.get().readObject(input, clazz);
	}
	public static void main(String[] args) throws Exception {

		Kryo kryo = new Kryo();
		// configure kryo instance, customize settings
		kryo.setRegistrationRequired(false); // 关闭注册行为
		kryo.setReferences(true); // 支持循环引用,关闭可以提升性能

//		kryo.register(CompositeFutureImpl.class);
//		kryo.register(CompositeFutureImpl.class);
//		kryo.register(Class.class);
//		kryo.register(SerializedLambda.class);
//		kryo.register(ClosureSerializer.Closure.class, new ClosureSerializer());
//		Callable<Integer> closure1 = (Callable<Integer> & java.io.Serializable) (() -> 72363);

//		Promise<Long> promise = Promise.promise();
//	    Future<Long> ff = promise.future();

//		byte[] respDatas = KryoUtils.serialize(resp);
//		System.out.println(respDatas);

//		Promise<Long> promise2 = Promise.promise();
//		Future<Long> ff2 = promise.future();

//		Future<Long> map = ff.map(r -> r + 100);
//		
//		promise.complete(256L);
//
//		Promise<Long> ppp = Promise.promise();
//		ppp.complete(map.result());


		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add("1");
		arrayList.add("2");
		arrayList.add("3");

		byte[] data = serializeClassAndObject(arrayList);

		ArrayList<String> obj = (ArrayList<String>) deserializeClassAndObject(data);

		System.out.println(obj.get(0));

//		Output output = new Output(1024, -1);
//		kryo.writeObject(output, result);

//		Input input = new Input(output.getBuffer(), 0, output.position());
//		Result r2 = kryo.readObject(input, Result.class);
//		Future<Long> closure2 = (Future<Long>) r2.getResult();
//		Callable<Integer> closure2 = (Callable<Integer>) kryo.readObject(input, ClosureSerializer.Closure.class);
//		System.out.println(closure2.result());
	}
	
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

		// register CGLibProxySerializer, works in combination with the appropriate action in handleUnregisteredClass (see below)
//		kryo.register( CGLibProxySerializer.CGLibProxyMarker.class, new CGLibProxySerializer( kryo ) );
		// dexx
//		ListSerializer.registerSerializers( kryo );
//		MapSerializer.registerSerializers( kryo );
//		SetSerializer.registerSerializers( kryo );
		// joda DateTime, LocalDate, LocalDateTime and LocalTime
//		kryo.register( DateTime.class, new JodaDateTimeSerializer() );
//		kryo.register( LocalDate.class, new JodaLocalDateSerializer() );
//		kryo.register( LocalDateTime.class, new JodaLocalDateTimeSerializer() );
//		kryo.register( LocalDateTime.class, new JodaLocalTimeSerializer() );
		// protobuf
//		kryo.register( SampleProtoA.class, new ProtobufSerializer() ); // or override Kryo.getDefaultSerializer as shown below
		// wicket
//		kryo.register( MiniMap.class, new MiniMapSerializer() );
		// guava ImmutableList, ImmutableSet, ImmutableMap, ImmutableMultimap, ImmutableTable, ReverseList, UnmodifiableNavigableSet
		ImmutableListSerializer.registerSerializers(kryo);
		ImmutableSetSerializer.registerSerializers(kryo);
		ImmutableMapSerializer.registerSerializers(kryo);
		ImmutableMultimapSerializer.registerSerializers(kryo);
		ImmutableTableSerializer.registerSerializers(kryo);
		ReverseListSerializer.registerSerializers(kryo);
		UnmodifiableNavigableSetSerializer.registerSerializers(kryo);
		// guava ArrayListMultimap, HashMultimap, LinkedHashMultimap, LinkedListMultimap, TreeMultimap, ArrayTable, HashBasedTable, TreeBasedTable
		ArrayListMultimapSerializer.registerSerializers(kryo);
		HashMultimapSerializer.registerSerializers(kryo);
		LinkedHashMultimapSerializer.registerSerializers(kryo);
		LinkedListMultimapSerializer.registerSerializers(kryo);
		TreeMultimapSerializer.registerSerializers(kryo);
		ArrayTableSerializer.registerSerializers(kryo);
		HashBasedTableSerializer.registerSerializers(kryo);
		TreeBasedTableSerializer.registerSerializers(kryo);
	}
}