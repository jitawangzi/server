package cn.game.games.net.game.gm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记GM指令分组处理器类
 *
 * @author shuyongqiang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GmGroup {

	/** 分组名称（建议使用小写英文标识符，如 item、player、config） */
	String name();

	/** 分组中文说明 */
	String description() default "";
}