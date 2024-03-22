import cn.game.core.base.ServerContext;
import cn.game.core.net.vertx.VxHolder;
import cn.game.util.ServerType;

public class TVxHolderTest {

	public static void main(String[] args) throws Exception {
		ServerContext.getInstance().setServerType(ServerType.World);
		VxHolder.init();
		VxHolder.vertx.setPeriodic(5000, r -> {
			System.out.println("exec...");
		});

	}


}
