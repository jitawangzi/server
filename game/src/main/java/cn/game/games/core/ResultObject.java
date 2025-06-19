package cn.game.games.core;


/**
 * 返回值对象
 */
public class ResultObject<T> {
	private static final ResultObject SUCCESS = new ResultObject();
	
	/** 错误码 */
	private int errorCode;

	/** 返回的对象 */
	private T value;

	private ResultObject() { }
	public T getValue() {
		return value;
	}

	public int getErrorCode() {
		return errorCode;
	}
	/**
	 * 是否成功
	 * @return
	 */
	public boolean isOK(){
		return errorCode == 0;
	}

	public boolean isFail() {
		return errorCode != 0;
	}
	
	/**
	 * 成功的返回值 
	 * 
	 * @param 
	 * @return 
	 */
	public static <T> ResultObject<T> success() {
		return SUCCESS;
	}

	/**
	 * 成功的返回值 
	 * 
	 * @param <T>
	 * @param  value	返回所带的对象
	 * @return 返回值对象
	 */
	public static <T> ResultObject<T> success(T value) {
		ResultObject<T> resultObject = new ResultObject<T>();
		resultObject.value = value;
		return resultObject;
	}

	/**
	 * 错误返回
	 * 
	 * @param  errorCode	错误码
	 */
	public static <T> ResultObject<T> fail(int errorCode) {
		ResultObject<T> resultObject = new ResultObject<T>();
		resultObject.errorCode = errorCode;
		return resultObject;
	}

}
