package cn.game.util.db;


/**
 * Mybatis中操作json的参数
 * 2021年4月6日 下午2:44:38
 * @author SYQ
 */
public class MbJsonParamObject {

	/** 待操作的json格式的列 */
	private String column;
	/** 待操作的json array index */
	private int index;
	/** 数据行主键 */
	private Object pk;
	/** 值，待更新或者插入的值 */
	private String value;
	/** key,一般在json中使用，array中不使用 */
	private String key;

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Object getPk() {
		return pk;
	}

	public void setPk(Object pk) {
		this.pk = pk;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
