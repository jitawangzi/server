package cn.game.core.performance;

import java.util.Arrays;

public class MovingAverage {
	private final double[] window;
	private int index = 0;
	private boolean filled = false;

	public MovingAverage(int windowSize) {
		this.window = new double[windowSize];
	}

	public double next(double value) {
		window[index++] = value;
		if (index == window.length) {
			index = 0;
			filled = true;
		}
		return Arrays.stream(window).average().orElse(0);
	}
}