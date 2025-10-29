package cn.game.simulation.test.gen;

import java.util.Collection;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

import cn.game.protocol.generated.config.ChapterPacksConfig;
import cn.game.protocol.generated.manager.ChapterPacksManager;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.base.ServerTest;

@Component
public class ShopChapterPacksBuyRequest_15000020Test extends ServerTest {

	@Override
	public Message getMessage(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyRequest_15000020.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyRequest_15000020
				.newBuilder();
		Collection<ChapterPacksConfig> list = ChapterPacksManager.instance().list();
		if (list != null) {
			for (ChapterPacksConfig chapterPacksConfig : list) {
				builder.setId(chapterPacksConfig.ID);
				break;
			}
		} else {
			builder.setId(1);
		}
		return builder.build();
	}

	@Override
	public Message getMessagePressure(Client client) {
		cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyRequest_15000020.Builder builder = cn.game.protocol.protobuf.ShopMsg.ShopChapterPacksBuyRequest_15000020
				.newBuilder();
		Collection<ChapterPacksConfig> list = ChapterPacksManager.instance().list();
		if (list != null) {
			for (ChapterPacksConfig chapterPacksConfig : list) {
				builder.setId(chapterPacksConfig.ID);
				break;
			}
		} else {
			builder.setId(1);
		}
		return builder.build();
	}

	public static void main(String args[]) throws Exception {
		ShopChapterPacksBuyRequest_15000020Test instance = new ShopChapterPacksBuyRequest_15000020Test();
		instance.start();
	}

}