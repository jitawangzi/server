package cn.game.games.net.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.util.SpringContextLoader;

public class GateServer {

	private static final Logger log = LoggerFactory.getLogger(GateServer.class)	 ; 
	private static GateServer instance = new GateServer()  ; 
	public static int sequence ; 
	
	private GateServer(){} ; 
	public static void main(String args[]) throws Exception {

//		LogbackConfig.init(true, "res/config/logback-gatewayServer.xml");
		log.info("正在启动网关服。。");
		
		SpringContextLoader.main(args);
		
		log.info("网关服启动成功");

	}
	public static GateServer getInstance() {
		return instance;
	}
	
}
