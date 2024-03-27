package cn.game.games.net.game.module.shop;

public class ShopHelper {

	// 购买的消耗类型
	/** 普通资源购买 */
	public static final int COST_TYPE_RESOURCE = 1;
	/** 充值 */
	public static final int COST_TYPE_RECHARGE = 2;
	/** 看广告 */
	public static final int COST_TYPE_ADVERTISE = 3;

	/** 
	 * 根据给的折扣，计算并返回折扣后的数量
	 * @param array
	 * @param discount
	 * @return
	 */
	public static int[][] discount(int[][] array, int discount) {
		if (discount == 0) {
			return array;
		}
		int[][] ret = new int[array.length][array[0].length];

		for (int i = 0; i < array.length; i++) {

			ret[i][0] = array[i][0];
			ret[i][1] = (int) (array[i][1] * (discount) / 100f);
		}
		return ret;
	}

	/** 
	 * 3个参数的
	 * @param array
	 * @param discount
	 * @return
	 */
	public static int[] discount(int[] array, int discount) {
		if (discount == 0) {
			return array;
		}
		int[] ret = new int[array.length];
		ret[0] = array[0];
		ret[1] = array[1];
		ret[2] = (int) (array[2] * (discount) / 100f);
		return ret;
	}

	/** 
	 * 修改数量
	 * @param array
	 * @param multiple 倍数
	 * @return
	 */
	public static int[][] multipleCount(int[][] array, int multiple) {
		if (multiple <= 1) {
			return array;
		}
		int[][] ret = new int[][] {};

		for (int i = 0; i < array.length; i++) {

			ret[i][0] = ret[i][0];
			ret[i][1] = ret[i][1] * multiple;
		}
		return ret;
	}
}
