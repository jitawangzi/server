package db;


import com.alibaba.fastjson.JSON;

import cn.game.games.cache.entity.PlayerData;
import cn.game.games.net.data.mapper.JsonTestMapper;
import cn.game.util.SpringContextLoader;
import cn.game.util.db.MbJsonParamObject;


/**
 * @Description Mybatis中json格式数据操作示例
 * 2021年4月6日 下午2:54:41
 * @author SYQ
 */
public class MbJsonTest {

	static JsonTestMapper mapper;

	public static void main(String[] args) throws Exception {
		SpringContextLoader.main(new String[] { "src/main/resources/applicationContext-dataserver.xml",
				"src/main/resources/applicationContext-gameserver.xml" });
		mapper = SpringContextLoader.getContext().getBean(JsonTestMapper.class);
//		addToArray();
//		updateInArray();
//		delFromArray();
//		addJson();
//		delJson();
//		setJson();
		replaceJson();

		System.exit(0);
	}

	public static void addJson() {

		MbJsonParamObject param = new MbJsonParamObject();
		param.setColumn("json_doc");
		param.setPk(1);
		param.setKey("p");
		param.setValue("pp");
		mapper.addJson(param);

	}
	public static void delJson() {

		MbJsonParamObject param = new MbJsonParamObject();
		param.setColumn("json_doc");
		param.setPk(1);
		param.setKey("p");
		mapper.delJson(param);

	}
	public static void setJson() {

		MbJsonParamObject param = new MbJsonParamObject();
		param.setColumn("json_doc");
		param.setPk(1);
		param.setKey("p");
		param.setValue("p");
		mapper.setJson(param);

	}
	public static void replaceJson() {

		MbJsonParamObject param = new MbJsonParamObject();
		param.setColumn("json_doc");
		param.setPk(1);
		param.setKey("p");
		param.setValue("ppppp");
		mapper.replaceJson(param);

	}
	public static void addToArray() {
		PlayerData objectAdd = new PlayerData();
		objectAdd.setPlayerId(333L);
		objectAdd.setExp(33888);

		MbJsonParamObject param = new MbJsonParamObject();
		param.setColumn("json_array");
		param.setIndex(1);
		param.setPk(1);
		param.setValue(JSON.toJSONString(objectAdd));
		mapper.addToJsonArray(param);
	}
	public static void updateInArray() {
		PlayerData objectAdd = new PlayerData();
		objectAdd.setPlayerId(333L);
		objectAdd.setExp(3388822);

		MbJsonParamObject param = new MbJsonParamObject();
		param.setColumn("json_array");
		param.setIndex(1);
		param.setPk(1);
		param.setValue(JSON.toJSONString(objectAdd));
		mapper.updateInJsonArray(param);
	}
	public static void delFromArray() {

		MbJsonParamObject param = new MbJsonParamObject();
		param.setColumn("json_array");
		param.setIndex(1);
		param.setPk(1);
		mapper.delFromJsonArray(param);
	}

}
