package cn.game.games.net.game.module.prop;

import cn.game.protocol.generated.enume.AttributeTypeEnum;

public class RoleProperty extends IntProperty {

    public RoleProperty() {
        super(AttributeTypeEnum.values().length + 1);
    }

}
