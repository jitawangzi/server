package cn.game.games.core.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.OldConditionTypeEnum;
import cn.game.util.file.PackageScanner;
import cn.game.util.reflect.ClassHelper;

public class ClassManager {
	private static ClassManager instance = new ClassManager() ; 
	Map<Integer, Class<? extends AbstractCondition>> conditionClass = new HashMap<Integer, Class<? extends AbstractCondition>>();
	Map<Integer, Class<? extends ActivityBase>> activityClass = new HashMap<Integer, Class<? extends ActivityBase>>();

	private ClassManager() {
	};
	
	public static ClassManager getInstance() {
		return instance;
	}

	public void init() {
		initConditionClass();
		initActivityClass();

	}

	/** 
	 * 初始化条件类class
	 */
	private void initConditionClass() {

		Set<Class<? extends AbstractCondition>> subclasses = ClassHelper.findSubclasses(AbstractCondition.class.getPackageName(), AbstractCondition.class);
		for (Class<? extends AbstractCondition> c : subclasses) {
			ConditionType annotation = c.getAnnotation(ConditionType.class); 
			if (annotation == null) {
				throw new IllegalArgumentException(c.getName() + " 没有配置ConditionType注解");
			}
			OldConditionTypeEnum type = annotation.type();
			if (conditionClass.containsKey(type.getId())) {
				throw new IllegalArgumentException(
						MessageFormat.format("class[{0}] 配置了重复的ConditionType注解 [{1}]，已经在[{2}]里配置了", c.getName(), type,
								conditionClass.get(type.getId())));
			}
			conditionClass.put(type.getId(), c);
		}
	}

	public Class<? extends AbstractCondition> getConditionClass(int type) {
		return conditionClass.get(type);
	}

	public AbstractCondition createConditionClassInstance(int type) {
		AbstractCondition newInstance = null;
		try {
			Class<? extends AbstractCondition> clazz = conditionClass.get(type);
			newInstance = clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return newInstance;
	}

	/** 
	 * 初始化活动类class
	 */
	private void initActivityClass() {

		Set<Class<? extends ActivityBase>> subclasses = ClassHelper
				.findSubclasses(ActivityBase.class.getPackageName(), ActivityBase.class);
		for (Class<? extends ActivityBase> c : subclasses) {
			ActivityType annotation = c.getAnnotation(ActivityType.class);
			if (annotation == null) {
				throw new IllegalArgumentException(c.getName() + " 没有配置ActivityType注解");
			}
			ActivityTypeEnum type = annotation.type();
			if (activityClass.containsKey(type.ID)) {
				throw new IllegalArgumentException(
						MessageFormat.format("class[{0}] 配置了重复的ActivityType注解 [{1}]，已经在[{2}]里配置了", c.getName(), type,
								activityClass.get(type.ID)));
			}
			activityClass.put(type.ID, c);
		}
	}

	public Class<? extends ActivityBase> getActivityClass(int type) {
		return activityClass.get(type);
	}

	public ActivityBase createActivityClassInstance(int type) {
		ActivityBase newInstance = null;
		try {
			Class<? extends ActivityBase> clazz = activityClass.get(type);
			newInstance = clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return newInstance;
	}

	public List<Class<?>> scanClassesWithAnnotation(String packageName, Class<? extends Annotation> annotation) {
		List<Class<?>> classes = new ArrayList<>();
		try {
			for (Class<?> clazz : PackageScanner.scanClasses(packageName)) {
				if (clazz.isAnnotationPresent(annotation)) {
					classes.add(clazz);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classes;
	}
}
