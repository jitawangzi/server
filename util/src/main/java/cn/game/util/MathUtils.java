package cn.game.util;


/**    
 * 一些计算方法，和前端同步，只用在公式计算中
 * 2022年8月31日 下午4:02:06
 * @author SYQ
 */
public class MathUtils {

	/**
     * 返回小于或等于x的最大整数
     * @param x 
     * @returns 
     */
    public static int floor(float x) {
        return (int) Math.floor(x);
    }

	/**
     * 返回大于或等于x的最小整数
     * @param x 
     * @returns 
     */
    public static int ceil(float x) {
        return (int) Math.ceil(x);
    }
	public static int ceil(double x) {
		return (int) Math.ceil(x);
	}

	/**
     * 返回x的四舍五入到最接近的整数
     * @param x 
     * @returns 
     */
    public static int round(float x) {
        return Math.round(x);
    }
	/**
	 * 返回x的四舍五入到最接近的整数
	 * @param x 
	 * @returns 
	 */
	public static int round(double x) {
		return (int) Math.round(x);
	}

	/**
     * 返回一个数的绝对值
     * @param x 
     * @returns 
     */
    public static int abs(int x) {
        return Math.abs(x);
    }
	public static float abs(float x) {
    	return Math.abs(x);
    }

	/**
     * 返回x和y之间较小的一个
     * @param x 
     * @returns 
     */
	public static int min(int x, int y) {
        return Math.min(x, y);
    }
	/**
	 * 返回x和y之间较小的一个
	 * @param x 
	 * @returns 
	 */
	public static float min(float x, float y) {
		return Math.min(x, y);
	}
	/**
	 * 返回x和y之间较小的一个
	 * @param x 
	 * @returns 
	 */
	public static double min(double x, double y) {
		return Math.min(x, y);
	}

	/**
     * 返回x和y之间较大的一个
     * @param x 
     * @returns 
     */
	public static int max(int x, int y) {
        return Math.max(x, y);
    }
	/**
     * 返回x和y之间较大的一个
     * @param x 
     * @returns 
     */
	public static float max(float x, float y) {
    	return Math.max(x, y);
    }

	/**
     * 随机数 [>=n <m]
     * @param n 最小
     * @param m 最大
     * @returns 
     */
	public static int randomInt(int n, int m) {
		return Rnd.nextInt(n, m);
    }

	public static void main(String args[]) {
		for (int i = 0; i < 1000; i++) {
			int nextInt = randomInt(1, 3);
			if (nextInt >= 2) {
				System.out.println(nextInt);
			}
		}
	}
}
