package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 剧情选项表
 * 
 * 工具生成的，不要手动修改
 */
 public class StoryOptionConfig {

	/** id -- id */
	private final int id;		
	/** 奖励任务 -- 奖励任务 */
	private final int prizeMission;		
	/** 奖励道具 -- 奖励道具 */
	private final List<Entry<Integer,Integer>> prizeItem;		
	/** 命令列表 -- 命令列表 */
	private final List<Integer> commandList;		

	public StoryOptionConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.prizeMission = Integer.parseInt(element.getAttribute("prizeMission") == null || element.getAttribute("prizeMission").length() == 0 ? "0"
			: element.getAttribute("prizeMission")); // 奖励任务
		String prizeItemString = element.getAttribute("prizeItem"); // 奖励道具
		if (prizeItemString != null && prizeItemString.length() > 0) {
			String[] prizeItemStrings = prizeItemString.split("\\|"); 
			List<Entry<Integer,Integer>> prizeItem = new ArrayList<Entry<Integer,Integer>>(prizeItemStrings.length) ; 
			for (int i = 0; i < prizeItemStrings.length; i++) {
			    String[] split = prizeItemStrings[i].split(":", 2);
				prizeItem.add(new Entry<Integer,Integer>()	{
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

			this.prizeItem = com.google.common.collect.ImmutableList.copyOf(prizeItem);						
		} else {
			this.prizeItem = java.util.Collections.emptyList();
		}
		String commandListString = element.getAttribute("commandList"); // 命令列表
		if (commandListString != null && commandListString.length() > 0) {
			String[] commandListStrings = commandListString.split("\\|"); 
			List<Integer> commandList = new ArrayList<Integer>(commandListStrings.length) ; 
			for (int i = 0; i < commandListStrings.length; i++) {
				Integer temp = Integer.parseInt(commandListStrings[i]);
				commandList.add(temp);
			}
			this.commandList = com.google.common.collect.ImmutableList.copyOf(commandList);						
		} else {
			this.commandList = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getPrizeMission() {
		return prizeMission;
	}
	
	public List<Entry<Integer,Integer>> getPrizeItem() {
		return prizeItem;
	}
	
	public List<Integer> getCommandList() {
		return commandList;
	}
	
}
