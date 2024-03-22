import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import cn.game.games.cache.entity.Item;
import cn.game.games.cache.op.impl.BattlePassOp;
import cn.game.util.JsonUtil;

public class GG {

	static class BT {
		BitSet biSet ; 
		String name ;
		@Override
		public String toString() {
			return "BT [bSet=" + biSet + ", name=" + name + "]";
		} 
			
	}
	
	public static void main(String[] args) throws Exception {

		
		List<Integer> list = new ArrayList<>();
		list.add(3); 
		System.out.println(list.contains(3));
		
		BT op = new BT() ; 
		
		BitSet bitSet = new BitSet();
		bitSet.set(1, true);
		bitSet.set(5, true);
		
		op.biSet = bitSet; 
		op.name = "ddd" ; 

		String jsonString2 = JsonUtil.toJsonString(op);
		System.out.println(jsonString2);
		
		GG.BT object = JsonUtil.parseObject(jsonString2, BT.class); 
		System.out.println(object);

	}
}
