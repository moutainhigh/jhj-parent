package com.jhj.po.dao.period;

import java.util.List;

import com.jhj.po.model.period.PeriodOrder;

public interface PeriodOrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PeriodOrder record);

    int insertSelective(PeriodOrder record);

    PeriodOrder selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PeriodOrder record);

    int updateByPrimaryKey(PeriodOrder record);
    
    PeriodOrder selectByOrderNo(String orderNo);
    
    int insertBatch(List<PeriodOrder> periodOrderList);
    
    List<PeriodOrder> periodOrderListPage(PeriodOrder periodOrder);
}