package cn.game.protocol.manual;

import java.util.HashMap;
import java.util.Map;


public enum UnionTitleEnum {
	
	Leader(1),		//会长
	DeputyLeader(2),		//副会长
	Elite(3),		//精英
	Ordinary(4);		//成员

	private int v;
	
	public int v(){
		return v;
	}
	private UnionTitleEnum(int v){
		this.v = v;
	}
	
	static Map<Integer, UnionTitleEnum> vs = new HashMap<>();
	static{
		vs.put(1, Leader); 
		vs.put(2, DeputyLeader); 
		vs.put(3, Elite); 
		vs.put(4, Ordinary); 
	}
	public static UnionTitleEnum get(int v){
		return vs.get(v);
	}
	
}
