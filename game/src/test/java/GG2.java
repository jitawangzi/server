import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.game.protocol.generated.config.RoleFettersConfig;
import cn.game.protocol.generated.manager.RoleFettersManager;
import cn.game.util.BinarySearch;

public class GG2 {

	public static void main(String[] args) throws Exception {
		
		Collection<RoleFettersConfig> cc = RoleFettersManager.getInstance().list(); 
		
		List<RoleFettersConfig> list = new ArrayList<>();
		list.addAll(cc) ; 
		RoleFettersConfig config = new RoleFettersConfig(null) ; 
		
		BinarySearch.searchFirstBig(list, config); 
		
	}

}
