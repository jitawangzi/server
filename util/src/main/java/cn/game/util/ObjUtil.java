package cn.game.util;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**   
 * 
 * 2016-6-17 下午3:05:31
 * @author SYQ
 */
public class ObjUtil {
	protected static final Logger	log			= LoggerFactory.getLogger(ObjUtil.class);

	public static void setDefaultValue(Object obj){

		Field[] fields = obj.getClass().getDeclaredFields(); 
		for (Field field : fields) {
			
			if (field.getName().equals("serialVersionUID")||field.getModifiers()==Modifier.FINAL) {
				continue ; 
			}
			try {
				field.setAccessible(true);//设置允许访问  
				Object object = field.get(obj); 
				if (object==null) {
					Type type = field.getGenericType() ; 
					if (type == String.class) {
						field.set(obj, "") ; 
					}else if (type == Double.class) {
						field.set(obj, 0.0) ; 
					}else if (type == Float.class) {
						field.set(obj, 0.0f) ; 
					}else if (type == Long.class) {
						field.set(obj, 0l) ; 
					}else if (type == Integer.class) {
						field.set(obj, 0) ; 
					}else if (type == Short.class) {
						field.set(obj, (short) 0) ; 
					}else if (type == Byte.class) {
						field.set(obj, (byte) 0) ; 
					} else if (type == Boolean.class) {
						field.set(obj, false);
					}
				}
			} catch (Exception e) {
				e.printStackTrace() ; 
			}
		
		}
	
	}
	
	public static void printObject(Object obj) {

		// 获取所有包含private的属性
		Field[] declaredFields = obj.getClass().getDeclaredFields();
		System.out.println(obj.getClass().getSimpleName());
		for (Field field : declaredFields) {
			
				
			field.setAccessible(true); // 设置为可访问
			try {
				if (field.get(obj)==null) {
					continue ; 
				}
				if (Collection.class.isAssignableFrom(field.getType())) {
					Collection collection = (Collection) field.get(obj);
					for (Object object : collection) {
						
						printObject(object);
						// sb.append(object2.toString()).append("\t") ;
					}
				} else if (field.getType().isArray()) {
					if (field.getType()==long[].class) {
						
						long[] array = (long[]) field.get(obj);
						System.out.println("\t" + field.getName() +Arrays.toString(array)); ; 

					} else if (field.getType()==int[].class) {
						int[] array = (int[]) field.get(obj);
						System.out.println("\t" + field.getName() +Arrays.toString(array)); ; 

					}else if (field.getType()==short[].class) {
						short[] array = (short[]) field.get(obj);
						System.out.println("\t" + field.getName() +Arrays.toString(array)); ; 

					}else if (field.getType()==byte[].class) {
						byte[] array = (byte[]) field.get(obj);
						System.out.println("\t" + field.getName() +Arrays.toString(array)); ; 
					}else if (field.getType()==double[].class) {
						double[] array = (double[]) field.get(obj);
						System.out.println("\t" + field.getName() +Arrays.toString(array)); ; 

					}else if (field.getType()==float[].class) {
						float[] array = (float[]) field.get(obj);
						System.out.println("\t" + field.getName() +Arrays.toString(array)); ; 

					}else {
						Object[] objects = (Object[])field.get(obj) ; 
						for (Object object : objects) {
							printObject(object) ; 
						}
					}
				}else if (field.getType()==Long.class||field.getType()==Double.class||field.getType()==Float.class||field.getType()==Integer.class||field.getType()==Short.class||field.getType()==Byte.class||field.getType()==Boolean.class||field.getType()==String.class||field.getType()==Character.class){
					System.out.println("\t" + field.getName() + "["
							+ field.get(obj));
				}else {
					System.out.println("\t" + field.getName() + "["
							+ field.get(obj));
//					printObject(field) ; 
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

	}
	
	public static void printNbObject(Object obj){
		
		StringBuffer buffer = new StringBuffer() ; 
		StringBuffer nbString = toNbString(obj, buffer,true,true,Config.oneLine); 
		if (Config.useLog) {
			log.info(nbString.toString()) ; 
		}else {
			System.out.println(nbString.toString());
		}
	
	}
	public static void printNbObject2Line(Object obj){

		StringBuffer buffer = new StringBuffer();
		StringBuffer nbString = toNbString(obj, buffer, true, true, Config.oneLine);
		if (Config.useLog) {
			log.info(nbString.toString());
		} else {
			System.out.println(nbString.toString());
		}
		
	}
	/**
	 * 对象的每个属性为一行,如果属性为基本类型则不换行.
	 * @param obj
	 * @param buffer
	 * @param newLine
	 * @param first
	 * @return
	 */
	public static StringBuffer toNbString(Object obj,StringBuffer buffer,boolean newLine,boolean first,boolean oneLine) {
		if (obj==null) {
			return buffer; 
		}
		
		if (isBaseType(obj.getClass())) {
			buffer.append(obj.getClass().getSimpleName() + "["+obj+"]") ; 
			return buffer; 
		}	
			
		// 获取所有包含private的属性
		Field[] declaredFields = obj.getClass().getDeclaredFields();
//		buffer.append("\t") ; 
		buffer.append(obj.getClass().getSimpleName()).append(":") ; 
		
		boolean fuck = false ; 
		// 对象名后换行
		if (!oneLine) {
			
			if (newLine&&first) {
				buffer.append("\r\n") ; 
				fuck = true ; 
			}
		}
		
		
		for (Field field : declaredFields) {
			
			field.setAccessible(true); // 设置为可访问
			try {
				if (field.get(obj)==null) {
					continue ; 
				}
				if (field.getName().equals("serialVersionUID")) {
					continue ; 
				}
				Class<?> type = field.getType(); 
				if (Collection.class.isAssignableFrom(type)) {
					Collection collection = (Collection) field.get(obj);
//					buffer.append("\r\n") ; 
					if (!oneLine) {
						
						if (newLine&&first) {
							if (fuck) {
								fuck = false ; 
							}else {
								buffer.append("\r\n") ; 
							}
						}
					}
					
					buffer.append(field.getName()+":") ; 
					buffer.append("{") ; 
					for (Object object : collection) {
						toNbString(object,buffer,false,false,oneLine);
					}
					buffer.append("}") ; 
//					buffer.append("\r\n") ; 
				} else if (type.isArray()) {
									
					if (!oneLine) {
						if (newLine && first) {

							if (fuck) {
								fuck = false;
							} else {
								buffer.append("\r\n");
							}
						}
					}
					
					if (type==long[].class) {
						long[] array = (long[]) field.get(obj);
//						System.out.println("\t" + field.getName() +Arrays.toString(array)); ; 
						buffer.append(field.getName() +Arrays.toString(array)) ; 
						
					} else if (type==int[].class) {
						int[] array = (int[]) field.get(obj);
						buffer.append(field.getName() +Arrays.toString(array)) ; 
						
					}else if (type==short[].class) {
						short[] array = (short[]) field.get(obj);
						buffer.append(field.getName() +Arrays.toString(array)) ; 
						
					}else if (type==byte[].class) {
						byte[] array = (byte[]) field.get(obj);
						buffer.append(field.getName() +Arrays.toString(array)) ; 

					}else if (type==double[].class) {
						double[] array = (double[]) field.get(obj);
						buffer.append(field.getName() +Arrays.toString(array)) ; 
						
					}else if (type==float[].class) {
						float[] array = (float[]) field.get(obj);
						buffer.append(field.getName() +Arrays.toString(array)) ; 
						
					}else {
						Object[] objects = (Object[])field.get(obj) ; 
						for (Object object : objects) {
							if (object!=null) {
								toNbString(object, buffer,false,false,oneLine) ; 
							}
						}
//						buffer.append("\r\n") ; 
					}
				}else if (isBaseType(type)){
					buffer.append(field.getName() + "["+field.get(obj)+"]") ; 
					fuck = false ;

				}else if (type.isEnum()){
					buffer.append(field.getName() + "["+field.get(obj)+"]") ; 
					fuck = false ;

				}else if (Map.class.isAssignableFrom(type)){
					Map map = (Map) field.get(obj); 
					for (Object object : map.values()) {
						if (isBaseType(object.getClass())) {
							buffer.append(field.getName() + "["+field.get(obj)+"]") ; 
							fuck = false ;
						}else {
							toNbString(object, buffer,false,false,oneLine) ; 
							fuck = false ;
						}
						
					}

				}else{
					
					if (!oneLine) {
						
						if (newLine&&first) {
							if (fuck) {
								fuck = false ; 
							}else {
								buffer.append("\r\n") ; 
							}
						}
					}
					
					toNbString(field.get(obj), buffer,true,false,oneLine) ; 
//					buffer.append("\r\n") ; 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return buffer ; 
		
	}
	

	public static boolean isBaseType(Class<?> type) {
		if (type == Long.class || type == Double.class || type == Float.class
				|| type == Integer.class || type == Short.class
				|| type == Byte.class || type == Boolean.class
				|| type == String.class || type == Character.class
				|| type == long.class || type == double.class
				|| type == float.class || type == int.class
				|| type == short.class || type == byte.class
				|| type == boolean.class || type == char.class) {
			return true;
		}
		return false;
	}
	
	public static void main(String args[]) {

	}
}

