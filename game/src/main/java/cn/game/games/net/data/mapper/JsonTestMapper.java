package cn.game.games.net.data.mapper;

import java.util.HashMap;

import cn.game.util.db.MbJsonParamObject;
import cn.game.games.cache.entity.JsonTest;

public interface JsonTestMapper {

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(JsonTest row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(JsonTest row);

	/**
	 * @mbg.generated
	 */
	JsonTest selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(JsonTest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(JsonTest row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(JsonTest row);

	int delJson(MbJsonParamObject param);

	int addJson(MbJsonParamObject param);

	int setJson(MbJsonParamObject param);

	int replaceJson(MbJsonParamObject param);

	int addToJsonArrayUseMap(HashMap<String, String> params);

	int delFromJsonArray(MbJsonParamObject param);

	int addToJsonArray(MbJsonParamObject param);

	int updateInJsonArray(MbJsonParamObject param);

}