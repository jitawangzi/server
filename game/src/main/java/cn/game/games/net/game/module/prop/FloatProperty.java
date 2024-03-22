package cn.game.games.net.game.module.prop;

import java.util.Map;
import cn.game.util.MapUtil;

public class FloatProperty {

    private Map<Integer, Float> values;

    public FloatProperty(int size) {
        //values = new HashMap<>((int) (size / 0.75) + 1);
        values = MapUtil.newHashMap(size);
    }

    /**
     * 得到指定的属性值
     * @param index
     * @return
     */
    public float get(int index) {
        return values.get(index);
    }

    public void clear() {
        values.clear();
    }

    public void add(FloatProperty property) {
        for (Map.Entry<Integer, Float> entry : property.values.entrySet()) {
            add(entry.getKey(), property.get(entry.getKey()));
        }
    }

    /**
     * 增加index对应的int值
     *
     */
    public void add(int index, float value) {

        Float oldvalue = values.get(index);
        if (null == oldvalue) {
            values.putIfAbsent(index, value);
        } else {
            Float newValue = oldvalue + value;
            values.replace(index, oldvalue, newValue);
        }
    }

}
