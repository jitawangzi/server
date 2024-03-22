package cn.game.util;
import java.util.ArrayList;
import java.util.List;

public class ConversionUtil {

	public static List<Long> toLongList(List<String> list) {

		List<Long> ret = new ArrayList<Long>(list.size());
		for (int i = 0; i < list.size(); i++) {
			ret.add(Long.parseLong(list.get(i)));
		}
		return ret;
	}
	public static void main(String[] args) {


	}

}
