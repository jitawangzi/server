package cn.game.games.net.game.module.prop;

import java.util.ArrayList;
import java.util.List;
import cn.game.games.net.game.module.prop.effector.PropertyEffectorFactory;

public enum  RolePropFromType {

    BASE(0, PropertyEffectorFactory.BASE_EFFECTOR),
    EQUIP(1, PropertyEffectorFactory.EQUIP_EFFECTOR),
   // LEVELUP(3, PropertyEffectorFactory.LEVELUP_EFFECTOR),
    BUILDING(2, PropertyEffectorFactory.SUPERBUILDING_EFFECTOR),
    BUFF(4, PropertyEffectorFactory.BUFF_EFFECTOR),
    //COREUNIT(2, PropertyEffectorFactory.COREUNIT_EFFECTOR),
    //MECHA(3, PropertyEffectorFactory.MECHA_EFFECTOR),
    //CHIP(4, PropertyEffectorFactory.CHIP_EFFECTOR),
    //SOULWEAPON(3, PropertyEffectorFactory.SOULWEAPON_EFFECTOR)
    ;

    public final int index;

    public final PropertyEffectorFactory effector;

    private RolePropFromType(int index, PropertyEffectorFactory effector) {
        this.index = index;
        this.effector = effector;
    }

    public static List<RolePropFromType> getRolePropFromTypeList() {
        RolePropFromType[] values = RolePropFromType.values();
        int curIndex = 0;
        int maxIndex = Integer.MIN_VALUE;
        for (RolePropFromType type :values) {
            curIndex = type.getIndex();
            if (curIndex > maxIndex) {
                maxIndex = curIndex;
            }
        }
        List<RolePropFromType> list = new ArrayList<>(maxIndex + 1);
        for (int i = 0; i < maxIndex + 1; i++) {
            list.add(null);
        }
        for (RolePropFromType type :values) {
            list.set(type.getIndex(), type);
        }
        return list;
    }

    public int getIndex() {
        return index;
    }

}
