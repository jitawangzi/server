package cn.game.core.zookeeper;

import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.zookeeper.codec.JsonValueCodec;
import cn.game.util.ZkHelper;

public class ZkBackedCacheFactory {

	public static ZkBackedCache.Builder<String, VirtualServerView> createVirtualServerBuilder() {
        IdExtractor<VirtualServerView> idExtractor = v -> v.getID();
		return new ZkBackedCache.Builder<String, VirtualServerView>().client(ZkHelper.curator)
				.codec(new JsonValueCodec<VirtualServerView>(VirtualServerView.class))
				.idExtractor(idExtractor)
				.keyAdapter(KeyAdapter.stringKey())
				.pathPolicy(PathPolicy.simple(PathPolicy.DEFAULT_VIRTUAL_SERVER_PATH))	;
	}
	public static ZkBackedCache<String, VirtualServerView> createVirtualServerCache() {
		return createVirtualServerBuilder().build();
	}
}
