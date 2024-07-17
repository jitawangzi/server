package cn.game.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

import cn.game.util.reflect.BeanMap;;

/**
 * 动态代理实现动态赋值
 * 
 * @ClassName: PbBeanUtil
 * @Description: TODO
 * @author Melon
 * 2013-12-13 下午7:19:11
 */
public class PbBeanUtil {

	private static Lock lock = new ReentrantLock();

//	public static PbBeanUtil getInstance() {
//		if (beanUtil == null) {
//			beanUtil = new PbBeanUtil();
//		}
//		return beanUtil;
//	}
	private PbBeanUtil(){} ; 

	private static  Map<String, Descriptor> descriptorMap = new ConcurrentHashMap<String, Descriptor>();

	private static Descriptor regist(Message message) {
		if (message == null) {
			return null;
		}
		Descriptor descriptor = message.getDescriptorForType();
		String key = message.getClass().getName();
		lock.lock();
		try {
			descriptorMap.put(key, descriptor);
		} finally {
			lock.unlock();
		}
		return descriptor;
	}
	
	/**
	 * 检测缓存中是否存在这个消息的Descriptor
	 * 
	 * @param key
	 * @return
	 */
	private static boolean detectDescriptor(String key) {
		if (descriptorMap.get(key) != null) {
			return true;
		}
		return false;
	}

	/**
	 * 根据key，读取缓存中的Descriptor
	 * 
	 * @param name
	 * @return
	 */
	private static Descriptor findDescriptorByName(String name) {
		return descriptorMap.get(name);
	}

	private static String getKeyByMsg(Message msg) {
		return msg.getClass().getName();
	}

	/**
	 * @Description 对简单Builder的赋值
	 * @param source
	 * @param builder
	 */
	public static  void copy(Object source, Message.Builder builder) {
		BeanMap beanMap = new BeanMap(source) ; 
		Descriptor out = builder.getDescriptorForType();
		List<FieldDescriptor> fieldDescriptors = out.getFields();

		for (int i = 0; i < fieldDescriptors.size(); i++) {
			// 如果是集合字段
			if (!fieldDescriptors.get(i).getJavaType().equals(JavaType.MESSAGE)
					&& !fieldDescriptors.get(i).isRepeated()) {
				// 如果pojo中有这个属性，而且值不为null;
				if (beanMap.containsKey(fieldDescriptors.get(i).getName())
						&& beanMap.get(fieldDescriptors.get(i).getName()) != null) {
					builder.setField(fieldDescriptors.get(i), beanMap.get(fieldDescriptors.get(i).getName()));
				}
			}
		}
		return;

	}

	/**
	 * @Description 对消息（Message赋值）
	 * @param source
	 * @param message   一个消息的默认实例
	 * @param key
	 * @return
	 */
	public static  <T extends Message> T copy(Object source, T message, String key) {

		Builder builder = message.newBuilderForType();
		BeanMap beanMap = new BeanMap(source);
		if (key == null || "".equals(key)) {
			key = getKeyByMsg(message);
		}
		Descriptor out = null;
		if (!detectDescriptor(key)) {
			out = regist(message);
		} else {
			out = findDescriptorByName(key);
		}
		List<FieldDescriptor> fieldDescriptors = out.getFields();
		for (int i = 0; i < fieldDescriptors.size(); i++) {
			if (fieldDescriptors.get(i).getJavaType().equals(JavaType.MESSAGE)) { // 集合类型
				if (fieldDescriptors.get(i).isRepeated()) {// 检查是否存在这个key和值
					if (detectKeyFromBeanMap(beanMap, fieldDescriptors.get(i).getName())) {
						if (beanMap.get(fieldDescriptors.get(i).getName()) instanceof java.util.List) {
							@SuppressWarnings("rawtypes")
							List children = (List) beanMap.get(fieldDescriptors.get(i).getName());
							if (!children.isEmpty()) {
								Message.Builder meBuilder = DynamicMessage
										.newBuilder(fieldDescriptors.get(i).getMessageType());
								for (int j = 0; j < children.size(); j++) {
									Message msg = setField(children.get(j), meBuilder.getDefaultInstanceForType(),
											fieldDescriptors.get(i).getFullName());
									builder.addRepeatedField(fieldDescriptors.get(i), msg);
								}
							}
						}
					}
				} else {// 单个消息类型
						// 检查是否存在这个key和值
					if (detectKeyFromBeanMap(beanMap, fieldDescriptors.get(i).getName())) {
						Message.Builder meBuilder = DynamicMessage.newBuilder(fieldDescriptors.get(i).getMessageType());
						Message msg = setField(beanMap.get(fieldDescriptors.get(i).getName()),
								meBuilder.getDefaultInstanceForType(), fieldDescriptors.get(i).getFullName());
						builder.setField(fieldDescriptors.get(i), msg);
					}
				}
			} else if (fieldDescriptors.get(i).isRepeated()) {
				// 检查是否存在这个key和值
				if (detectKeyFromBeanMap(beanMap, fieldDescriptors.get(i).getName())) {
					List list = (List) beanMap.get(fieldDescriptors.get(i).getName());
					for (int j = 0; j < list.size(); j++) {
						builder.addRepeatedField(fieldDescriptors.get(i), list.get(j));
					}
				}
			} else {
				// 基本类型
				setFieldVal(builder, beanMap, fieldDescriptors.get(i));
			}
		}
		return (T) builder.build();

	}

	public static <T extends Message> T copy(Object source, T message) {
		return copy(source, message, null);
	}

	/**
	 * 设置某个属性的值
	 * 
	 * @param builder
	 * @param beanMap
	 * @param fieldDescriptor
	 */
	private static void setFieldVal(Builder builder, BeanMap beanMap, FieldDescriptor fieldDescriptor) {
		// 检查是否存在这个key和值
		if (detectKeyFromBeanMap(beanMap, fieldDescriptor.getName())) {
			// beanMap.get(fieldDescriptor.getName());
			/*
			 * if(fieldDescriptor.getJavaType().equals(JavaType.MESSAGE)) {
			 * if(fieldDescriptor.isRepeated()){
			 * 
			 * } }
			 */
			builder.setField(fieldDescriptor, beanMap.get(fieldDescriptor.getName()));
		}
	}

	/**
	 * 对一个不包含repeated和其他Message的对象进行动态赋值
	 * 
	 * @param source
	 * @param message
	 * @return
	 */
	private static Message setField(Object source, Message message, String key) {
		Builder builder = message.newBuilderForType();
		BeanMap beanMap = new BeanMap(source);
		Descriptor out = null;
		if (!detectDescriptor(key)) {
			out = regist(message);
		} else {
			out = findDescriptorByName(key);
		}
		List<FieldDescriptor> fieldDescriptors = out.getFields();
		for (int i = 0; i < fieldDescriptors.size(); i++) {
			/*
			 * if(fieldDescriptors.get(i).getJavaType().equals(JavaType.MESSAGE)
			 * ) { Message
			 * dynamicMsg=DynamicMessage.getDefaultInstance(fieldDescriptors.get
			 * (i).getMessageType());
			 * dynamicMsg=copy(beanMap.get(fieldDescriptors.get(i).getName(
			 * )), dynamicMsg,fieldDescriptors.get(i).getFullName());
			 * builder.setField(fieldDescriptors.get(i), dynamicMsg); }
			 */
			setFieldVal(builder, beanMap, fieldDescriptors.get(i));
		}
		return builder.build();
	}

	/**
	 * 检查key是否存在于beanMap中，并有值
	 * 
	 * @param beanMap
	 * @param key
	 * @return
	 */
	private static boolean detectKeyFromBeanMap(BeanMap beanMap, String key) {
		if (beanMap.containsKey(key) && beanMap.get(key) != null) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {

		// CustomsCfg customsCfg=new CustomsCfg();
		// final int crystalVal=10;
		// customsCfg.setCrystal(crystalVal);
		// CustomsBaseCfg customsBaseCfg=new CustomsBaseCfg();
		// final int customsId=5;
		// customsBaseCfg.setCustomsId(customsId);
		// customsCfg.setBaseCfg(customsBaseCfg);
		//

		// CustomsCfgPro.Builder testBuild=CustomsCfgPro.newBuilder();
		// CustomsCfgPro cfgPro=CustomsCfgPro.getDefaultInstance();
		// cfgPro =(CustomsCfgPro)BeanUtil.getInstance().copy(customsCfg,
		// cfgPro);
		// System.out.println(cfgPro.getBaseCfg().getCustomsId());
		// System.exit(0);
	}
}
