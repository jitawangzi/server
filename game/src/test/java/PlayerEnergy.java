
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PlayerEnergy {

	private static final int[] REWARD_HOURS = { 6, 12, 18, 22 };
	private static final int ENERGY_PER_REWARD = 30;

	private LocalDateTime lastOnlineTime;
	private int energy;

	public PlayerEnergy(LocalDateTime lastOnlineTime, int initialEnergy) {
		this.lastOnlineTime = lastOnlineTime;
		this.energy = initialEnergy;
	}

	public int getEnergy() {
		return energy;
	}

	public void setLastOnlineTime(LocalDateTime lastOnlineTime) {
		this.lastOnlineTime = lastOnlineTime;
	}

	public void updateEnergy(LocalDateTime currentOnlineTime) {
		List<LocalDateTime> rewardTimes = calculateRewardTimes(lastOnlineTime, currentOnlineTime);

		for (LocalDateTime rewardTime : rewardTimes) {
			if (rewardTime.isAfter(lastOnlineTime) && rewardTime.isBefore(currentOnlineTime)) {
				energy += ENERGY_PER_REWARD;
			}
		}

		// 更新最后上线时间为当前上线时间
		setLastOnlineTime(currentOnlineTime);
	}

	private List<LocalDateTime> calculateRewardTimes(LocalDateTime from, LocalDateTime to) {
		List<LocalDateTime> rewardTimes = new ArrayList<>();
		LocalDateTime startOfDay = from.toLocalDate().atStartOfDay();

		for (LocalDateTime date = startOfDay; date.isBefore(to.plusDays(1)); date = date.plusDays(1)) {
			for (int hour : REWARD_HOURS) {
				LocalDateTime rewardTime = date.with(LocalTime.of(hour, 0));
				rewardTimes.add(rewardTime);
			}
		}

		return rewardTimes;
	}

	public static void main(String[] args) {
		// 示例：玩家上次上线时间
		LocalDateTime lastOnlineTime = LocalDateTime.of(2024, 5, 3, 14, 30);
		// 示例：玩家当前上线时间
		LocalDateTime currentOnlineTime = LocalDateTime.of(2024, 5, 4, 23, 45);

		PlayerEnergy player = new PlayerEnergy(lastOnlineTime, 0);
		player.updateEnergy(currentOnlineTime);

		System.out.println("当前体力: " + player.getEnergy()); // 输出体力值
	}
}
