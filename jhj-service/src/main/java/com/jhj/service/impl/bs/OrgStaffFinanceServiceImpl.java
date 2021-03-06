package com.jhj.service.impl.bs;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhj.common.Constants;
import com.jhj.po.dao.bs.OrgStaffFinanceMapper;
import com.jhj.po.model.bs.DictCoupons;
import com.jhj.po.model.bs.OrgStaffDetailDept;
import com.jhj.po.model.bs.OrgStaffFinance;
import com.jhj.po.model.bs.OrgStaffs;
import com.jhj.po.model.bs.Orgs;
import com.jhj.po.model.cooperate.CooperativeBusiness;
import com.jhj.po.model.order.OrderDispatchPrices;
import com.jhj.po.model.order.OrderDispatchs;
import com.jhj.po.model.order.OrderPriceExt;
import com.jhj.po.model.order.OrderPrices;
import com.jhj.po.model.order.Orders;
import com.jhj.po.model.university.PartnerServiceType;
import com.jhj.po.model.user.UserAddrs;
import com.jhj.po.model.user.UserCoupons;
import com.jhj.po.model.user.Users;
import com.jhj.service.bs.DictCouponsService;
import com.jhj.service.bs.OrgStaffBlackService;
import com.jhj.service.bs.OrgStaffDetailDeptService;
import com.jhj.service.bs.OrgStaffDetailPayService;
import com.jhj.service.bs.OrgStaffFinanceService;
import com.jhj.service.bs.OrgsService;
import com.jhj.service.cooperate.CooperateBusinessService;
import com.jhj.service.order.OaOrderService;
import com.jhj.service.order.OrderDispatchPriceService;
import com.jhj.service.order.OrderDispatchsService;
import com.jhj.service.order.OrderPriceExtService;
import com.jhj.service.order.OrderPricesService;
import com.jhj.service.order.OrdersService;
import com.jhj.service.university.PartnerServiceTypeService;
import com.jhj.service.users.UserAddrsService;
import com.jhj.service.users.UserCouponsService;
import com.jhj.service.users.UsersService;
import com.jhj.utils.OrderUtils;
import com.jhj.vo.order.OrderDispatchSearchVo;
import com.jhj.vo.order.OrderSearchVo;
import com.jhj.vo.staff.OrgStaffFinanceSearchVo;
import com.jhj.vo.staff.OrgStaffIncomingVo;
import com.meijia.utils.MathBigDecimalUtil;
import com.meijia.utils.OneCareUtil;
import com.meijia.utils.TimeStampUtil;

/**
 *
 * @author :hulj
 * @Date : 2015年7月6日下午2:44:25
 * @Description:
 *
 */
@Service
public class OrgStaffFinanceServiceImpl implements OrgStaffFinanceService {

	@Autowired
	private OrgStaffFinanceMapper orgStaffFinanceMapper;

	@Autowired
	private OrderPricesService orderPricesService;

	@Autowired
	private OrderPriceExtService orderPriceExtService;

	@Autowired
	private OrgStaffDetailDeptService orgStaffDetailDeptService;

	@Autowired
	private OrgStaffDetailPayService orgStaffDetailPayService;

	@Autowired
	private OrgStaffBlackService orgStaffBlackService;

	@Autowired
	private OrdersService ordersService;

	@Autowired
	private OrderDispatchsService orderDispatchService;

	@Autowired
	private UsersService userService;

	@Autowired
	private UserCouponsService userCouponsService;

	@Autowired
	private DictCouponsService dictCouponsService;

	@Autowired
	private PartnerServiceTypeService partnerServiceTypeService;

	@Autowired
	private OrgsService orgService;

	@Autowired
	private UserAddrsService userAddrService;
	
	@Autowired
	private OrderDispatchPriceService orderDispatchPriceService;
	
	@Autowired
	private CooperateBusinessService cooperateBusinessService;
	
	@Autowired
	private OrderDispatchPriceService orderDisppatchPriceService;
	
	@Autowired
	private OaOrderService oaOrderService;
	

	@Override
	public int deleteByPrimaryKey(Long id) {

		return orgStaffFinanceMapper.deleteByPrimaryKey(id);
	}

	@Override
	public Long insert(OrgStaffFinance record) {

		return orgStaffFinanceMapper.insert(record);
	}

	@Override
	public Long insertSelective(OrgStaffFinance record) {

		return orgStaffFinanceMapper.insertSelective(record);
	}

	@Override
	public OrgStaffFinance selectByPrimaryKey(Long orderId) {

		return orgStaffFinanceMapper.selectByPrimaryKey(orderId);
	}

	@Override
	public int updateByPrimaryKeySelective(OrgStaffFinance record) {

		return orgStaffFinanceMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(OrgStaffFinance record) {

		return orgStaffFinanceMapper.updateByPrimaryKey(record);
	}

	@Override
	public OrgStaffFinance initOrgStaffFinance() {

		OrgStaffFinance record = new OrgStaffFinance();

		record.setId(0L);
		record.setStaffId(0L);
		record.setMobile("");
		record.setTotalIncoming(new BigDecimal(0));
		record.setTotalDept(new BigDecimal(0));
		record.setTotalCash(new BigDecimal(0));
		record.setRestMoney(new BigDecimal(0));
		record.setIsBlack((short) 0);
		record.setAddTime(TimeStampUtil.getNowSecond());
		record.setUpdateTime(TimeStampUtil.getNowSecond());

		return record;
	}

	@Override
	public OrgStaffFinance selectByStaffId(Long userId) {

		return orgStaffFinanceMapper.selectByStaffId(userId);
	}

	@Override
	public PageInfo<OrgStaffFinance> selectByListPage(OrgStaffFinanceSearchVo searchVo, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<OrgStaffFinance> list = orgStaffFinanceMapper.selectByListPage(searchVo);
		PageInfo<OrgStaffFinance> result = new PageInfo<>(list);
		return result;
	}

	@Override
	public List<OrgStaffFinance> selectBySearchVo(OrgStaffFinanceSearchVo searchVo) {
		return orgStaffFinanceMapper.selectBySearchVo(searchVo);
	}

	/**
	 * 完成服务的操作
	 * 1. 记录消费明细
	 * 2. 如果有欠款，记录明细
	 * 3. 如果有加时，则需要记录加时的欠款
	 * 4. 服务人员总收入增加
	 * 5. 服务人员总欠款增加
	 */
	@Override
	public void orderDone(Orders orders, OrderPrices orderPrices, OrgStaffs orgStaffs) {
		Long orderId = orders.getId();
		Long staffId = orgStaffs.getStaffId();

		BigDecimal totalOrderPay = orderPricesService.getTotalOrderPay(orderPrices);
		
		Map<String, String> orderIncomingMap = new HashMap<String, String>();
		
		Long serviceTypeId = orders.getServiceType();
		if (serviceTypeId.equals(28L) || serviceTypeId.equals(68L) || serviceTypeId.equals(73L)) {
			orderIncomingMap = orderPricesService.getTotalOrderIncomingHour(orders, staffId);
		} else {
			orderIncomingMap = orderPricesService.getTotalOrderIncomingDeep(orders, staffId);
		}
		
		String orderIncomingStr = orderIncomingMap.get("totalOrderPay");
		BigDecimal orderIncoming = new BigDecimal(orderIncomingStr);


		// =======================|订单收入组成，并生成备注

		String remarks = orderIncomingMap.get("incomingStr");
		
		// 服务人员财务表
		OrgStaffFinance orgStaffFinance = this.selectByStaffId(staffId);
		if (orgStaffFinance == null) {
			orgStaffFinance = this.initOrgStaffFinance();
			orgStaffFinance.setStaffId(staffId);
		}
		orgStaffFinance.setMobile(orgStaffs.getMobile());

		// 新增服务人员交易明细表 org_staff_detail_pay
		String orderStatusStr = OrderUtils.getOrderStatusName(orders.getOrderType(), orders.getOrderStatus());
		Boolean orderStaffDetailPay = orgStaffDetailPayService.setStaffDetailPay(staffId, orgStaffs.getMobile(), Constants.STAFF_DETAIL_ORDER_TYPE_0, orderId,
				orders.getOrderNo(), totalOrderPay, orderIncoming, orderStatusStr, remarks, 0L);

		if (orderStaffDetailPay == true) {
			// 总收入
			BigDecimal totalIncoming = orgStaffFinance.getTotalIncoming();
			// 最终总收入
			BigDecimal totalIncomingend = totalIncoming.add(orderIncoming);
			orgStaffFinance.setTotalIncoming(totalIncomingend);
			orgStaffFinance.setUpdateTime(TimeStampUtil.getNowSecond());

			if (orgStaffFinance.getId() > 0L) {
				this.updateByPrimaryKeySelective(orgStaffFinance);
			} else {
				this.insert(orgStaffFinance);
			}
		}

		if (orderPrices.getPayType().equals((short) 6)) {
			OrderSearchVo orgStaffDetailPaySearchVo = new OrderSearchVo();
			orgStaffDetailPaySearchVo.setOrderNo(orders.getOrderNo());
			orgStaffDetailPaySearchVo.setStaffId(staffId);
			orgStaffDetailPaySearchVo.setOrderType(Constants.STAFF_DETAIL_DEPT_ORDER_TYPE_0);
			// 判断是否已经存在欠款
			List<OrgStaffDetailDept> orgStaffDetailDepts = orgStaffDetailDeptService.selectBySearchVo(orgStaffDetailPaySearchVo);

			if (orgStaffDetailDepts.isEmpty()) {

				BigDecimal totalOrderDept = orderPricesService.getTotalOrderDept(orders, staffId);

				OrgStaffDetailDept orgStaffDetailDept = orgStaffDetailDeptService.initOrgStaffDetailDept();
				// 新增欠款明细表 org_staff_detail_dept
				orgStaffDetailDept.setStaffId(staffId);
				orgStaffDetailDept.setMobile(orgStaffs.getMobile());
				orgStaffDetailDept.setOrderType(orders.getOrderType());
				orgStaffDetailDept.setOrderId(orderId);
				orgStaffDetailDept.setOrderNo(orders.getOrderNo());
				orgStaffDetailDept.setOrderMoney(totalOrderPay);
				orgStaffDetailDept.setOrderDept(totalOrderDept);
				orgStaffDetailDept.setOrderStatusStr(OrderUtils.getOrderStatusName(orders.getOrderType(), orders.getOrderStatus()));
				orgStaffDetailDept.setRemarks(orders.getRemarks());
				orgStaffDetailDeptService.insert(orgStaffDetailDept);

				// 更新欠款总表
				BigDecimal totalDept = orgStaffFinance.getTotalDept();
				totalDept = totalDept.add(totalOrderDept);
				orgStaffFinance.setTotalDept(totalDept);
				orgStaffFinance.setUpdateTime(TimeStampUtil.getNowSecond());
				this.updateByPrimaryKeySelective(orgStaffFinance);

				orgStaffBlackService.checkStaffBlank(orgStaffFinance);
			}
		} else {
			// 判断是否订单支付方式不是现金支付，但是有订单加时，则这个也需要增加
			OrderSearchVo osearchVo = new OrderSearchVo();
			osearchVo.setOrderId(orderId);
			osearchVo.setOrderExtType((short) 1);
			List<OrderPriceExt> list = orderPriceExtService.selectBySearchVo(osearchVo);
			if (!list.isEmpty()) {
				for (OrderPriceExt ope : list) {
					this.orderOverWork(orders, ope, orgStaffs);
				}
			}
		}
		
		//记录详细的服务人员消费表
		OrderDispatchSearchVo searchVo = new OrderDispatchSearchVo();
		searchVo.setOrderId(orderId);
		searchVo.setDispatchStatus((short) 1);
		searchVo.setStaffId(staffId);
		List<OrderDispatchs> orderDispatchs = orderDispatchService.selectBySearchVo(searchVo);
		if (!orderDispatchs.isEmpty()) {
			OrderDispatchs orderDispatch = orderDispatchs.get(0);
			orderDispatchPriceService.doOrderDispatchPrice(orders, orderDispatch);
		}
	}

	// 订单补时，服务人员的财务信息操作，仅做判断更新欠款，是否需要加入黑名单
	@Override
	public void orderOverWork(Orders orders, OrderPriceExt orderPriceExt, OrgStaffs orgStaffs) {
		Long orderId = orders.getId();
		Long staffId = orgStaffs.getStaffId();

		int staffNum = orders.getStaffNums();
//		BigDecimal orderPay = orderPriceExtService.getTotalOrderExtPay(orders, orderExtType);
		BigDecimal orderPay = orderPriceExt.getOrderPay();
		orderPay = MathBigDecimalUtil.div(orderPay, new BigDecimal(staffNum));
		// 服务人员财务表
		OrgStaffFinance orgStaffFinance = this.selectByStaffId(staffId);
		if (orgStaffFinance == null) {
			orgStaffFinance = this.initOrgStaffFinance();
			orgStaffFinance.setStaffId(staffId);
		}
		orgStaffFinance.setMobile(orgStaffs.getMobile());

		if (orderPriceExt.getPayType().equals((short) 6)) {

			OrderSearchVo paySearchVo = new OrderSearchVo();
			paySearchVo.setStaffId(staffId);
			paySearchVo.setOrderNo(orderPriceExt.getOrderNoExt());
			paySearchVo.setOrderType(Constants.STAFF_DETAIL_DEPT_ORDER_TYPE_0);
			// 判断是否已经存在欠款
			List<OrgStaffDetailDept> orgStaffDetailDepts = orgStaffDetailDeptService.selectBySearchVo(paySearchVo);

			if (orgStaffDetailDepts.isEmpty()) {
				OrgStaffDetailDept orgStaffDetailDept = orgStaffDetailDeptService.initOrgStaffDetailDept();
				// 新增欠款明细表 org_staff_detail_dept
				orgStaffDetailDept.setStaffId(staffId);
				orgStaffDetailDept.setMobile(orgStaffs.getMobile());
				orgStaffDetailDept.setOrderType(Constants.STAFF_DETAIL_DEPT_ORDER_TYPE_0);
				orgStaffDetailDept.setOrderId(orderId);
				orgStaffDetailDept.setOrderNo(orderPriceExt.getOrderNoExt());
				orgStaffDetailDept.setOrderMoney(orderPay);
				orgStaffDetailDept.setOrderDept(orderPay);
				orgStaffDetailDept.setOrderStatusStr("加时服务");
				orgStaffDetailDept.setRemarks(orders.getRemarks());
				orgStaffDetailDeptService.insert(orgStaffDetailDept);

				// 更新欠款总表
				BigDecimal totalDept = orgStaffFinance.getTotalDept();
				totalDept = totalDept.add(orderPay);
				orgStaffFinance.setTotalDept(totalDept);
				orgStaffFinance.setUpdateTime(TimeStampUtil.getNowSecond());
				this.updateByPrimaryKeySelective(orgStaffFinance);

				orgStaffBlackService.checkStaffBlank(orgStaffFinance);
			}
		}
	}

	// 服务人员每单的详细情况VO
	@Override
	public OrgStaffIncomingVo getStaffInComingDetail(OrgStaffs orgStaff, Orders order, OrderDispatchs orderDispatch) {
		OrgStaffIncomingVo vo = new OrgStaffIncomingVo();

		Long orderId = order.getId();
		Long staffId = orgStaff.getStaffId();

		Long parentId = orgStaff.getParentOrgId();
		Orgs parentOrg = orgService.selectByPrimaryKey(parentId);
		vo.setParentId(parentId);
		vo.setParentOrgName(parentOrg.getOrgName());

		Long orgId = orgStaff.getOrgId();
		Orgs org = orgService.selectByPrimaryKey(orgId);
		vo.setOrgId(orgId);
		vo.setOrgName(org.getOrgName());

		vo.setStaffId(orgStaff.getStaffId());
		vo.setStaffName(orgStaff.getName());
		vo.setStaffMobile(orgStaff.getMobile());

		vo.setOrderId(order.getId());
		vo.setOrderNo(order.getOrderNo());
		vo.setServiceHour(order.getServiceHour());
		vo.setUserMobile(order.getMobile());
		vo.setStaffNum(order.getStaffNums());

		String addTimeStr = TimeStampUtil.timeStampToDateStr(order.getAddTime() * 1000, "yyyy-MM-dd HH:mm");
		vo.setAddTimeStr(addTimeStr);
		
		String serviceDateStr = TimeStampUtil.timeStampToDateStr(order.getServiceDate() * 1000, "yyyy-MM-dd HH:mm");
		vo.setServiceDateStr(serviceDateStr);
		String orderDonetimeStr = TimeStampUtil.timeStampToDateStr(order.getOrderDoneTime() * 1000, "yyyy-MM-dd HH:mm");
		vo.setOrderDonetimeStr(orderDonetimeStr);

		Long serviceTypeId = order.getServiceType();
		PartnerServiceType serviceType = partnerServiceTypeService.selectByPrimaryKey(serviceTypeId);
		String orderTypeName = "";
		if (serviceType != null)
			orderTypeName = serviceType.getName();
		vo.setOrderTypeName(orderTypeName);

		OrderSearchVo searchVo = new OrderSearchVo();
		searchVo.setOrderId(orderId);
		searchVo.setOrderNo(order.getOrderNo());
		List<OrderDispatchPrices> orderDispatchPricesList = orderDisppatchPriceService.selectBySearchVo(searchVo);

		OrderDispatchPrices orderDispatchPrices = null;
		String isVipStr = "否";
		if(orderDispatchPricesList!=null && orderDispatchPricesList.size()>0){
			orderDispatchPrices = orderDispatchPricesList.get(0);
			vo.setAddr(orderDispatchPrices.getAddr());
			
			Short isVip = orderDispatchPrices.getIsVip();
			if(isVip.equals("1")){
				isVipStr = "是";
			}
			vo.setIsVip(isVip);
			vo.setIsVipStr(isVipStr);
			
			vo.setPayTypeName(OneCareUtil.getPayTypeName(orderDispatchPrices.getPayType()));
			
			vo.setOrderMoney(orderDispatchPrices.getOrderMoney());
			vo.setOrderPay(orderDispatchPrices.getOrderPay());
			
			vo.setTotalOrderMoney(orderDispatchPrices.getTotalOrderMoney());
			vo.setTotalOrderPay(orderDispatchPrices.getTotalOrderPay());
			vo.setTotalOrderDept(orderDispatchPrices.getTotalOrderDept());
			
			vo.setIncomingPercent(orderDispatchPrices.getIncomingPercent());
			
			//补差价
			vo.setOrderPayExtDiff(orderDispatchPrices.getOrderPayExtDiff());
			vo.setOrderPayExtDiffIncoming(orderDispatchPrices.getOrderPayExtDiffIncoming());
			
			//加时
			vo.setOrderPayExtOverWork(orderDispatchPrices.getOrderPayExtOverwork());
			vo.setOrderPayExtOverWorkIncoming(orderDispatchPrices.getOrderPayExtOverworkIncoming());
			
		}
		
		//订单来源
		Short orderFrom = order.getOrderFrom();
		Long orderOpFrom = order.getOrderOpFrom();
		CooperativeBusiness cooperativeBusiness = null;
		if (orderOpFrom != null) {
			cooperativeBusiness = cooperateBusinessService.selectByPrimaryKey(orderOpFrom);
		}
		
		String orderFromStr = OrderUtils.getOrderFromName(orderFrom, orderOpFrom, cooperativeBusiness);
		vo.setOrderFromStr(orderFromStr);
		
		
		//=========================订单金额相关
		
		Map<String, String> orderIncomingMap = null;
		
		if (serviceTypeId.equals(28L) || serviceTypeId.equals(68L) || serviceTypeId.equals(73L)) {
			orderIncomingMap = orderPricesService.getTotalOrderIncomingHour(order, staffId);
		} else {
			orderIncomingMap = orderPricesService.getTotalOrderIncomingDeep(order, staffId);
		}
		
		String remarks = orderIncomingMap.get("incomingStr");
		
		// 1.订单收入金额
		BigDecimal orderPayIncoming = orderDispatchPrices.getOrderPayIncoming();
		BigDecimal totalOrderIncoming = orderDispatchPrices.getTotalOrderIncoming();
		remarks = "订单总收入:" + MathBigDecimalUtil.round2(orderDispatchPrices.getTotalOrderIncoming());
		vo.setOrderIncoming(orderPayIncoming);
		vo.setTotalOrderIncoming(totalOrderIncoming);
		
		BigDecimal orderPayCoupon = orderDispatchPrices.getOrderPayCoupon();
		BigDecimal orderPayCouponIncoming = orderDispatchPrices.getOrderPayCouponIncoming();
		remarks += " + 订单优惠劵补贴:" + MathBigDecimalUtil.round2(orderPayCoupon);
		vo.setOrderPayCoupon(orderPayCoupon);
		vo.setOrderPayCouponIncoming(orderPayCouponIncoming);
		
		vo.setRemarks(remarks);
		
		//团购码和是否验码
		vo.setGroupCode(oaOrderService.getOrderGroupCode(order.getId()));
		
		vo.setValidateCodeName("否");
		if(order.getValidateCode().equals((short)1)){
			vo.setValidateCodeName("是");
		}
		
		return vo;
	}

	// 统计服务人员欠款
	public Map<String, Object> totalMoney(OrgStaffFinanceSearchVo searchVo) {
		return orgStaffFinanceMapper.totalMoney(searchVo);
	}

	/**
	 * 计算服务人员财物明细
	 * 
	 */
	@Override
	public OrgStaffIncomingVo calcStaffInComingDetail(OrgStaffs orgStaff,
			Orders order, OrderDispatchs orderDispatch) {
		
		OrgStaffIncomingVo vo = new OrgStaffIncomingVo();

		Long orderId = order.getId();
		Long staffId = orgStaff.getStaffId();

		Long parentId = orgStaff.getParentOrgId();
		Orgs parentOrg = orgService.selectByPrimaryKey(parentId);
		vo.setParentId(parentId);
		vo.setParentOrgName(parentOrg.getOrgName());

		Long orgId = orgStaff.getOrgId();
		Orgs org = orgService.selectByPrimaryKey(orgId);
		vo.setOrgId(orgId);
		vo.setOrgName(org.getOrgName());

		vo.setStaffId(orgStaff.getStaffId());
		vo.setStaffName(orgStaff.getName());
		vo.setStaffMobile(orgStaff.getMobile());

		vo.setOrderId(order.getId());
		vo.setOrderNo(order.getOrderNo());

		Long addTime = order.getAddTime();
		String addTimeStr = TimeStampUtil.timeStampToDateStr(addTime * 1000, "yyyy-MM-dd HH:mm");
		vo.setAddTimeStr(addTimeStr);

		Long serviceTypeId = order.getServiceType();
		PartnerServiceType serviceType = partnerServiceTypeService.selectByPrimaryKey(serviceTypeId);
		String orderTypeName = "";
		if (serviceType != null)
			orderTypeName = serviceType.getName();
		vo.setOrderTypeName(orderTypeName);

		String serviceDateStr = TimeStampUtil.timeStampToDateStr(order.getServiceDate() * 1000, "yyyy-MM-dd HH:mm");
		vo.setServiceDateStr(serviceDateStr);

		vo.setServiceHour(order.getServiceHour());

		Long addrId = order.getAddrId();
		UserAddrs userAddrs = userAddrService.selectByPrimaryKey(addrId);
		String addr = "";
		if (userAddrs != null)
			addr = userAddrs.getName() + "" + userAddrs.getAddr();
		vo.setAddr(addr);

		vo.setUserMobile(order.getMobile());

		Long userId = order.getUserId();
		Users u = userService.selectByPrimaryKey(userId);
		
		vo.setIsVip((short) 0);
		String isVipStr = "否";
		if (u != null) {
			if (u.getIsVip() == 1) {
				vo.setIsVip((short) 1);
				isVipStr = "是";
			}
		}
		vo.setIsVipStr(isVipStr);

		OrderPrices orderPrices = orderPricesService.selectByOrderId(orderId);
		String payTypeName = OneCareUtil.getPayTypeName(orderPrices.getPayType());
		vo.setPayTypeName(payTypeName);
		
		//订单来源
		
		Short orderFrom = order.getOrderFrom();
		Long orderOpFrom = order.getOrderOpFrom();
		CooperativeBusiness cooperativeBusiness = null;
		if (orderOpFrom != null) {
			cooperativeBusiness = cooperateBusinessService.selectByPrimaryKey(orderOpFrom);
		}
		
		String orderFromStr = OrderUtils.getOrderFromName(orderFrom, orderOpFrom, cooperativeBusiness);
		vo.setOrderFromStr(orderFromStr);
		
		//=========================订单金额相关
		
		Map<String, String> orderIncomingMap = new HashMap<String, String>();
		
	
		if (serviceTypeId.equals(28L) || serviceTypeId.equals(68L) || serviceTypeId.equals(73L)) {
			orderIncomingMap = orderPricesService.getTotalOrderIncomingHour(order, staffId);
		} else {
			orderIncomingMap = orderPricesService.getTotalOrderIncomingDeep(order, staffId);
		}
		
		String orderIncomingStr = orderIncomingMap.get("totalOrderPay");
		BigDecimal totalOrderIncoming = new BigDecimal(orderIncomingStr);
		
		String remarks = orderIncomingMap.get("incomingStr");
		
		int staffNum = order.getStaffNums();
		vo.setStaffNum(staffNum);
		BigDecimal totalOrderMoney = orderPricesService.getTotalOrderMoney(orderPrices);
		totalOrderMoney = MathBigDecimalUtil.div(totalOrderMoney, new BigDecimal(staffNum));
		
		BigDecimal totalOrderPay = orderPricesService.getTotalOrderPay(orderPrices);
		totalOrderPay = MathBigDecimalUtil.div(totalOrderPay, new BigDecimal(staffNum));
		
		BigDecimal incomingPercent = orderPricesService.getOrderPercent(order, staffId);
		
		//10.20 -  11.21 号完成服务的订单， 服务人员收入 = (订单支付金额/派工人数) *  0.7
		if (order.getAddTime() >= 1476892800L && order.getAddTime() < 1479657600) {
			incomingPercent = new BigDecimal(0.7);
		}
		
		
		// 1.订单支付金额
		BigDecimal orderIncoming = orderPrices.getOrderPay();
		orderIncoming = MathBigDecimalUtil.div(orderIncoming, new BigDecimal(staffNum));
		if (order.getAddTime() >= 1476892800L && order.getAddTime() < 1479657600) {
			orderIncoming = orderIncoming.multiply(incomingPercent);
		}else{
			if(serviceTypeId.equals(28L)){
				if(u.getIsVip()==0){
					orderIncoming = Constants.JINPAIBAOJIE_NOT_VIP_PERCENTAGE;
				}
				if(u.getIsVip()==1){
					orderIncoming = Constants.JINPAIBAOJIE_VIP_PERCENTAGE;
				}
			}
			
			if(serviceTypeId.equals(68L)){
				if(u.getIsVip()==0){
					orderIncoming = Constants.JICHUBAOJIE_NOT_VIP_PERCENTAGE;
				}
				if(u.getIsVip()==1){
					orderIncoming = Constants.JICHUBAOJIE_VIP_PERCENTAGE;
				}
			}
		}
		String orderPayStr = MathBigDecimalUtil.round2(orderIncoming);
		remarks = "订单收入:" + orderPayStr;
		// 2.订单优惠劵金额
		BigDecimal orderPayCoupon = new BigDecimal(0);
		BigDecimal orderPayCouponIncoming = new BigDecimal(0);
		Long userCouponId = orderPrices.getCouponId();
		vo.setCouponName("");
		
		//11.21- 12.16号的完成服务的订单， 服务人员收入 =  (订单支付金额/派工人数) *  服务人员回扣比例（是否为会员，是否为用户指派人员）
		//12.16号之后的，按照全部的公式比例来计算收入
		if (userCouponId > 0L && order.getAddTime() >= 1481904000L && order.getAddTime() <= 1489557600) {
			UserCoupons userCoupon = userCouponsService.selectByPrimaryKey(userCouponId);
			if (userCoupon != null) {
				Long couponId = userCoupon.getCouponId();
				DictCoupons dictCoupon = dictCouponsService.selectByPrimaryKey(couponId);
				orderPayCoupon = dictCoupon.getValue();
				orderPayCouponIncoming = MathBigDecimalUtil.div(orderPayCoupon, new BigDecimal(staffNum));
				orderPayCouponIncoming = orderPayCouponIncoming.multiply(new BigDecimal(0.5));
				String orderPayCouponStr = MathBigDecimalUtil.round2(orderPayCoupon);
				vo.setCouponName(dictCoupon.getDescription());
				remarks += " + 订单优惠劵补贴:" + orderPayCouponStr;
			}
		}

		// 3.订单补差价金额
		BigDecimal orderPayExtDiff = orderPriceExtService.getTotalOrderExtPay(order, (short) 0);
		BigDecimal orderPayExtDiffIncoming = MathBigDecimalUtil.div(orderPayExtDiff, new BigDecimal(staffNum));
		orderPayExtDiffIncoming = orderPayExtDiffIncoming.multiply(incomingPercent);
//		if (orderPayExtDiffIncoming.compareTo(BigDecimal.ZERO) == 1) {
//			String orderPayExtDiffStr = MathBigDecimalUtil.round2(orderPayExtDiffIncoming);
//			remarks += " + 订单补差价收入:" + orderPayExtDiffStr;
//		}

		// 4.订单加时金额
		BigDecimal orderPayExtOverWork = orderPriceExtService.getTotalOrderExtPay(order, (short) 1);
		BigDecimal orderPayExtOverWorkIncoming = MathBigDecimalUtil.div(orderPayExtOverWork, new BigDecimal(staffNum));
		orderPayExtOverWorkIncoming = orderPayExtOverWorkIncoming.multiply(incomingPercent);
//		if (orderPayExtOverWorkIncoming.compareTo(BigDecimal.ZERO) == 1) {
//			String orderPayExtOverWorkStr = MathBigDecimalUtil.round2(orderPayExtOverWorkIncoming);
//			remarks += " + 订单加时收入:" + orderPayExtOverWorkStr;
//		}
		
//		BigDecimal totalOrderIncoming = new BigDecimal(0);
//		totalOrderIncoming = totalOrderIncoming.add(orderIncoming);
//		totalOrderIncoming = totalOrderIncoming.add(orderPayExtDiffIncoming);
//		totalOrderIncoming = totalOrderIncoming.add(orderPayExtOverWorkIncoming);
//		totalOrderIncoming = totalOrderIncoming.add(orderPayCouponIncoming);
		totalOrderIncoming = MathBigDecimalUtil.round(totalOrderIncoming, 2);
		
		vo.setTotalOrderMoney(totalOrderMoney);
		vo.setTotalOrderPay(totalOrderPay);
		
		BigDecimal orderMoney = orderPrices.getOrderMoney();
//		orderMoney = MathBigDecimalUtil.div(orderMoney, new BigDecimal(staffNum));
		
		BigDecimal orderPay = orderPrices.getOrderPay();
		orderPay = MathBigDecimalUtil.div(orderPay, new BigDecimal(staffNum));
		
		vo.setOrderMoney(orderMoney);
		vo.setOrderPay(orderPay);
		
		orderPayExtDiff =  MathBigDecimalUtil.div(orderPayExtDiff, new BigDecimal(staffNum));
		vo.setOrderPayExtDiff(orderPayExtDiff);
		
		orderPayExtOverWork = MathBigDecimalUtil.div(orderPayExtOverWork, new BigDecimal(staffNum));
		vo.setOrderPayExtOverWork(orderPayExtOverWork);
		
		vo.setOrderIncoming(orderIncoming);
		vo.setOrderPayCoupon(orderPayCoupon);
		vo.setOrderPayCouponIncoming(orderPayCouponIncoming);
		vo.setOrderPayExtDiffIncoming(orderPayExtDiffIncoming);
		vo.setOrderPayExtOverWorkIncoming(orderPayExtOverWorkIncoming);
		
		vo.setTotalOrderIncoming(totalOrderIncoming);
		
		BigDecimal totalOrderDept = orderPricesService.getTotalOrderDept(order, staffId);
		vo.setTotalOrderDept(totalOrderDept);
		vo.setRemarks(remarks);
		vo.setIncomingPercent(incomingPercent);
		return vo;
		
	}

}
