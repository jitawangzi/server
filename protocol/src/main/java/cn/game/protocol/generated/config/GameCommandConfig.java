package cn.game.protocol.generated.config;

import cn.game.protocol.generated.enume.GameCommandEnum;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 命令列表
 * 
 * 工具生成的，不要手动修改
 */
 public class GameCommandConfig {

	/** id -- id */
	private final int id;		
	/** 类型 -- 类型 */
	private final GameCommandEnum type;		
	/** 参数列表 -- 参数列表 */
	private final List<Integer> parameterList;		

	public GameCommandConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.type = GameCommandEnum.get(Integer.parseInt(element.getAttribute("type")));	// 类型
		String parameterListString = element.getAttribute("parameterList"); // 参数列表
		if (parameterListString != null && parameterListString.length() > 0) {
			String[] parameterListStrings = parameterListString.split("\\|"); 
			List<Integer> parameterList = new ArrayList<Integer>(parameterListStrings.length) ; 
			for (int i = 0; i < parameterListStrings.length; i++) {
				Integer temp = Integer.parseInt(parameterListStrings[i]);
				parameterList.add(temp);
			}
			this.parameterList = com.google.common.collect.ImmutableList.copyOf(parameterList);						
		} else {
			this.parameterList = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public GameCommandEnum getType() {
		return type;
	}
	
	public List<Integer> getParameterList() {
		return parameterList;
	}
	
}
