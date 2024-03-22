package cn.game.protocol.generated.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.w3c.dom.Element;
import org.apache.commons.lang.StringUtils;

/**
 * 通行证任务
 * 
 * 工具生成的，不要手动修改
 */
 public class TaskMainConfig 
{

	/** 描述 */
	private int id;		
	/** 枚举类型 */
	private int taskType;		
	/** 枚举参数 */
	private int enumType;		
	/** 任务奖励 */
	private List<List<Integer>> enumParameter;		
	/** 任务奖励 */
	private List<Entry<Integer,Integer>> taskMedal;		

	public TaskMainConfig (Element element)
	{
	
		this.id = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("id")) ? "0"
			: element.getAttribute("id")); // id
		this.taskType = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("taskType")) ? "0"
			: element.getAttribute("taskType")); // 任务类型
		this.enumType = Integer.parseInt(StringUtils.isEmpty(element.getAttribute("enumType")) ? "0"
			: element.getAttribute("enumType")); // 枚举类型
		String enumParameter = element.getAttribute("enumParameter"); // 枚举参数
		if (enumParameter != null && enumParameter.length() > 0)
		{
			String[] enumParameterStrings = enumParameter.split("\\|"); 
			this.enumParameter = new ArrayList<List<Integer>>(enumParameterStrings.length) ; 
			for (int i = 0; i < enumParameterStrings.length; i++) 
			{
				String[] enumParameterStrings2 = enumParameterStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(enumParameterStrings2.length) ; 
				for (int j = 0; j < enumParameterStrings2.length; j++) 
				{
					Integer temp = Integer.valueOf(enumParameterStrings2[j]);
					list.add(temp) ; 
				}
				this.enumParameter.add(list);
			}
		}
		else 
		{
			this.enumParameter = new ArrayList<List<Integer>>(0);
		}
		String taskMedal = element.getAttribute("taskMedal"); // 任务奖励
		if (taskMedal != null && taskMedal.length() > 0)
		{
			String[] taskMedalStrings = taskMedal.split("\\|"); 
			this.taskMedal = new ArrayList<Entry<Integer,Integer>>(taskMedalStrings.length) ; 
			for (String string : taskMedalStrings)
			{
			    String[] split = string.split(":");
			    if (split.length != 2) {
					throw new IllegalArgumentException(" 表： " + this.getClass().getSimpleName() + " id  : " + this.id + " 字段: "
							+ "taskMedal" + "需为key-value格式，用冒号分隔");
				} 
				this.taskMedal.add(new Entry<Integer,Integer>()
				{
					@Override
					public Integer setValue(Integer value)
					{
						return null;
					}
					@Override
					public Integer getValue()
					{
						return Integer.valueOf(split[1]);
					}
					@Override
					public Integer getKey()
					{
						return Integer.valueOf(split[0]);
					}
				}) ; 
			}
		}
		else 
		{
			this.taskMedal = new ArrayList<Entry<Integer,Integer>>(0);
		}
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public int getTaskType()
	{
		return this.taskType;
	}
	
	public int getEnumType()
	{
		return this.enumType;
	}
	
	public List<List<Integer>> getEnumParameter()
	{
		return this.enumParameter;
	}
	
	public List<Entry<Integer,Integer>> getTaskMedal()
	{
		return this.taskMedal;
	}
	
}
