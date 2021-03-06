package com.jhj.po.dao.order;

import java.util.List;

import com.jhj.po.model.order.OrderPriceExt;
import com.jhj.vo.order.OrderSearchVo;

public interface OrderPriceExtMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OrderPriceExt record);

    int insertSelective(OrderPriceExt record);

    OrderPriceExt selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrderPriceExt record);

    int updateByPrimaryKey(OrderPriceExt record);
    
	OrderPriceExt selectByOrderNoExt(String orderNoExt);

	List<OrderPriceExt> selectBySearchVo(OrderSearchVo searchVo);

}