package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 挑战任务子表
 * 
 * 工具生成的，不要手动修改
 */
 public class MissionChallengeGroupConfig {

	/** id -- 任务id */
	private final int id;		
	/** 标记 -- 初始化任务 */
	private final boolean sign;		
	/** 后置条件 -- 后置条件 */
	private final List<Integer> openGroupId;		
	/** 任务id -- 任务id */
	private final List<Integer> missionId;		
	/** 任务说明 -- 描述 */
	private final int type;		
	/** 组条件 -- 组条件 */
	private final int chapterLimit;		
	/** 任务组奖励 -- 任务奖励 */
	private final List<Entry<Integer,Integer>> reward;		

	public MissionChallengeGroupConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.sign = Boolean.parseBoolean(element.getAttribute("sign") == null || element.getAttribute("sign").length() == 0 ? "false"
			: element.getAttribute("sign")); // 标记
		String openGroupIdString = element.getAttribute("openGroupId"); // 后置条件
		if (openGroupIdString != null && openGroupIdString.length() > 0) {
			String[] openGroupIdStrings = openGroupIdString.split("\\|"); 
			List<Integer> openGroupId = new ArrayList<Integer>(openGroupIdStrings.length) ; 
			for (int i = 0; i < openGroupIdStrings.length; i++) {
				Integer temp = Integer.parseInt(openGroupIdStrings[i]);
				openGroupId.add(temp);
			}
			this.openGroupId = com.google.common.collect.ImmutableList.copyOf(openGroupId);						
		} else {
			this.openGroupId = java.util.Collections.emptyList();
		}
		String missionIdString = element.getAttribute("missionId"); // 任务id
		if (missionIdString != null && missionIdString.length() > 0) {
			String[] missionIdStrings = missionIdString.split("\\|"); 
			List<Integer> missionId = new ArrayList<Integer>(missionIdStrings.length) ; 
			for (int i = 0; i < missionIdStrings.length; i++) {
				Integer temp = Integer.parseInt(missionIdStrings[i]);
				missionId.add(temp);
			}
			this.missionId = com.google.common.collect.ImmutableList.copyOf(missionId);						
		} else {
			this.missionId = java.util.Collections.emptyList();
		}
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 任务说明
		this.chapterLimit = Integer.parseInt(element.getAttribute("chapterLimit") == null || element.getAttribute("chapterLimit").length() == 0 ? "0"
			: element.getAttribute("chapterLimit")); // 组条件
		String rewardString = element.getAttribute("reward"); // 任务组奖励
		if (rewardString != null && rewardString.length() > 0) {
			String[] rewardStrings = rewardString.split("\\|"); 
			List<Entry<Integer,Integer>> reward = new ArrayList<Entry<Integer,Integer>>(rewardStrings.length) ; 
			for (int i = 0; i < rewardStrings.length; i++) {
			    String[] split = rewardStrings[i].split(":", 2);
				reward.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						try {
							return Integer.parseInt(split[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();	
					}
					@Override
					public Integer getKey() {
						try {
							return Integer.parseInt(split[0]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();
					}
				}) ; 
			}

			this.reward = com.google.common.collect.ImmutableList.copyOf(reward);						
		} else {
			this.reward = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public boolean getSign() {
		return sign;
	}
	
	public List<Integer> getOpenGroupId() {
		return openGroupId;
	}
	
	public List<Integer> getMissionId() {
		return missionId;
	}
	
	public int getType() {
		return type;
	}
	
	public int getChapterLimit() {
		return chapterLimit;
	}
	
	public List<Entry<Integer,Integer>> getReward() {
		return reward;
	}
	
}
