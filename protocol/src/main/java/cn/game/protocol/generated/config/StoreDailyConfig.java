package cn.game.protocol.generated.config;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 商店日常
 * 
 * 工具生成的，不要手动修改
 */
 public class StoreDailyConfig 
{

	/** 物品表id */
	private int id;		
	/** 物品名称 */
	private int itemId;		
	/** 原价 */
	private int itemUnitPrice;		
	/** 折扣 */
	private int number;		
	/** 总价 */
	private float discount;		
	/** 限制参数1 */
	private int totalPrcie;		
	/** 限制参数2 */
	private int limitParameters1;		
	/** 是否上架 */
	private int limitParameters2;		
	/** 是否上架 */
	private boolean upDown;		

	public StoreDailyConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // 商品id
		this.itemId = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("itemId")) ? "0"
			: element.getAttribute("itemId")); // 物品表id
		this.itemUnitPrice = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("itemUnitPrice")) ? "0"
			: element.getAttribute("itemUnitPrice")); // 货币类型
		this.number = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("number")) ? "0"
			: element.getAttribute("number")); // 数量
		this.discount = Float.parseFloat(StringUtils.isEmpty(element.getAttribute("discount")) ? "0"
			: element.getAttribute("discount")); // 折扣
		this.totalPrcie = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("totalPrcie")) ? "0"
			: element.getAttribute("totalPrcie")); // 总价
		this.limitParameters1 = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("limitParameters1")) ? "0"
			: element.getAttribute("limitParameters1")); // 限制参数1
		this.limitParameters2 = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("limitParameters2")) ? "0"
			: element.getAttribute("limitParameters2")); // 限制参数2
		this.upDown = Boolean.parseBoolean(StringUtils.isEmpty(element.getAttribute("upDown")) ? "false"
			: element.getAttribute("upDown")); // 是否上架
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getItemId()
	{
		return this.itemId;
	}
	
	public int getItemUnitPrice()
	{
		return this.itemUnitPrice;
	}
	
	public int getNumber()
	{
		return this.number;
	}
	
	public float getDiscount()
	{
		return this.discount;
	}
	
	public int getTotalPrcie()
	{
		return this.totalPrcie;
	}
	
	public int getLimitParameters1()
	{
		return this.limitParameters1;
	}
	
	public int getLimitParameters2()
	{
		return this.limitParameters2;
	}
	
	public boolean getUpDown()
	{
		return this.upDown;
	}
	
}
