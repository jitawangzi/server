package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 随机名字
 * 
 * 工具生成的，不要手动修改
 */
 public class RandomNameConfig {

	/** 索引 */
	public final int ID;		
	/** 姓氏 */
	public final String Familyname;		
	/** 男名1 */
	public final String MenName1;		
	/** 男名2 */
	public final String MenName2;		
	/** 女名1 */
	public final String WomenName1;		
	/** 女名2 */
	public final String WomenName2;		

	public RandomNameConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 索引
		Familyname = element.getAttribute("Familyname"); // 姓氏
		MenName1 = element.getAttribute("MenName1"); // 男名1
		MenName2 = element.getAttribute("MenName2"); // 男名2
		WomenName1 = element.getAttribute("WomenName1"); // 女名1
		WomenName2 = element.getAttribute("WomenName2"); // 女名2
	}
	

}
