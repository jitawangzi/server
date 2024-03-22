package cn.game.games.net.game.module.prop;

public class IntProperty {

    /**
     * 属性值数组
     */
    private final int[] values;

    public IntProperty(int size) {
        values = new int[size];
    }

    /**
     * 设置index对应的值为value
     * @param index
     * @param value
     * @return
     */
    public boolean set(int index, int value) {
        int _o = values[index];
        if (_o != value) {
            values[index] = value;
            return true;
        }
        return false;
    }

    public int size() {
        return this.values.length;
    }

    public int get(int index) {
        return values[index];
    }

    public void clear() {
        for (int i = 0; i < this.values.length; i++) {
            values[i] = 0;
        }
    }

    public void add(IntProperty property) {
        for (int i = 0; i < this.values.length; i++) {
            add(i, property.get(i));
        }
    }

    /**
     * 增加index对应的int值
     *
     */
    public int add(int index, int value) {
        int _o = values[index];
        int _n = _o + value;
        if (_o != _n) {
            values[index] = _n;
        }
        return values[index];
    }

}
