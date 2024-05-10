package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 排行榜
 * 
 * 工具生成的，不要手动修改
 */
 public class RankConfig {

	/** ID */
	public final int ID;		
	/** 榜单名称 */
	public final String Name;		
	/** 所属系统 */
	public final int Type;		
	/** 上榜要求（前多少） */
	public final int Request;		
	/** 上榜人数 */
	public final int ShowCnt;		
	/** 统计人数 */
	public final int StatisticsCnt;		
	/** 结算周期 */
	public final int Period;		
	/** 奖励区间（1名） */
	public final int[][] First;		
	/** 奖励区间（2名） */
	public final int[][] Second;		
	/** 奖励区间（3名） */
	public final int[][] Third;		
	/** 奖励区间（前10） */
	public final int[][] Tenth;		
	/** 奖励区间（前20） */
	public final int[][] Twentieth;		
	/** 奖励区间（前50） */
	public final int[][] Fiftieth;		
	/** 奖励区间（前100） */
	public final int[][] Hundredth;		
	/** 奖励区间（前200） */
	public final int[][] TwoHundredth;		
	/** 奖励区间（上榜） */
	public final int[][] List;		

	public RankConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Name = element.getAttribute("Name"); // 榜单名称
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 所属系统
		Request = Integer.parseInt(element.getAttribute("Request") == null || element.getAttribute("Request").length() == 0 ? "0"
			: element.getAttribute("Request")); // 上榜要求（前多少）
		ShowCnt = Integer.parseInt(element.getAttribute("ShowCnt") == null || element.getAttribute("ShowCnt").length() == 0 ? "0"
			: element.getAttribute("ShowCnt")); // 上榜人数
		StatisticsCnt = Integer.parseInt(element.getAttribute("StatisticsCnt") == null || element.getAttribute("StatisticsCnt").length() == 0 ? "0"
			: element.getAttribute("StatisticsCnt")); // 统计人数
		Period = Integer.parseInt(element.getAttribute("Period") == null || element.getAttribute("Period").length() == 0 ? "0"
			: element.getAttribute("Period")); // 结算周期
		String FirstString = element.getAttribute("First"); // 奖励区间（1名）
		if (FirstString != null && FirstString.length() > 0) {
			String[] FirstStrings = FirstString.split("\\|"); 
			int[][] FirstTemp = new int[FirstStrings.length][] ; 
			for (int i = 0; i < FirstStrings.length; i++) {
				String[] FirstStrings2 = FirstStrings[i].split(";"); 
				int[] array = new int[FirstStrings2.length];
				for (int j = 0; j < FirstStrings2.length; j++) {
					int temp = Integer.parseInt(FirstStrings2[j]);	
					array[j] = temp;
				}
				FirstTemp[i] = array;
			}
			First = FirstTemp ;			
		} else {
			First = new int[][] {};
		}
		String SecondString = element.getAttribute("Second"); // 奖励区间（2名）
		if (SecondString != null && SecondString.length() > 0) {
			String[] SecondStrings = SecondString.split("\\|"); 
			int[][] SecondTemp = new int[SecondStrings.length][] ; 
			for (int i = 0; i < SecondStrings.length; i++) {
				String[] SecondStrings2 = SecondStrings[i].split(";"); 
				int[] array = new int[SecondStrings2.length];
				for (int j = 0; j < SecondStrings2.length; j++) {
					int temp = Integer.parseInt(SecondStrings2[j]);	
					array[j] = temp;
				}
				SecondTemp[i] = array;
			}
			Second = SecondTemp ;			
		} else {
			Second = new int[][] {};
		}
		String ThirdString = element.getAttribute("Third"); // 奖励区间（3名）
		if (ThirdString != null && ThirdString.length() > 0) {
			String[] ThirdStrings = ThirdString.split("\\|"); 
			int[][] ThirdTemp = new int[ThirdStrings.length][] ; 
			for (int i = 0; i < ThirdStrings.length; i++) {
				String[] ThirdStrings2 = ThirdStrings[i].split(";"); 
				int[] array = new int[ThirdStrings2.length];
				for (int j = 0; j < ThirdStrings2.length; j++) {
					int temp = Integer.parseInt(ThirdStrings2[j]);	
					array[j] = temp;
				}
				ThirdTemp[i] = array;
			}
			Third = ThirdTemp ;			
		} else {
			Third = new int[][] {};
		}
		String TenthString = element.getAttribute("Tenth"); // 奖励区间（前10）
		if (TenthString != null && TenthString.length() > 0) {
			String[] TenthStrings = TenthString.split("\\|"); 
			int[][] TenthTemp = new int[TenthStrings.length][] ; 
			for (int i = 0; i < TenthStrings.length; i++) {
				String[] TenthStrings2 = TenthStrings[i].split(";"); 
				int[] array = new int[TenthStrings2.length];
				for (int j = 0; j < TenthStrings2.length; j++) {
					int temp = Integer.parseInt(TenthStrings2[j]);	
					array[j] = temp;
				}
				TenthTemp[i] = array;
			}
			Tenth = TenthTemp ;			
		} else {
			Tenth = new int[][] {};
		}
		String TwentiethString = element.getAttribute("Twentieth"); // 奖励区间（前20）
		if (TwentiethString != null && TwentiethString.length() > 0) {
			String[] TwentiethStrings = TwentiethString.split("\\|"); 
			int[][] TwentiethTemp = new int[TwentiethStrings.length][] ; 
			for (int i = 0; i < TwentiethStrings.length; i++) {
				String[] TwentiethStrings2 = TwentiethStrings[i].split(";"); 
				int[] array = new int[TwentiethStrings2.length];
				for (int j = 0; j < TwentiethStrings2.length; j++) {
					int temp = Integer.parseInt(TwentiethStrings2[j]);	
					array[j] = temp;
				}
				TwentiethTemp[i] = array;
			}
			Twentieth = TwentiethTemp ;			
		} else {
			Twentieth = new int[][] {};
		}
		String FiftiethString = element.getAttribute("Fiftieth"); // 奖励区间（前50）
		if (FiftiethString != null && FiftiethString.length() > 0) {
			String[] FiftiethStrings = FiftiethString.split("\\|"); 
			int[][] FiftiethTemp = new int[FiftiethStrings.length][] ; 
			for (int i = 0; i < FiftiethStrings.length; i++) {
				String[] FiftiethStrings2 = FiftiethStrings[i].split(";"); 
				int[] array = new int[FiftiethStrings2.length];
				for (int j = 0; j < FiftiethStrings2.length; j++) {
					int temp = Integer.parseInt(FiftiethStrings2[j]);	
					array[j] = temp;
				}
				FiftiethTemp[i] = array;
			}
			Fiftieth = FiftiethTemp ;			
		} else {
			Fiftieth = new int[][] {};
		}
		String HundredthString = element.getAttribute("Hundredth"); // 奖励区间（前100）
		if (HundredthString != null && HundredthString.length() > 0) {
			String[] HundredthStrings = HundredthString.split("\\|"); 
			int[][] HundredthTemp = new int[HundredthStrings.length][] ; 
			for (int i = 0; i < HundredthStrings.length; i++) {
				String[] HundredthStrings2 = HundredthStrings[i].split(";"); 
				int[] array = new int[HundredthStrings2.length];
				for (int j = 0; j < HundredthStrings2.length; j++) {
					int temp = Integer.parseInt(HundredthStrings2[j]);	
					array[j] = temp;
				}
				HundredthTemp[i] = array;
			}
			Hundredth = HundredthTemp ;			
		} else {
			Hundredth = new int[][] {};
		}
		String TwoHundredthString = element.getAttribute("TwoHundredth"); // 奖励区间（前200）
		if (TwoHundredthString != null && TwoHundredthString.length() > 0) {
			String[] TwoHundredthStrings = TwoHundredthString.split("\\|"); 
			int[][] TwoHundredthTemp = new int[TwoHundredthStrings.length][] ; 
			for (int i = 0; i < TwoHundredthStrings.length; i++) {
				String[] TwoHundredthStrings2 = TwoHundredthStrings[i].split(";"); 
				int[] array = new int[TwoHundredthStrings2.length];
				for (int j = 0; j < TwoHundredthStrings2.length; j++) {
					int temp = Integer.parseInt(TwoHundredthStrings2[j]);	
					array[j] = temp;
				}
				TwoHundredthTemp[i] = array;
			}
			TwoHundredth = TwoHundredthTemp ;			
		} else {
			TwoHundredth = new int[][] {};
		}
		String ListString = element.getAttribute("List"); // 奖励区间（上榜）
		if (ListString != null && ListString.length() > 0) {
			String[] ListStrings = ListString.split("\\|"); 
			int[][] ListTemp = new int[ListStrings.length][] ; 
			for (int i = 0; i < ListStrings.length; i++) {
				String[] ListStrings2 = ListStrings[i].split(";"); 
				int[] array = new int[ListStrings2.length];
				for (int j = 0; j < ListStrings2.length; j++) {
					int temp = Integer.parseInt(ListStrings2[j]);	
					array[j] = temp;
				}
				ListTemp[i] = array;
			}
			List = ListTemp ;			
		} else {
			List = new int[][] {};
		}
	}
	

}
