import java.util.HashMap;

import cn.game.util.JsonUtil;

public class GG2 {

	public static void main(String[] args) throws Exception {
		
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(1, 3);
		map.put(2, 3);
		
		String jsonString = JsonUtil.toJsonString(map);
		System.out.println(jsonString);
		
		
//		Collection<RoleFettersConfig> cc = RoleFettersManager.getInstance().list(); 
//		
//		List<RoleFettersConfig> list = new ArrayList<>();
//		list.addAll(cc) ; 
//		RoleFettersConfig config = new RoleFettersConfig(null) ; 
//		
//		BinarySearch.searchFirstBig(list, config); 
//		
	}

}
