package cn.game.simulation.test.gen;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.HeroConfig;
import cn.game.protocol.generated.manager.HeroManager;
import cn.game.protocol.protobuf.BaseMsg.HeroInfo;
import cn.game.simulation.client.Client;
import cn.game.simulation.test.base.ServerTest;

@Component
public class HeroFragmentComposeRequest_16000050Test extends ServerTest<cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050, cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeResponse_16000051> {

	@Override
	public cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050 buildDebugRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.newBuilder() ; 
		
		builder.addHeroId(331001);
		
		return builder.build() ; 
	}
	
	@Override
	public cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050 tryBuildSimulationRequest(Client client) {
		cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.Builder builder = cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050.newBuilder() ; 
		
		int heroId = 0 ; 
		Collection<HeroConfig> list = HeroManager.instance().list(); 
		
		for (HeroConfig heroConfig : list) {
			if (client.hasHero(heroConfig.ID)) {
				continue ;
			}
			if (!client.hasItem(heroConfig.Fragment, 10)) {
				continue; 
			}
			heroId = heroConfig.ID; 
			break; 
		}
		if (heroId == 0) {
			return null; 
		}
		builder.addHeroId(heroId); 
		
		return builder.build() ; 
	}
	/**
	 * 校验响应数据的正确性,这里默认服务器已经成功返回消息，并且消息没有错误
	 * <p>
	 * 作用：检查服务器返回的 response 中的数据值是否符合预期。
	 * 检查逻辑：
	 *      1. 如果某字段必须有值，那么是否有值？
	 *      2. 如果字段有值，那么这个值是否在合理范围内？
	 *
	 * @param client   当前客户端对象（包含最新状态）
	 * @param request  刚才发送的请求消息（用于上下文对比）,必定不是null
	 * @param response 服务器返回的响应消息,必定不是null
	 * @return 错误描述，如果为null或者空字符串表示没有错误
	 */
	public String verifyResponse(Client client, cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeRequest_16000050 requestMessage, cn.game.protocol.protobuf.HeroMsg.HeroFragmentComposeResponse_16000051 responseMessage) {
		
		return null; 
	}

	
	public static void main(String args[]) throws Exception {
	    HeroFragmentComposeRequest_16000050Test instance = new HeroFragmentComposeRequest_16000050Test();
	    instance.start();
	}

}