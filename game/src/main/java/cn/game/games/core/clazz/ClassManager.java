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
import cn.game.games.net.cross.activity.CrossActivityBase;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.games.net.game.module.activity.PlayerActivityBase;
import cn.game.games.net.game.module.quest.AbstractCondition;
import cn.game.games.net.game.module.quest.ConditionType;
import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.util.file.PackageScanner;
import cn.game.util.reflect.ClassHelper;

public class ClassManager {
    private static ClassManager instance = new ClassManager();
    private final Map<Integer, Class<? extends AbstractCondition>> conditionClass = new HashMap<>();
    private final Map<Integer, Class<? extends PlayerActivityBase>> playerActivityClass = new HashMap<>();
    private final Map<Integer, Class<? extends ActivityBase>> serverActivityClass = new HashMap<>();
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
     * 初始化活动类class，区分玩家侧活动与服务器侧活动双桶注册
     */
    private void initActivityClass() {
        playerActivityClass.clear();
        serverActivityClass.clear();
        activityClass.clear();
        Set<Class<? extends ActivityBase>> subclasses = ClassHelper
                .findSubclasses("cn.game.games", ActivityBase.class);
        for (Class<? extends ActivityBase> c : subclasses) {
            if (java.lang.reflect.Modifier.isAbstract(c.getModifiers()) || c == CrossActivityBase.class) {
                continue;
            }
            ActivityType annotation = c.getAnnotation(ActivityType.class);
            if (annotation == null) {
                throw new IllegalArgumentException(c.getName() + " 没有配置ActivityType注解");
            }
            ActivityTypeEnum type = annotation.type();
            if (PlayerActivityBase.class.isAssignableFrom(c)) {
                @SuppressWarnings("unchecked")
                Class<? extends PlayerActivityBase> playerClass = (Class<? extends PlayerActivityBase>) c;
                if (playerActivityClass.containsKey(type.ID)) {
                    throw new IllegalArgumentException(
                            MessageFormat.format("Player activity class[{0}] 配置了重复的ActivityType注解 [{1}]，已经在[{2}]里配置了", c.getName(), type,
                                    playerActivityClass.get(type.ID)));
                }
                playerActivityClass.put(type.ID, playerClass);
                activityClass.put(type.ID, c);
            } else {
                if (serverActivityClass.containsKey(type.ID)) {
                    throw new IllegalArgumentException(
                            MessageFormat.format("Server activity class[{0}] 配置了重复的ActivityType注解 [{1}]，已经在[{2}]里配置了", c.getName(), type,
                                    serverActivityClass.get(type.ID)));
                }
                serverActivityClass.put(type.ID, c);
                if (!activityClass.containsKey(type.ID)) {
                    activityClass.put(type.ID, c);
                }
            }
        }
    }

    public boolean hasPlayerActivityClass(ActivityTypeEnum type) {
        return type != null && playerActivityClass.containsKey(type.ID);
    }

    public boolean hasPlayerActivityClass(int type) {
        return playerActivityClass.containsKey(type);
    }

    public boolean hasServerActivityClass(ActivityTypeEnum type) {
        return type != null && serverActivityClass.containsKey(type.ID);
    }

    public boolean hasServerActivityClass(int type) {
        return serverActivityClass.containsKey(type);
    }

    public Class<? extends PlayerActivityBase> getPlayerActivityClass(ActivityTypeEnum type) {
        return type == null ? null : playerActivityClass.get(type.ID);
    }

    public Class<? extends PlayerActivityBase> getPlayerActivityClass(int type) {
        return playerActivityClass.get(type);
    }

    public Class<? extends ActivityBase> getServerActivityClass(ActivityTypeEnum type) {
        return type == null ? null : serverActivityClass.get(type.ID);
    }

    public Class<? extends ActivityBase> getServerActivityClass(int type) {
        return serverActivityClass.get(type);
    }

    public Class<? extends ActivityBase> getActivityClass(int type) {
        return activityClass.get(type);
    }

    public PlayerActivityBase createPlayerActivityClassInstance(int type) {
        try {
            Class<? extends PlayerActivityBase> clazz = playerActivityClass.get(type);
            if (clazz == null) {
                throw new IllegalArgumentException("PlayerActivityClass is null, activity type : " + type);
            }
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public ActivityBase createServerActivityClassInstance(int type) {
        try {
            Class<? extends ActivityBase> clazz = serverActivityClass.get(type);
            if (clazz == null) {
                throw new IllegalArgumentException("ServerActivityClass is null, activity type : " + type);
            }
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public ActivityBase createActivityClassInstance(int type) {
        if (playerActivityClass.containsKey(type)) {
            return createPlayerActivityClassInstance(type);
        }
        if (serverActivityClass.containsKey(type)) {
            return createServerActivityClassInstance(type);
        }
        throw new IllegalArgumentException("ActivityClass is null, activity type : " + type);
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