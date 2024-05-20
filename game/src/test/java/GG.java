import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {

		// 假设特定时间是一个毫秒时间戳
		long millis = 1621474200000L; // 示例时间戳，可以替换为你的毫秒时间戳

		// 使用毫秒时间戳创建 Instant 对象
		Instant instant = Instant.ofEpochMilli(millis);

		// 将 Instant 对象转换为 LocalDateTime
		LocalDateTime specificDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

		// 获取 n 天后的日期
		int n = 5; // 假设 n = 5 天
		LocalDate targetDate = specificDateTime.toLocalDate().plusDays(n);

		// 设置时间为 0 点 0 分 0 秒
		LocalDateTime targetDateTime = targetDate.atStartOfDay();

		// 获取系统默认时区
		ZoneId systemDefaultZoneId = ZoneId.systemDefault();

		// 获取时间戳（秒）
		long timestamp = targetDateTime.atZone(systemDefaultZoneId).toEpochSecond();

		System.out.println("特定时间点 " + specificDateTime + " 的 " + n + " 天后的 0 点 0 分 0 秒的时间戳：" + timestamp);
		
		
	}


}
