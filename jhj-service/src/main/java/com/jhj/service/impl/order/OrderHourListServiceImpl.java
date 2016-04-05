package com.jhj.service.impl.order;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.jhj.po.dao.order.OrdersMapper;
import com.jhj.po.model.order.Orders;
import com.jhj.po.model.university.PartnerServiceType;
import com.jhj.po.model.user.UserAddrs;
import com.jhj.service.dict.ServiceTypeService;
import com.jhj.service.order.OrderHourListService;
import com.jhj.service.order.OrdersService;
import com.jhj.service.university.PartnerServiceTypeService;
import com.jhj.service.users.UserAddrsService;
import com.jhj.vo.OrderSearchVo;
import com.jhj.vo.order.OrderHourListVo;
import com.meijia.utils.BeanUtilsExp;
import com.meijia.utils.OneCareUtil;

/**
 *
 * @author :hulj
 * @Date : 2015年8月4日上午11:42:33
 *
 */
@Service
public class OrderHourListServiceImpl implements OrderHourListService {
	
	@Autowired
	private OrdersMapper orderMapper;
	
	@Autowired
	private UserAddrsService userAddrService;
	@Autowired
	private ServiceTypeService dictServiceTypeSerivice;
	
	@Autowired
	private OrdersService orderService;
	
	@Autowired
	private PartnerServiceTypeService partService;
	
	/*
	 * 当前订单
	 */
	@Override
	public List<Orders> selectNowOrderHourListByUserId(Long userId,int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		
		OrderSearchVo orderSearchVo = new OrderSearchVo();
		orderSearchVo.setUserId(userId);
		List<Orders> list = orderMapper.selectNowOrderHourByListPage(orderSearchVo);
		
		return list;
	}
	
	/*
	 * 历史订单
	 */
	@Override
	public List<Orders> selectOldOrderHourListByUserId(Long userId,int pageNo, int pageSize) {
	PageHelper.startPage(pageNo, pageSize);
		
		OrderSearchVo orderSearchVo = new OrderSearchVo();
		orderSearchVo.setUserId(userId);
		List<Orders> list = orderMapper.selectOldOrderHourByListPage(orderSearchVo);
		
		return list;
	}

	
	
	
	/*
	 * 设置 vo 相关字段供 展示
	 */
	@Override
	public List<OrderHourListVo> transOrderHourListVo(List<Orders> orderHourList){
		
		
		List<OrderHourListVo> voList = new  ArrayList<OrderHourListVo>();
		
		//可能在滑动到第二页时。已没有记录，则返回null即可
		if(!orderHourList.isEmpty() || orderHourList.size()>0){
			
			for (Orders orders : orderHourList) {
				OrderHourListVo orderHourListVo = initOHLV();
				BeanUtilsExp.copyPropertiesIgnoreNull(orders, orderHourListVo);
				
				//订单类型
				Short orderType = orders.getOrderType();
				
				
				/*
				 * 用户版-我的订单，对应的钟点工，助理预约单，改相应的名字
				 * 
				 *  2016年4月5日15:36:403  将 订单的大类型。。变为 具体 的 服务类型名称 
				 *  
				 *  如： 钟点工-->  厨娘烧饭初体验。
				 */
				
				Long typeId = orders.getServiceType();
				
				PartnerServiceType partType = partService.selectByPrimaryKey(typeId);
				
				// 需要 区分 
				
				orderHourListVo.setOrderHourTypeName(partType.getName());
				
				//订单状态
				Short orderStatus = orders.getOrderStatus();
				
				orderHourListVo.setOrderHourStatusName(OneCareUtil.
							getJhjOrderStausNameFromOrderType(orderType,orderStatus));
				
				//地址 
				UserAddrs userAddrs = userAddrService.selectByPrimaryKey(orders.getAddrId());
				
				orderHourListVo.setAddrId(orders.getAddrId());
				if(userAddrs!=null){
					orderHourListVo.setAddress(userAddrs.getName()+" "+userAddrs.getAddr());
				}
				
				voList.add(orderHourListVo);
			}
		}
		return voList;
	}
	@Override
	public OrderHourListVo initOHLV() {
		Orders orders = orderService.initOrders();
		OrderHourListVo  hourListVo = new OrderHourListVo();
		
		BeanUtilsExp.copyPropertiesIgnoreNull(orders, hourListVo);
		hourListVo.setOrderHourTypeName("");
		hourListVo.setOrderHourStatusName("");
		hourListVo.setAddrId(0L);
		hourListVo.setAddress("");
		
		return hourListVo;
	}
}
