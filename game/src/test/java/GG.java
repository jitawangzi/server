import java.time.LocalDateTime;
import java.util.Date;

public class GG {
	public static int x = 0;
	public static long firstTime = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {

//		Calendar calendar = Calendar.getInstance();
//		System.out.println(calendar.get(Calendar.DAY_OF_YEAR));
		

		LocalDateTime targetDateTime = LocalDateTime.now();
		// 获取时间戳（秒）
		long timestamp = targetDateTime.toEpochSecond(java.time.ZoneOffset.UTC);
//		targetDateTime.toLocalTime().to

		System.out.println("n 天后的 0 点 0 分 0 秒的时间戳：" + timestamp);
		System.out.println(timestamp);
		System.out.println(new Date().getTime());
		
		
	}


}
