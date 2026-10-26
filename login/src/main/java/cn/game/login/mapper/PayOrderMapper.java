package cn.game.login.mapper;

import cn.game.login.cache.entity.PayOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PayOrderMapper {

    /** @param row 已校验身份的回执字段 @param delivered 仅 Game 明确提交成功时为 true @return 匹配并更新的行数 */
    int mergePaymentReceipt(@Param("row") PayOrder row, @Param("delivered") boolean delivered);

	/**
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int insert(PayOrder row);

	/**
	 * @mbg.generated
	 */
	int insertSelective(PayOrder row);

	/**
	 * @mbg.generated
	 */
	PayOrder selectByPrimaryKey(Long id);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(PayOrder row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKeyWithBLOBs(PayOrder row);

	/**
	 * @mbg.generated
	 */
	int updateByPrimaryKey(PayOrder row);

	/**
	 * @mbg.generated
	 */
	List<PayOrder> selectAll();

	/**
	 * @mbg.generated
	 */
	List<PayOrder> getBatchOffset(@Param("offset") int offset, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<PayOrder> getBatchCursor(@Param("lastId") Long lastId, @Param("limit") int limit);

	/**
	 * @mbg.generated
	 */
	List<PayOrder> selectByThirdOrderId(@Param("thirdOrderId") String thirdOrderId);

	/**
	 * @mbg.generated
	 */
	int insertOrUpdate(PayOrder record);

	public List<PayOrder> selectOrderList(
			@Param("playerId") Long playerId,
            @Param("status") Integer status,
            @Param("selfOrderId") String selfOrderId,
            @Param("start") Integer page,
            @Param("end") Integer pageSize
	);
}
