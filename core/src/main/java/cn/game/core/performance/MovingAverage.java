package cn.game.core.performance;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class MovingAverage {

	private final double[] window;
	private final AtomicInteger index = new AtomicInteger(0);
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

		int currentIndex = index.getAndUpdate(i -> (i + 1) % window.length);
		window[currentIndex] = value;

		if (currentIndex == window.length - 1) {
			filled = true;
		}

		// 如果窗口未填满，只计算已填充部分的平均值
		if (!filled) {
			return Arrays.stream(window, 0, currentIndex + 1).average().orElse(0);
		} else {
			return Arrays.stream(window).average().orElse(0);
		}
	}

	/**
	 * 获取当前平均值，不添加新数据
	 */
	public double current() {
		if (!filled && index.get() == 0) {
			return 0.0;
		}

		if (!filled) {
			return Arrays.stream(window, 0, index.get()).average().orElse(0);
		} else {
			return Arrays.stream(window).average().orElse(0);
		}
	}

	/**
	 * 重置滑动平均
	 */
	public void reset() {
		Arrays.fill(window, 0.0);
		index.set(0);
		filled = false;
	}
}