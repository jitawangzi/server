package cn.game.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;


/**   
 * 
 * 2016-6-17 上午10:24:35
 * @author SYQ
 */
public final class Config {
	private static Logger log = LoggerFactory.getLogger(Config.class);
	/** 配置文件路径 */
	public static final String CONFIG_FILE = "config/config.conf";
	/** 聊天关键字配置文件 */
	public static final String CHAT_FILTER_FILE = "chatfilter.txt";

	/** 文件流编码 */
	public static final String charsetName = "GBK";
	/** 延时500毫秒 */
	public static final int DELAY_500MS = 500;
	/** 延时1秒 */
	public static final int DELAY_1S = 1000;
	/** 收包最大空闲时间(毫秒) */
	private static int RECVIDLETIMEOUT_DEFAULT = 5 * 60 * 1000;

	/** 保存在线玩家数据间隔，秒 */
	public static int ONLINE_SAVE;
	/** 最后一次收到包的时间戳 */
	public static long lastRecvPacketTime = RECVIDLETIMEOUT_DEFAULT;
	/** 过虑通配符 */
	public static List<String> FILTER_LIST;
	/** 替换字符 */
	public static String CHAT_FILTER_CHARS = "^_^";
	/** 服务器版本号 */
	public static int min_vision;
	public static int max_vision;

	public static int generalPacketMin;
	public static int generalPacketMax;
	public static int generalPacketKeepAliveTime;
	public static int generalPacketWorkQueueSize = 1024;
	public static int generalScheduled = 3;

	/** 是否输出上行包执行时间 */
	public static boolean debugPackExecTime;
	/** 是否打印客户超时日志 */
	public static boolean printTimeOut;
	/** 用户平台地址 */
	public static String userplatformurl = "http://192.168.0.20:8888/Passport/ServerXML";
	/** 死锁检测时间 */
	public static int DEADLOCK_CHECK_INTERVAL;
	/** 正常一个包的执行时间，超过这个值则记录。 */
	public static int packetExecTime;
	/** 运营管理ＩＰ */
	public static String managerIp;

	public static String vertxRedisUrl;

	public static boolean useLog;
	public static boolean oneLine;
	public static boolean recordSendData;
	public static boolean recordRecvData;

	/** 是否开启gm命令 */
	public static boolean gmOpen;

	public static boolean hotUpdate;
	public static String agentJar;

	/** 服务器心跳包间隔 */
	public static int heart;
	/** 同步远程调用的超时时间,5s */
	public static int remoteCallTimeOut = 5;

	public static int remoteCallTimeOutMillisecond = remoteCallTimeOut * 1000;

	/** 服务器关闭时最大等待时间 */
	public static int shutdownWaitTime = 60 * 60;

	public static String redisPwd;

//	public static boolean isTest;

	public static int[] modulesDisabled;
	
	/** 微信发货消息推送相关参数 */
	public static String  wechat_push_token;
	public static String  wechat_push_EncodingAESKey;
	public static String  wechat_appid;
	public static String  wechat_secret;
	public static String  wechat_midas_offerId;
	public static byte  wechat_midas_env;
	public static String  wechat_midas_AppKey;
	// 消息统计的时间间隔，分钟
	public static int messageStatisticsInterval;

	/** 游戏唯一表示 */
	public static String APP_KEY;

	/**
	 *
	 */
	public static void load() {
		try {
			// 初始化参数
			com.ctrip.framework.apollo.Config initialProp = ConfigService.getConfig("config");

//			InputStream path = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
//			Properties initialProp = new Properties();
//			initialProp.load(path);
//			path.close();
			generalPacketMin = Integer.parseInt(initialProp.getProperty("generalPacketMin", "5"));
			generalPacketMax = Integer.parseInt(initialProp.getProperty("generalPacketMax", "7"));
			generalPacketKeepAliveTime = Integer.parseInt(initialProp.getProperty("generalPacketKeepAliveTime", "15"));
			generalPacketWorkQueueSize = Integer.parseInt(initialProp.getProperty("generalPacketWorkQueueSize", "1024"));
			generalScheduled = Integer.parseInt(initialProp.getProperty("generalScheduled", "10"));

			debugPackExecTime = Boolean.parseBoolean(initialProp.getProperty("debugPackExecTime", "false"));

			min_vision = Short.parseShort(initialProp.getProperty("min_vision", "10000"));
			max_vision = Short.parseShort(initialProp.getProperty("max_vision", "10000"));
			lastRecvPacketTime = Integer.parseInt(initialProp.getProperty("lastRecvPacketTime", "300")) * 1000;

			printTimeOut = Boolean.parseBoolean(initialProp.getProperty("printTimeOut", "false"));
			userplatformurl = initialProp.getProperty("userplatformurl", "http://192.168.0.20:8888/Passport/ServerXML");
			managerIp = initialProp.getProperty("managerIp", "");

			DEADLOCK_CHECK_INTERVAL = Integer.parseInt(initialProp.getProperty("deadlock_check_interval", "20"));
			packetExecTime = Integer.parseInt(initialProp.getProperty("packetExecTime", "30"));
			useLog = Boolean.parseBoolean(initialProp.getProperty("useLog", "false"));
			oneLine = Boolean.parseBoolean(initialProp.getProperty("oneLine", "false"));
			recordSendData = Boolean.parseBoolean(initialProp.getProperty("recordSendData", "false"));
			recordRecvData = Boolean.parseBoolean(initialProp.getProperty("recordRecvData", "false"));
			oneLine = Boolean.parseBoolean(initialProp.getProperty("oneLine", "false"));
			gmOpen = Boolean.parseBoolean(initialProp.getProperty("gmOpen", "false"));

			vertxRedisUrl = initialProp.getProperty("vertxRedisUrl", "");
			agentJar = initialProp.getProperty("agent.jar.addr", "");
			hotUpdate = Boolean.parseBoolean(initialProp.getProperty("hot.update", "false"));
			heart = Integer.parseInt(initialProp.getProperty("heart", "3000"));
			ONLINE_SAVE = Integer.parseInt(initialProp.getProperty("online_save", "300"));
			redisPwd = initialProp.getProperty("redisPwd", "");

			remoteCallTimeOut = Integer.parseInt(initialProp.getProperty("remoteCallTimeOut", "5"));
//			isTest = initialProp.getBooleanProperty("isTest", false);
			String[] modules = initialProp.getArrayProperty("modulesDisabled", ",", new String[] {});
			
			wechat_push_token = initialProp.getProperty("wechat_push_token", "");
			wechat_push_EncodingAESKey = initialProp.getProperty("wechat_push_EncodingAESKey", "");
			wechat_appid = initialProp.getProperty("wechat_appid", "");
			wechat_secret = initialProp.getProperty("wechat_secret", "");
			wechat_midas_offerId = initialProp.getProperty("wechat_midas_offerId", "");
			wechat_midas_env = Byte.parseByte(initialProp.getProperty("wechat_midas_env", ""));
			wechat_midas_AppKey = initialProp.getProperty("wechat_midas_AppKey", "");
			
			messageStatisticsInterval = Integer.parseInt(initialProp.getProperty("messageStatisticsInterval", "5"));

			APP_KEY = initialProp.getProperty("APP_KEY", "");

			if (modules.length > 0) {
				modulesDisabled = new int[modules.length];
				for (int i = 0; i < modules.length; i++) {
					String parse = modules[i].trim().toLowerCase().replaceAll("0x", "");
					if (parse.length() > 0) {
						modulesDisabled[i] = Integer.parseInt(parse, 16);
					}
				}
			}
			initialProp.addChangeListener(new ConfigChangeListener() {
				@Override
				public void onChange(ConfigChangeEvent changeEvent) {
//					System.out.println("Changes for namespace " + changeEvent.getNamespace());
					// 简单处理，重新载入
					load();
				}
			});

//			loadKeyWord();

		} catch (Exception e) {
			e.printStackTrace();
			throw new Error("Failed to Load Config File.");
		}
	}

	public static void loadKeyWord() {

		try {
			InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(CHAT_FILTER_FILE);
			List<String> tmp = new ArrayList<String>();
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inputStream)) ; 
			String line = null;
			while ((line = lnr.readLine()) != null) {
				if (line.trim().isEmpty() || line.startsWith("#")) {
					continue;
				}
				
				tmp.add(line.trim());
			}
			FILTER_LIST = tmp;
			log.info("Loaded " + FILTER_LIST.size() + " Filter Words");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 检测关键字是否存在
	 * 
	 * @param test
	 * @return true 有 false无
	 */
	public static boolean checkKeyWord(String test) {
		boolean ret = false;
		for (String pattern : Config.FILTER_LIST) {

			ret = Pattern.compile(".*" + pattern + ".*").matcher(test).matches();
			if (test.indexOf(".") != -1) {
				ret = true;
			}
			if (ret) {
				break;
			}
		}
		return ret;
	}

	/**
	 * 检查文本，返回替换违法字的新文本
	 * @param text
	 * @return
	 */
	public static String checkText(String text) {
		for (String pattern : Config.FILTER_LIST) {
			text = text.replaceAll("(?i)" + pattern, Config.CHAT_FILTER_CHARS);
		}
		return text;
	}

	/**
	 * 检测是否为运营后台ＩＰ地址
	 * 
	 * @param testIp
	 * @return
	 */
	public static boolean isManagerIp(String testIp) {
		boolean result = false;
		if (StringUtils.isNotBlank(managerIp) && managerIp.indexOf(testIp) != -1) {
			result = true;
		}
		log.info("opType[isManagerIp]managerIp[{}]testIp[testIp]result[{}]", new Object[] { managerIp, testIp, result });
		return result;
	}
	/**
	 * 某功能模块是否禁用
	 * 
	 * @param module
	 * @return
	 */
	public static boolean isModuleDisabled(int module) {
		if (modulesDisabled == null || modulesDisabled.length == 0) {
			return false ; 
		}
		for (int i = 0; i < modulesDisabled.length; i++) {
			if (modulesDisabled[i] == module) {
				return true ; 
			}
		}
		return false ; 
	}
}
