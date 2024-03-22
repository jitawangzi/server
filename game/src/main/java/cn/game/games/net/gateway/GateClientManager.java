package cn.game.games.net.gateway;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.net.client.GateClient;
public class GateClientManager
{
	private static Logger							log				= LoggerFactory.getLogger(GateClientManager.class);
	/* 客户端管理唯一实例 */
	private static GateClientManager						instance		=  new GateClientManager() ;

	private ConcurrentMap<String, GateClient> clients = new ConcurrentHashMap<>();
	
	private GateClientManager(){} ; 
	public static GateClientManager getInstance()
	{
		return instance;
	}
	
	public GateClient getGateClient(String sessionId) {
		return this.clients.get(sessionId) ; 
	}
	
	public void addGateClient(GateClient gateClient){
		
		clients.putIfAbsent(gateClient.getSessionId(), gateClient) ; 
	}
	public void removeGateClient(GateClient gateClient){
		
		clients.remove(gateClient.getSessionId()) ; 
	}
	
	public void broadbast(Object message){
		for (GateClient gateClient : clients.values()) {
			gateClient.sendProtocol(message);
		}
	}
	public void broadbast(Object message,List<Long> sessionIds){
		for (Long id : sessionIds) {
			GateClient gateClient = clients.get(id); 
			if (gateClient!=null) {
				gateClient.sendProtocol(message);
			}
		}
	}
	
}
	
