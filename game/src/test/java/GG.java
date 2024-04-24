import java.util.HashMap;
import java.util.Map;

public class GG<T extends Number> {

	public static void main(String[] args) throws Exception {

//		RewardInfo rewardInfo1 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(100).setCount(100)).build();
//		RewardInfo rewardInfo2 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(100).setCount(200)).build();
//		RewardInfo rewardInfo3 = RewardInfo.newBuilder().setItem(ItemInfo.newBuilder().setId(100).setCount(300)).build();
//
//		List<RewardInfo> list = new ArrayList<>();
//		list.add(rewardInfo1);
//		list.add(rewardInfo2);
//		list.add(rewardInfo3);
//
//		PlayerHelper.mergeRewards(list);
//
//		for (RewardInfo rewardInfo : list) {
//			System.out.println(rewardInfo);
//		}
//		System.out.println(6 * 300);
//		System.out.println(604800 / 60 / 60 / 24);
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(1, 2);
		map.put(2, 5);
		map.put(3, 6);
		map.keySet().remove(1);

		System.out.println(map.keySet());

	}

}

