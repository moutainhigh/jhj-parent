package com.jhj.service.bs;

import java.util.List;

import com.jhj.po.model.bs.OrgStaffDetailDept;
import com.jhj.vo.order.OrderSearchVo;


/**
 *
 * @author :hulj
 * @Date : 2015年7月1日上午11:16:44
 * @Description: TODO
 *
 */
public interface OrgStaffDetailDeptService {
	int deleteByPrimaryKey(Long id);

    int insert(OrgStaffDetailDept record);

    int insertSelective(OrgStaffDetailDept record);

    OrgStaffDetailDept selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrgStaffDetailDept record);

    int updateByPrimaryKey(OrgStaffDetailDept record);
    
    OrgStaffDetailDept initOrgStaffDetailDept();

	List<OrgStaffDetailDept> selectBySearchVo(OrderSearchVo searchVo);


}
