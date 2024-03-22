package cn.game.util;

/**
 * 三元组
 */
public class Triple<A, B, C> extends Pair<A,B>{

    public final C third;

    public Triple(A first, B second, C third) {
        super(first, second);
        this.third = third;
    }

    @Override
    public String toString() {
        return "Triple{" +
                "third=" + third +
                ", first=" + first +
                ", second=" + second +
                '}';
    }
}