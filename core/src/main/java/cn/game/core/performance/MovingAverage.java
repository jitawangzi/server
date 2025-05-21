package cn.game.core.performance;

import java.util.Arrays;

/**
 * 滑动平均计算
 */
public class MovingAverage {

	private final double[] window;
	private int index = 0;
	private volatile boolean filled = false;

	public MovingAverage(int windowSize) {
		if (windowSize <= 0) {
			throw new IllegalArgumentException("Window size must be positive");
		}
		this.window = new double[windowSize];
		Arrays.fill(window, 0.0);
	}

	public double next(double value) {
		// 确保值在合理范围内
		value = Math.max(0.0, Math.min(1.0, value));
		window[index++] = value;
		if (index == window.length) {
			index = 0;
			filled = true;
		}
		return current();
	}

	/**
	 * 获取当前平均值，不添加新数据
	 * 目前这个实现在值不满的时候，平均值不太精确，不过基本不影响结果
	 */
	public double current() {
		return Arrays.stream(window, 0, filled ? window.length : index).average().orElse(0.0);
	}


	/**
	 * 重置滑动平均
	 */
	public void reset() {
		Arrays.fill(window, 0.0);
		index = 0;
		filled = false;
	}
}