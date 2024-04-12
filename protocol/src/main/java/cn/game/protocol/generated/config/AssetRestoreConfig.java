package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 恢复
 * 
 * 工具生成的，不要手动修改
 */
 public class AssetRestoreConfig {

	/** 能量ID */
	public final int ID;		
	/** 多少分钟恢复1点 */
	public final int interval;		
	/** 显示的自动恢复上限 */
	public final int maxShow;		
	/** 自动恢复上限的倍数 */
	public final int maxMultiple;		
	/** 额外增加的上限类型 */
	public final int maxType;		
	/** 额外增加的上限值 */
	public final int maxValue;		

	public AssetRestoreConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 能量ID
		interval = Integer.parseInt(element.getAttribute("interval") == null || element.getAttribute("interval").length() == 0 ? "0"
			: element.getAttribute("interval")); // 多少分钟恢复1点
		maxShow = Integer.parseInt(element.getAttribute("maxShow") == null || element.getAttribute("maxShow").length() == 0 ? "0"
			: element.getAttribute("maxShow")); // 显示的自动恢复上限
		maxMultiple = Integer.parseInt(element.getAttribute("maxMultiple") == null || element.getAttribute("maxMultiple").length() == 0 ? "0"
			: element.getAttribute("maxMultiple")); // 自动恢复上限的倍数
		maxType = Integer.parseInt(element.getAttribute("maxType") == null || element.getAttribute("maxType").length() == 0 ? "0"
			: element.getAttribute("maxType")); // 额外增加的上限类型
		maxValue = Integer.parseInt(element.getAttribute("maxValue") == null || element.getAttribute("maxValue").length() == 0 ? "0"
			: element.getAttribute("maxValue")); // 额外增加的上限值
	}
	

}
