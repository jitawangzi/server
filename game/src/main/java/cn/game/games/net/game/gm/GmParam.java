package cn.game.games.net.game.gm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记GM动作方法参数元信息（未指定name时自动回退为编译期-parameters形参名）
 *
 * @author shuyongqiang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GmParam {

	/** 参数名称（留空则读取反射形参名） */
	String name() default "";

	/** 参数说明 */
	String description() default "";

	/** 是否必填 */
	boolean required() default true;

	/** 缺省值字符串 */
	String defaultValue() default "";

	/** 示例值 */
	String example() default "";

	/** 参数展示类型 */
	GmParamType type() default GmParamType.AUTO;

	/** 关联配表或引用目录标识（如 ItemConfig / BattleConfig） */
	String ref() default "";
}