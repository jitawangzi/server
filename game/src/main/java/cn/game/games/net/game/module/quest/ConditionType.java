package cn.game.games.net.game.module.quest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.game.protocol.generated.enume.ConditionTypeEnum;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConditionType {
	ConditionTypeEnum type();
}
