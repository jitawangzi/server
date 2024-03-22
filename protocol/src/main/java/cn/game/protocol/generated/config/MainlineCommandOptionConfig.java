package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 主线命令交互物体选项表
 * 
 * 工具生成的，不要手动修改
 */
 public class MainlineCommandOptionConfig {

	/** id -- id */
	private final int id;		
	/** 名称 -- 名称 */
	private final String name;		
	/** 确认文本 -- 填写后表示命令物体需要确认 */
	private final String txt;		
	/** 选项文本列表 */
	private final List<String> txtList;		
	/** 选项命令列表 -- 一一对应选项文本列表 */
	private final List<Integer> commandList;		
	/** 选项反馈文本列表 -- 一一对应选项命令列表 */
	private final List<String> feedbackTxtList;		

	public MainlineCommandOptionConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.name = element.getAttribute("name"); // 名称
		this.txt = element.getAttribute("txt"); // 确认文本
		String txtListString = element.getAttribute("txtList"); // 选项文本列表
		if (txtListString != null && txtListString.length() > 0) {
			String[] txtListStrings = txtListString.split("\\|"); 
			List<String> txtList = new ArrayList<String>(txtListStrings.length) ; 
			for (int i = 0; i < txtListStrings.length; i++) {
				String temp = txtListStrings[i];
				txtList.add(temp);
			}
			this.txtList = com.google.common.collect.ImmutableList.copyOf(txtList);						
		} else {
			this.txtList = java.util.Collections.emptyList();
		}
		String commandListString = element.getAttribute("commandList"); // 选项命令列表
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
		String feedbackTxtListString = element.getAttribute("feedbackTxtList"); // 选项反馈文本列表
		if (feedbackTxtListString != null && feedbackTxtListString.length() > 0) {
			String[] feedbackTxtListStrings = feedbackTxtListString.split("\\|"); 
			List<String> feedbackTxtList = new ArrayList<String>(feedbackTxtListStrings.length) ; 
			for (int i = 0; i < feedbackTxtListStrings.length; i++) {
				String temp = feedbackTxtListStrings[i];
				feedbackTxtList.add(temp);
			}
			this.feedbackTxtList = com.google.common.collect.ImmutableList.copyOf(feedbackTxtList);						
		} else {
			this.feedbackTxtList = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTxt() {
		return txt;
	}
	
	public List<String> getTxtList() {
		return txtList;
	}
	
	public List<Integer> getCommandList() {
		return commandList;
	}
	
	public List<String> getFeedbackTxtList() {
		return feedbackTxtList;
	}
	
}
