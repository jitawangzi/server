package cn.game.games.net.game.module.prop;

public class LongProperty {

    private long[] values;

    public LongProperty(int size) {
        values = new long[size];
    }

    public boolean set(int index, int value) {
        long _o = values[index];
        if (_o != value) {
            values[index] = value;
            return true;
        }
        return false;
    }

    public long get(int index) {
        return values[index];
    }

    public void clear() {
        for (int i = 0; i < this.values.length; i++) {
            values[i] = 0;
        }
    }

    public void add(LongProperty property) {
        for (int i = 0; i < this.values.length; i++) {
            add(i, property.get(i));
        }
    }

    /**
     * 增加index对应的int值
     *
     */
    public long add(int index, long value) {
        long _o = values[index];
        long _n = _o + value;
        if (_o != _n) {
            values[index] = _n;
        }
        return values[index];
    }

}
