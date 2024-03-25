import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import cn.game.games.cache.entity.Item;
import cn.game.games.cache.op.impl.BattlePassOp;
import cn.game.util.ByteHelp;
import cn.game.util.JsonUtil;

public class GG {

	public static void main(String[] args) throws Exception {

		long pid = 241070001L ; 
		System.out.println(ByteHelp.toBinaryStringWithZero(pid));
		long id = pid << 32 | 255 ; 
		System.out.println(ByteHelp.toBinaryStringWithZero(id));
		System.out.println(id>>32);
//		System.out.println(241070001);
//		System.out.println(Integer.MAX_VALUE);
		System.out.println();
	}
}
