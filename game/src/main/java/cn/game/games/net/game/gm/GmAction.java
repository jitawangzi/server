package cn.game.games.net.game.gm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记具体GM可执行动作方法
 *
 * @author shuyongqiang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GmAction {

	/** 动作名称（默认取方法名） */
	String name() default "";

	/** 向后兼容的旧文本命令或别名列表 */
	String[] aliases() default {};

	/** 动作说明 */
	String description() default "";

	/** 安全等级 */
	GmSecurityTier security() default GmSecurityTier.DEV_TEST_ONLY;

	/** 执行作用域 */
	GmScope scope() default GmScope.PLAYER;

	/** 是否支持演练模式 */
	boolean dryRunSupported() default false;

	/** 副作用说明 */
	String sideEffects() default "";

	/** 是否幂等 */
	boolean idempotent() default false;

	/** 是否为严格只读安全动作（用于 READONLY 角色显式授权，替代动作名后缀推断） */
	boolean readOnly() default false;

	/** 使用示例 */
	String example() default "";
}