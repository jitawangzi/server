/**
  * Copyright 2022 json.cn 
  */
package cn.game.protocol.steam.model;
import java.util.List;

/**
 * Auto-generated: 2022-09-29 17:23:5
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class Methods {

	private String name;
	private int version;
	private String httpmethod;
	private String description;
	private List<MethodParam> parameters;

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	public int getVersion() {
		return version;
	}

	public void setHttpmethod(String httpmethod) {
		this.httpmethod = httpmethod;
	}
	public String getHttpmethod() {
		return httpmethod;
	}

	public List<MethodParam> getParameters() {
		return parameters;
	}

	public void setParameters(List<MethodParam> parameters) {
		this.parameters = parameters;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}