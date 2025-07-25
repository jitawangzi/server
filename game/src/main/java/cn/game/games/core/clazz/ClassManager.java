package cn.game.games.core.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.game.games.net.game.module.activity.ActivityType;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.util.file.PackageScanner;
import cn.game.util.reflect.ClassHelper;

public class ClassManager {
    private static ClassManager instance = new ClassManager();
    private final Map<Integer, Class<? extends AbstractCondition>> conditionClass = new HashMap<>();
    private final Map<Integer, Class<? extends ActivityBase>> activityClass = new HashMap<>();

    // 缓存所有Condition实例，饿汉式加载
    private final Map<Integer, AbstractCondition> conditionClassInstanceCache = new HashMap<>();

    private ClassManager() {
    }

    public static ClassManager getInstance() {
        return instance;
    }

    public void init() {
        initConditionClass();
        initActivityClass();
    }

    /** 
     * 初始化条件类class和实例缓存（饿汉式加载实例）
     */
    private void initConditionClass() {
        conditionClass.clear();
        conditionClassInstanceCache.clear();

        Set<Class<? extends AbstractCondition>> subclasses = ClassHelper.findSubclasses(AbstractCondition.class.getPackageName(), AbstractCondition.class);
        for (Class<? extends AbstractCondition> c : subclasses) {
            ConditionType annotation = c.getAnnotation(ConditionType.class);
            if (annotation == null) {
                throw new IllegalArgumentException(c.getName() + " 没有配置ConditionType注解");
            }
            ConditionTypeEnum type = annotation.type();
            if (conditionClass.containsKey(type.ID)) {
                throw new IllegalArgumentException(
                    MessageFormat.format("class[{0}] 配置了重复的ConditionType注解 [{1}]，已经在[{2}]里配置了", c.getName(), type,
                        conditionClass.get(type.ID)));
            }
            conditionClass.put(type.ID, c);

            // 直接创建实例放到缓存
            try {
                AbstractCondition instance = c.getDeclaredConstructor().newInstance();
                conditionClassInstanceCache.put(type.ID, instance);
            } catch (Exception e) {
                throw new RuntimeException("创建Condition实例失败: " + c.getName(), e);
            }
        }
    }

    public Class<? extends AbstractCondition> getConditionClass(int type) {
        return conditionClass.get(type);
    }

    /** 
     * 每次创建新的实例
     * @param type
     * @return
     */
    public AbstractCondition createConditionClassInstance(int type) {
        AbstractCondition newInstance = null;
        try {
            Class<? extends AbstractCondition> clazz = conditionClass.get(type);
            if (clazz == null) {
                throw new IllegalArgumentException("ConditionClass is null, condition type : " + type);
            }
            newInstance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return newInstance;
    }

    /**
     * 获取缓存的实例
     */
    public AbstractCondition getCachedConditionClassInstance(int type) {
        return conditionClassInstanceCache.get(type);
    }

    /** 
     * 初始化活动类class
     */
    private void initActivityClass() {
        activityClass.clear();
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
            if (clazz == null) {
                throw new IllegalArgumentException("ActivityClass is null, activity type : " + type);
            }
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