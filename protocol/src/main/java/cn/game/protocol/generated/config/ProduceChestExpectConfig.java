package cn.game.protocol.generated.config;
import org.w3c.dom.Element;

/**
 * 宝箱各资源的预期量
 * 
 * 工具生成的，不要手动修改
 */
 public class ProduceChestExpectConfig {

	/** id */
	private int id;		
	/** 商品ID */
	private int goodsId;		
	/** 数量 */
	private int numbers;		
	/** 预期量 */
	private int expect;		

	public ProduceChestExpectConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.goodsId = Integer.parseInt(element.getAttribute("goodsId") == null || element.getAttribute("goodsId").length() == 0 ? "0"
			: element.getAttribute("goodsId")); // 商品ID
		this.numbers = Integer.parseInt(element.getAttribute("numbers") == null || element.getAttribute("numbers").length() == 0 ? "0"
			: element.getAttribute("numbers")); // 数量
		this.expect = Integer.parseInt(element.getAttribute("expect") == null || element.getAttribute("expect").length() == 0 ? "0"
			: element.getAttribute("expect")); // 预期量
	}
	
	public int getId() {
		return id;
	}
	
	public int getGoodsId() {
		return goodsId;
	}
	
	public int getNumbers() {
		return numbers;
	}
	
	public int getExpect() {
		return expect;
	}
	
}
