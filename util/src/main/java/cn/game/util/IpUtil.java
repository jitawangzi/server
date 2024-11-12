package cn.game.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class IpUtil {

	/**
	 * 获取局域网ip ，多网卡时，需要先指定网卡
	 * 
	 * @return
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public static String defaultAddress() throws SocketException, UnknownHostException {

		String defaultIp = System.getProperty("defaultNetworkIp", System.getenv("defaultNetworkIp"));
		if (defaultIp != null) {
			return defaultIp;
		}
		String defaulInterface = System.getProperty("defaultNetworkInterface", System.getenv("defaultNetworkInterface"));
		return defaultAddress(defaulInterface);
	}

	/**
	 * 获取局域网ip
	 * 
	 * @param defaultNetworkInterface
	 *            指定网卡名
	 * @return
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	private static String defaultAddress(String defaultNetworkInterface) throws SocketException, UnknownHostException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		NetworkInterface netinf;
		InetAddress result = null;
		loop: while (nets.hasMoreElements()) {
			netinf = nets.nextElement();
//			System.err.println(netinf.getName());
			// 筛选网卡
			if (defaultNetworkInterface != null && !defaultNetworkInterface.equalsIgnoreCase(netinf.getName())) {
				continue;
			}
			Enumeration<InetAddress> addresses = netinf.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				if (!address.isLoopbackAddress() && !address.isAnyLocalAddress() && !address.isMulticastAddress()
						&& !(address instanceof Inet6Address) && address.isSiteLocalAddress()) {
//					System.err.println(address);
					result = address;
					break loop;
				}
			}
		}
		if (result == null) {
			result = InetAddress.getLocalHost();
		}
		return result.getHostAddress();
	}

	/**
	 * 转换ip，如 127.0.0.1 , 127*2563+0*2562+0*256+1=2130706433 ；
	 * @param ip
	 * @return
	 */
	public static long getIp2(String ip) {
		String[] split = ip.split("\\.");
		return Integer.parseInt(split[0]) * 256 * 256 * 256 + Integer.parseInt(split[1]) * 256 * 256 + Integer.parseInt(split[2]) * 256
				+ Integer.parseInt(split[3]);
	}

	public static long getIp(String ip) {
		String[] split = ip.split("\\.");
		return Integer.parseInt(split[0]) * (2 << 23) + Integer.parseInt(split[1]) * (2 << 15) + Integer.parseInt(split[2]) * (2 << 7)
				+ Integer.parseInt(split[3]);
	}

	public static void main(String[] args) {

		System.out.println(getIp2("127.0.0.1"));
		// 2130706433
	}

}
