package cn.game.util;

import java.util.ArrayList;
import java.util.List;

public class IndexedEnumUtil {

    public static <E extends IndexedEnum> List<E> toIndexes(E[] enums) {
        int maxIndex = Integer.MIN_VALUE;
        int curIdx = 0;
        for (E e : enums) {
            curIdx = e.getIndex();
            if (curIdx < 0) {
                throw new IllegalArgumentException(String.format("枚举索引不能为负，index:%1$d type:%2$s", curIdx,
                        e.getClass().getComponentType().getName()));
            }
            if (curIdx > maxIndex) {
                maxIndex = curIdx;
            }
        }

        List<E> instance = new ArrayList<>(maxIndex + 1);
        for (int i = 0; i < maxIndex + 1; i++) {
            instance.add(null);
        }
        for (E e : enums) {
            curIdx = e.getIndex();
            if (instance.get(curIdx) != null) {
                throw new IllegalArgumentException("枚举中有重复的 index " + enums.getClass().getComponentType().getName());
            }
            instance.set(curIdx, e);
        }

        return instance;
    }

    public static <E extends IndexedEnum> E valueOf(List<E> values, int index) {
        E value = null;
        try{
            value = values.get(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}
