package com.jhj.service.share;

import java.util.List;

import com.jhj.po.model.share.OrderShare;

public interface OrderShareService {
	int deleteByPrimaryKey(Integer id);

    int insert(OrderShare record);

    int insertSelective(OrderShare record);

    OrderShare selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderShare record);

    int updateByPrimaryKey(OrderShare record);
    
    List<OrderShare> selectByShareId(Integer shareId);
    
    OrderShare selectByShareIdAndUserId(Integer shareId,Integer userId);
}
