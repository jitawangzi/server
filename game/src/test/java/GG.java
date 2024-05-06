import cn.game.util.ByteHelp;
import cn.game.util.HexUtil;

public class GG {
	public static void main(String[] args) {

		byte[] data = new byte[] { -10, 12, 49, 55, 49, 52, 57, 56, 50, 50, 56, 49, 50, 51, 18, 12, 49, 55, 49, 52, 57, 56, 50, 50, 56, 49, 50, 51 };
		String string = new String(data);
//		byte[] bytes = string.getBytes();
//		for (byte b : bytes) {
//			System.out.println(b);
//		}
//		System.out.println(bytes);

		String strhex = ByteHelp.strhex(data);
		System.out.println(strhex);
//		byte[] bytes = strhex.getBytes();
		byte[] bytes = HexUtil.getByteArrayFromHexString(strhex);
		for (byte b : bytes) {
			System.out.println(b);
		}

	}
}
