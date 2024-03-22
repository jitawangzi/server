package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 掉落
 * 
 * 工具生成的，不要手动修改
 */
 public class DropConfig 
{

	/** 26打头用作礼包 */
	private int id;		
	/** 掉落类型 */
	private int dropType;		
	/** 最大掉落次数，当掉落类型是1的时候用到 */
	private int maxCount;		
	/** 奖励id */
	private List<Integer> itemId;		
	/** 数量 */
	private List<Integer> count;		
	/** 权重 */
	private List<Integer> weight;		

	public DropConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // 掉落id
		this.dropType = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("dropType")) ? "0"
			: element.getAttribute("dropType")); // 掉落类型
		this.maxCount = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("maxCount")) ? "0"
			: element.getAttribute("maxCount")); // 最大掉落次数
		String itemId = element.getAttribute("itemId"); // 奖励id
		if (itemId != null && itemId.length() > 0)
		{
			String[] itemIdStrings = itemId.split(";"); 
			this.itemId = new ArrayList<Integer>(itemIdStrings.length) ; 
			for (int i = 0; i < itemIdStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(itemIdStrings[i]);
				this.itemId.add(temp);
			}
		}
		else 
		{
			this.itemId = new ArrayList<Integer>(0);
		}
		String count = element.getAttribute("count"); // 数量
		if (count != null && count.length() > 0)
		{
			String[] countStrings = count.split(";"); 
			this.count = new ArrayList<Integer>(countStrings.length) ; 
			for (int i = 0; i < countStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(countStrings[i]);
				this.count.add(temp);
			}
		}
		else 
		{
			this.count = new ArrayList<Integer>(0);
		}
		String weight = element.getAttribute("weight"); // 权重
		if (weight != null && weight.length() > 0)
		{
			String[] weightStrings = weight.split(";"); 
			this.weight = new ArrayList<Integer>(weightStrings.length) ; 
			for (int i = 0; i < weightStrings.length; i++) 
			{
				Integer temp = Integer.valueOf(weightStrings[i]);
				this.weight.add(temp);
			}
		}
		else 
		{
			this.weight = new ArrayList<Integer>(0);
		}
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getDropType()
	{
		return this.dropType;
	}
	
	public int getMaxCount()
	{
		return this.maxCount;
	}
	
	public List<Integer> getItemId()
	{
		return this.itemId;
	}
	
	public List<Integer> getCount()
	{
		return this.count;
	}
	
	public List<Integer> getWeight()
	{
		return this.weight;
	}
	
}
