package cn.game.games.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AddressUtil {

	private static Logger log = LoggerFactory.getLogger(AddressUtil.class);

	@SuppressWarnings("all")
	private static String getAddress(String ip) {
		try {

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			java.net.URL url = classLoader.getResource("ip2region.db");

			DbConfig config = new DbConfig();
			DbSearcher searcher = new DbSearcher(config, url.getPath());
			// 查询 IP 地址的地区信息
			DataBlock dataBlock = searcher.memorySearch(ip);
			String region = dataBlock.getRegion();
			return region;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("ip2region.db文件不存在");
		} catch (DbMakerConfigException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getCityInfo(String ip) {
		String addr = getAddress(ip);

		String[] split = addr.split("\\|");
		StringBuilder sb = new StringBuilder();
		loop: for (int i = 0; i < split.length - 1; i++) {
			if (split[i].equalsIgnoreCase("0")) {
				continue;
			}
			for (int j = i + 1; j < split.length - 1; j++) {
				if (split[j].equals(split[i])) {
					continue loop;
				}
			}
			sb.append(split[i]);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(getCityInfo("192.168.0.1"));
		System.out.println(getCityInfo("10.10.10.10"));
		System.out.println(getCityInfo("172.16.0.1"));
		System.out.println(getCityInfo("8.8.8.8"));
		System.out.println(getCityInfo("123.45.67.89"));
		System.out.println(getCityInfo("203.0.113.25"));
		System.out.println(getCityInfo("98.76.54.32"));
		System.out.println(getCityInfo("210.16.8.4"));
		System.out.println(getCityInfo("185.199.110.154"));
		System.out.println(getCityInfo("66.249.64.1"));
	}

}