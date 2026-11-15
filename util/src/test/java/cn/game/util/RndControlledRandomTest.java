package cn.game.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Rnd 随机源全量收敛与可控随机 (ControlledRandom) 确定性测试 (TC-C14-CONTROLLED-RNG)
 *
 * @author shuyongqiang
 */
public class RndControlledRandomTest {

	@BeforeEach
	@AfterEach
	public void tearDown() {
		Rnd.resetControlledRandom();
	}

	@Test
	public void testControlledRngConvergenceAndSequences() {
		// 1. 验证默认模式下收敛方法正常运行且无报错
		Assertions.assertFalse(Rnd.isControlledRandomEnabled());
		int odd = Rnd.getOdd(1, 10);
		Assertions.assertTrue(odd >= 1 && odd <= 10 && odd % 2 != 0);

		int[] numbers = Rnd.getRandomNumbers(new int[] { 1, 2, 3, 4, 5 }, 3);
		Assertions.assertEquals(3, numbers.length);

		int r1 = Rnd.random(new int[] { 10, 20, 30 }, 0, 3);
		Assertions.assertTrue(r1 >= 0 && r1 < 3);

		int r2 = Rnd.random(List.of(10, 20, 30), 0, 3);
		Assertions.assertTrue(r2 >= 0 && r2 < 3);

		List<String> list = new ArrayList<>(List.of("A", "B", "C", "D"));
		List<String> subList = Rnd.randomSubList(list, 2);
		Assertions.assertEquals(2, subList.size());

		List<Integer> genList = Rnd.generateRandomNumbers(1, 10, 5);
		Assertions.assertEquals(5, genList.size());

		// 2. 种子模式测试 (SEED): 相同种子下采样 100% 确定可复现
		long seed = 123456789L;
		Rnd.setControlledRandomSeed(seed);
		Assertions.assertTrue(Rnd.isControlledRandomEnabled());
		Map<String, Object> status = Rnd.getControlledRandomStatus();
		Assertions.assertEquals("SEED", status.get("mode"));

		int s1_int = Rnd.nextInt(100);
		double s1_dbl = Rnd.nextDouble();
		int s1_odd = Rnd.getOdd(1, 50);
		List<Integer> s1_gen = new ArrayList<>(Rnd.generateRandomNumbers(1, 20, 5));

		// 重置后重新设相同种子
		Rnd.resetControlledRandom();
		Rnd.setControlledRandomSeed(seed);
		int s2_int = Rnd.nextInt(100);
		double s2_dbl = Rnd.nextDouble();
		int s2_odd = Rnd.getOdd(1, 50);
		List<Integer> s2_gen = new ArrayList<>(Rnd.generateRandomNumbers(1, 20, 5));

		Assertions.assertEquals(s1_int, s2_int);
		Assertions.assertEquals(s1_dbl, s2_dbl);
		Assertions.assertEquals(s1_odd, s2_odd);
		Assertions.assertEquals(s1_gen, s2_gen);

		// 3. 序列模式测试 (SEQUENCE): 预设整数与浮点序列按序精准消费
		Rnd.resetControlledRandom();
		List<Integer> presetInts = List.of(3, 7, 1);
		List<Double> presetDoubles = List.of(0.12, 0.88);
		Rnd.setControlledRandomSequence(presetInts, presetDoubles, null);

		Map<String, Object> seqStatus = Rnd.getControlledRandomStatus();
		Assertions.assertEquals("SEQUENCE", seqStatus.get("mode"));
		Assertions.assertEquals(3, seqStatus.get("remainingInts"));
		Assertions.assertEquals(2, seqStatus.get("remainingDoubles"));

		// 消费整数序列
		Assertions.assertEquals(3, Rnd.nextInt(10));
		Assertions.assertEquals(7, Rnd.nextInt(10));
		Assertions.assertEquals(1, Rnd.nextInt(10));

		// 消费浮点序列
		Assertions.assertEquals(0.12, Rnd.nextDouble(), 0.0001);
		Assertions.assertEquals(0.88, Rnd.nextDouble(), 0.0001);

		// 4. 序列耗尽平滑回退，不抛越界异常
		int fallbackInt = Rnd.nextInt(10);
		Assertions.assertTrue(fallbackInt >= 0 && fallbackInt < 10);
		double fallbackDouble = Rnd.nextDouble();
		Assertions.assertTrue(fallbackDouble >= 0.0 && fallbackDouble < 1.0);

		// 5. 边界与参数校验异常
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Rnd.setControlledRandomSequence(null, null, null);
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Rnd.setControlledRandomSequence(List.of(), List.of(1.5), null);
		});
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			Rnd.setControlledRandomSequence(List.of(), List.of(-0.1), null);
		});

		// 6. 验证 get(min, max)、nextInt(n)、nextInt(min, max) 在耗尽时原子回退且不抛拆箱 NPE
		Rnd.resetControlledRandom();
		Rnd.setControlledRandomSequence(List.of(42), List.of(), null);
		Assertions.assertEquals(42, Rnd.nextInt(100)); // 消费唯一的元素
		// 再次消费，队列已空，平滑回退，断言不抛 NPE
		int depletedVal1 = Rnd.get(10, 20);
		Assertions.assertTrue(depletedVal1 >= 10 && depletedVal1 <= 20);
		int depletedVal2 = Rnd.nextInt(50);
		Assertions.assertTrue(depletedVal2 >= 0 && depletedVal2 < 50);
		int depletedVal3 = Rnd.nextInt(30, 40);
		Assertions.assertTrue(depletedVal3 >= 30 && depletedVal3 < 40);
	}
}
