package cn.game.games.cache.base;

import com.alibaba.fastjson.JSONObject;

public abstract class CacheEntity {

	//数据变化类型
	public static final byte OP_UNCHANGE = 0; //未修改
	public static final byte OP_INSERT = 1;   //插入
	public static final byte OP_UPDATE = 2;	  //更新
	public static final byte OP_DEL = 3;	  //删除
	
	//缓存修改状态
	private byte op = OP_UNCHANGE;
	
	public String toString(){
		return toTopJSON().toString();
	}
	
	public final JSONObject toTopJSON(){
		JSONObject json = new JSONObject();
		toJSON(json);
		return json;
	}
	
	public void toJSON(JSONObject json){		
	}
	
	/**
	 * 返回当前实体的主键或组合主键，不包含playerId
	 * 在当前角色中可以唯一区别
	 * 
	 * @return
	 */
	public abstract String pk();
	
	protected String calPk(Object... fields){
	
		StringBuilder b = new StringBuilder();
		b.append(this.getClass().getName());
		
		for(Object o : fields){
			b.append("_").append(o);
	}
	return b.toString();
}

	public byte getOp() {
		return op;
	}

	public void setOp(byte op) {
		this.op = op;
	}

	 
}
