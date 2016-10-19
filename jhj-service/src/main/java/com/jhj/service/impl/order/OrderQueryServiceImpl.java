package com.jhj.service.impl.order;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jhj.common.Constants;
import com.jhj.po.dao.bs.DictCouponsMapper;
import com.jhj.po.dao.order.OrderDispatchsMapper;
import com.jhj.po.dao.order.OrdersMapper;
import com.jhj.po.dao.user.UserCouponsMapper;
import com.jhj.po.model.bs.DictCoupons;
import com.jhj.po.model.bs.OrgStaffs;
import com.jhj.po.model.bs.Orgs;
import com.jhj.po.model.order.OrderDispatchs;
import com.jhj.po.model.order.OrderPrices;
import com.jhj.po.model.order.Orders;
import com.jhj.po.model.orderReview.JhjSetting;
import com.jhj.po.model.university.PartnerServiceType;
import com.jhj.po.model.user.UserAddrs;
import com.jhj.po.model.user.UserCoupons;
import com.jhj.po.model.user.Users;
import com.jhj.service.bs.OrgStaffsService;
import com.jhj.service.bs.OrgsService;
import com.jhj.service.dict.DictService;
import com.jhj.service.order.OrderDispatchsService;
import com.jhj.service.order.OrderPricesService;
import com.jhj.service.order.OrderQueryService;
import com.jhj.service.orderReview.SettingService;
import com.jhj.service.university.PartnerServiceTypeService;
import com.jhj.service.users.UserAddrsService;
import com.jhj.service.users.UsersService;
import com.jhj.utils.OrderUtils;
import com.jhj.vo.order.OrderDetailVo;
import com.jhj.vo.order.OrderDispatchSearchVo;
import com.jhj.vo.order.OrderListVo;
import com.jhj.vo.order.OrderSearchVo;
import com.jhj.vo.order.OrderViewVo;
import com.jhj.vo.order.UserListVo;
import com.jhj.vo.user.UserSearchVo;
import com.meijia.utils.BeanUtilsExp;
import com.meijia.utils.MathBigDecimalUtil;
import com.meijia.utils.OneCareUtil;
import com.meijia.utils.StringUtil;
import com.meijia.utils.TimeStampUtil;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

	@Autowired
	private OrdersMapper ordersMapper;

	@Autowired
	private OrderPricesService orderPricesService;

	@Autowired
	private UsersService usersService;

	@Autowired
	private OrderDispatchsService orderDispatchsService;

	@Autowired
	private UserAddrsService userAddrsService;

	@Autowired
	DictService dictService;

	@Autowired
	DictCouponsMapper dictCouponsMapper;

	@Autowired
	private UserCouponsMapper userCouponsMapper;

	@Autowired
	private OrderDispatchsMapper orderDispatchsMapper;

	@Autowired
	private SettingService settingService;

	@Autowired
	private OrgStaffsService orgStaffsService;

	@Autowired
	private OrgsService orgsService;

	@Autowired
	PartnerServiceTypeService partnerServiceTypeService;

	@Override
	public List<Orders> selectBySearchVo(OrderSearchVo searchVo) {
		return ordersMapper.selectBySearchVo(searchVo);
	}

	@Override
	public PageInfo selectByListPage(OrderSearchVo searchVo, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<Orders> list = ordersMapper.selectByListPage(searchVo);
		PageInfo result = new PageInfo(list);
		return result;
	}

	/*
	 * 进行orderViewVo 结合了 orders , order_prices, 两张表的元素
	 */
	@Override
	public OrderViewVo getOrderView(Orders order) {

		// 加载更多订单的信息
		OrderViewVo vo = new OrderViewVo();
		BeanUtils.copyProperties(order, vo);

		// 订单价格信息
		vo.setOrderMoney(new BigDecimal(0));
		vo.setOrderPay(new BigDecimal(0));
		vo.setPayType((short) 0);
		OrderPrices orderPrice = orderPricesService.selectByOrderId(vo.getId());
		if (orderPrice != null) {
			
			BigDecimal orderMoney = orderPricesService.getOrderMoney(orderPrice);
			BigDecimal orderPay = orderPricesService.getOrderPay(orderPrice);
			vo.setPayType(orderPrice.getPayType());
			vo.setOrderMoney(orderMoney);
			vo.setOrderPay(orderPay);
		}

		// 城市名称
		vo.setCityName("");
		if (vo.getCityId() > 0L) {
			String cityName = dictService.getCityName(vo.getCityId());
			vo.setCityName(cityName);
		}

		// 服务类型名称
		vo.setServiceTypeName("");
		vo.setIsMulti((short) 0);
		if (vo.getServiceType() > 0L) {
			PartnerServiceType serviceType = partnerServiceTypeService.selectByPrimaryKey(vo.getServiceType());
			
			if (serviceType != null) {
				vo.setServiceTypeName(serviceType.getName());
				vo.setIsMulti(serviceType.getIsMulti());
			}
		}

		// 用户称呼
		vo.setName("");
		if (vo.getUserId() > 0L) {
			Users user = usersService.selectByPrimaryKey(vo.getUserId());
			vo.setName(user.getName());
		}

		// 用户地址
		vo.setServiceAddr("");
		if (vo.getAddrId() > 0L) {
			UserAddrs userAddr = userAddrsService.selectByPrimaryKey(vo.getAddrId());
			vo.setServiceAddr(userAddr.getName() + userAddr.getAddr());
		}
		// 订单类型
		vo.setOrderTypeName("");
		if (vo.getUserId() > 0L) {

			if (vo.getOrderType() == 0) {
				vo.setOrderTypeName("钟点工");
			} else if (vo.getOrderType() == 1) {
				vo.setOrderTypeName("深度保洁");
			} else if (vo.getOrderType() == 2) {
				vo.setOrderTypeName("助理预约单");
			} else if (vo.getOrderType() == 3) {
				vo.setOrderTypeName("配送服务单");
			} else if (vo.getOrderType() == 4) {
				vo.setOrderTypeName("充值订单");
			} else if (vo.getOrderType() == 5) {
				vo.setOrderTypeName("提醒订单");
			} else {
				vo.setOrderTypeName("缴费单");
			}
		}
		// 服务日期转换
		Long serviceDate = order.getServiceDate();
		vo.setServiceDateStr(TimeStampUtil.timeStampToDateStr(serviceDate * 1000));
		// 优惠券描述
		UserCoupons userCoupons = userCouponsMapper.selectByOrderNo(order.getOrderNo());
		if (userCoupons != null) {
			DictCoupons dictCoupons = dictCouponsMapper.selectByPrimaryKey(userCoupons.getCouponId());
			if (dictCoupons != null) {
				vo.setIntroduction(dictCoupons.getIntroduction());
			}
		}
		// 订单状态
		String orderStatusName = OneCareUtil.getJhjOrderStausNameFromOrderType(order.getOrderType(), order.getOrderStatus());
		// String orderStatusName = getOrderStatusName(order.getOrderStatus());
		vo.setOrderStatusName(orderStatusName);

		return vo;
	}

	@Override
	public String getOrderStatusName(Short status) {

		String statusName = "";
		if (status.equals(Constants.ORDER_STATUS_0)) {
			statusName = "已取消";
		}

		if (status.equals(Constants.ORDER_STATUS_1)) {
			statusName = "待确认";
		}

		if (status.equals(Constants.ORDER_STATUS_2)) {
			statusName = "已确认";
		}

		if (status.equals(Constants.ORDER_STATUS_3)) {
			statusName = "待支付";
		}

		if (status.equals(Constants.ORDER_STATUS_4)) {
			statusName = "已支付";
		}

		if (status.equals(Constants.ORDER_STATUS_5)) {
			statusName = "服务中";
		}

		if (status.equals(Constants.ORDER_STATUS_6)) {
			statusName = "待评价";
		}

		if (status.equals(Constants.ORDER_STATUS_7)) {
			statusName = "已评价";
		}

		if (status.equals(Constants.ORDER_STATUS_9)) {
			statusName = "已关闭";
		}
		return statusName;
	}

	@Override
	public List<OrderViewVo> getOrderViewList(List<Orders> list) {

		// 加载更多订单的信息
		List<Long> userIds = new ArrayList<Long>();
		List<Long> addrIds = new ArrayList<Long>();
		List<Long> orderIds = new ArrayList<Long>();
		List<String> orderNos = new ArrayList<String>();
		Orders item = null;
		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			if (item.getAddrId() > 0L) {
				addrIds.add(item.getAddrId());
			}
			userIds.add(item.getUserId());
			orderIds.add(item.getId());
			orderNos.add(item.getOrderNo());
		}

		List<OrderPrices> orderPricesList = orderPricesService.selectByOrderIds(orderIds);

		UserSearchVo searchVo = new UserSearchVo();
		searchVo.setUserIds(userIds);
		List<Users> userList = usersService.selectBySearchVo(searchVo);

		List<UserAddrs> addrList = new ArrayList<UserAddrs>();
		if (addrIds.size() > 0) {
			addrList = userAddrsService.selectByIds(addrIds);
		}
		// 进行orderViewVo 结合了 orders , order_prices, order_dispatchs 三张表的元素
		List<OrderViewVo> result = new ArrayList<OrderViewVo>();
		Long orderId = 0L;
		Long addrId = 0L;
		Long userId = 0L;

		for (int i = 0; i < list.size(); i++) {
			item = list.get(i);
			orderId = item.getId();
			addrId = item.getAddrId();
			userId = item.getUserId();

			OrderViewVo vo = new OrderViewVo();
			BeanUtils.copyProperties(item, vo);

			// 查找订单金额信息.
			OrderPrices orderPrice = null;
			for (int k = 0; k < orderPricesList.size(); k++) {
				orderPrice = orderPricesList.get(k);
				if (orderPrice.getOrderId().equals(orderId)) {
					vo.setOrderMoney(orderPrice.getOrderMoney());
					vo.setOrderPay(orderPrice.getOrderPay());
					vo.setPayType(orderPrice.getPayType());
					break;
				}
			}

			// 城市名称
			vo.setCityName("");
			if (vo.getCityId() > 0L) {
				String cityName = dictService.getCityName(vo.getCityId());
				vo.setCityName(cityName);
			}

			// 服务类型名称
			vo.setServiceTypeName("");
			if (vo.getServiceType() > 0L) {
				String serviceTypeName = dictService.getServiceTypeName(vo.getServiceType());
				vo.setServiceTypeName(serviceTypeName);
			}

			// 用户姓名
			String name = "";
			Users u = null;
			for (int j = 0; j < userList.size(); j++) {
				u = userList.get(j);
				if (u.getId().equals(userId)) {
					name = u.getName();
					break;
				}
			}
			vo.setName("");

			// 用户地址
			String addrName = "";
			UserAddrs addr = null;
			for (int n = 0; n < addrList.size(); n++) {
				addr = addrList.get(n);
				if (addr.getId().equals(addrId)) {
					addrName = addr.getName() + addr.getAddr();
					break;
				}
			}

			vo.setServiceAddr(addrName);

			result.add(vo);
		}

		return result;
	}

	@Override
	public UserListVo getUserList(Orders order) {
		// 加载更多订单的信息
		UserListVo vo = new UserListVo();
		BeanUtils.copyProperties(order, vo);

		// 订单价格信息
		OrderPrices orderPrice = orderPricesService.selectByOrderId(vo.getId());
		if (orderPrice != null) {
			vo.setPayType(orderPrice.getPayType());
			vo.setOrderMoney(orderPrice.getOrderMoney());
			vo.setOrderPay(orderPrice.getOrderPay());
		}

		// 城市名称
		vo.setCityName("");
		if (vo.getCityId() > 0L) {
			String cityName = dictService.getCityName(vo.getCityId());
			vo.setCityName(cityName);
		}

		// 服务类型名称
		vo.setServiceTypeName("");
		if (vo.getServiceType() > 0L) {
			String serviceTypeName = dictService.getServiceTypeName(vo.getServiceType());
			vo.setServiceTypeName(serviceTypeName);
		}

		// 用户称呼
		vo.setName("");
		if (vo.getUserId() > 0L) {
			Users user = usersService.selectByPrimaryKey(vo.getUserId());
			vo.setName(user.getName());
		}

		// 用户地址
		vo.setServiceAddr("");
		if (vo.getAddrId() > 0L) {
			UserAddrs userAddr = userAddrsService.selectByPrimaryKey(vo.getAddrId());
			vo.setServiceAddr(userAddr.getName() + userAddr.getAddr());
		}
		// 订单类型
		vo.setOrderTypeName("");
		if (vo.getUserId() > 0L) {

			if (vo.getOrderType() == 0) {
				vo.setOrderTypeName("钟点工预约单");
			} else if (vo.getOrderType() == 1) {
				vo.setOrderTypeName("深度保洁预约单");
			} else {
				vo.setOrderTypeName("助理预约单");
			}
		}
		// 订单状态
		String orderStatusName = getOrderStatusName(order.getOrderStatus());
		vo.setOrderStatusName(orderStatusName);

		// 服务次数
		vo.setServiceTimes("0");
		// 1.获得用户列表
		OrderSearchVo searchVo = new OrderSearchVo();
		searchVo.setAmId(order.getAmId());
		List<Orders> list = ordersMapper.selectBySearchVo(searchVo);

		// 2.获得用户对应的ids
		List<Long> userIds = new ArrayList<Long>();
		for (Orders item : list) {
			userIds.add(item.getUserId());
		}
		// 3.获得ids的数量
		List<HashMap> counts = new ArrayList<HashMap>();
		if (!userIds.isEmpty()) {
			counts = ordersMapper.totalByUserIds(userIds);
		}

		for (HashMap serviceCounts : counts) {

			Long userId = Long.valueOf(serviceCounts.get("user_id").toString());
			if (userId.equals(vo.getUserId())) {
				vo.setServiceTimes(serviceCounts.get("total").toString());
			}
		}

		return vo;
	}

	@Override
	public OrderListVo getOrderListVo(Orders item, Long staffId) {

		OrderListVo vo = new OrderListVo();

		OrderDispatchSearchVo searchVo = new OrderDispatchSearchVo();
		searchVo.setOrderNo(item.getOrderNo());
		searchVo.setDispatchStatus((short) 1);
		
		if (staffId > 0L) searchVo.setStaffId(staffId);
		
		List<OrderDispatchs> orderDispatchs = orderDispatchsService.selectBySearchVo(searchVo);

		OrderDispatchs orderDispatch = null;
		if (!orderDispatchs.isEmpty()) {
			orderDispatch = orderDispatchs.get(0);
		}

		if (orderDispatch == null)
			return vo;

		OrderPrices orderPrices = orderPricesService.selectByOrderId(item.getId());
		Users users = usersService.selectByPrimaryKey(item.getUserId());
		OrgStaffs orgStaffs = orgStaffsService.selectByPrimaryKey(staffId);
		BeanUtilsExp.copyPropertiesIgnoreNull(item, vo);
		
		vo.setServiceTypeId(item.getServiceType());
		vo.setOrderId(item.getId());
		vo.setStaffId(staffId);
		vo.setMobile(users.getMobile());
		vo.setServiceHour(String.valueOf(item.getServiceHour()));
		// 服务日期
		Long addTime = item.getServiceDate() * 1000;
		vo.setServiceDate(TimeStampUtil.timeStampToDateStr(addTime, "MM-dd HH:mm"));

		// 如果为助理订单，则取addTime
		if (vo.getOrderType().equals((short) 2)) {
			vo.setServiceDate(TimeStampUtil.timeStampToDateStr(item.getAddTime() * 1000, "MM-dd HH:mm"));
		}

		// 服务类型名称
		vo.setServiceTypeName("");
		if (item.getServiceType() > 0L) {
			PartnerServiceType serviceType = partnerServiceTypeService.selectByPrimaryKey(item.getServiceType());
			vo.setServiceTypeName(serviceType.getName());
			vo.setServiceContent(serviceType.getName());
		}
		vo.setOrderTypeName("");
		// 订单类型
		if (item.getServiceType() > 0L) {
			PartnerServiceType serviceType = partnerServiceTypeService.selectByPrimaryKey(item.getServiceType());
			vo.setOrderTypeName(serviceType.getName());
		}

		// 订单支付名称

		vo.setPayTypeName("");
		if (orderPrices != null) {
			vo.setPayType(orderPrices.getPayType());
			vo.setPayTypeName(OrderUtils.getPayTypeName(item.getOrderStatus(), orderPrices.getPayType()));
		}

		Short level = orgStaffs.getLevel();
		String settingLevel = "-level-" + level.toString();

		// 订单价格信息
		OrderPrices orderPrice = orderPricesService.selectByOrderId(item.getId());
		vo.setOrderIncoming(new BigDecimal(0));
		vo.setOrderMoney(new BigDecimal(0));
		if (orderPrice != null) {
			BigDecimal orderMoney = orderPricesService.getOrderMoney(orderPrice);
			vo.setOrderMoney(orderMoney);
			// 总金额C * 85% = 结果.
			if (vo.getOrderType() == 0) {
				// 基础服务收入比例 hour-ratio
				String settingType = "hour-ratio" + settingLevel;
				JhjSetting jhjSetting = settingService.selectBySettingType(settingType);
				if (jhjSetting != null) {
					BigDecimal settingValue = new BigDecimal(jhjSetting.getSettingValue());
					BigDecimal orderIncoming = orderMoney.multiply(settingValue);
					orderIncoming = MathBigDecimalUtil.round(orderIncoming, 2);
					vo.setOrderIncoming(orderIncoming);
				}
			}
			
			if (vo.getOrderType() == 1) {
				// 深度养护收入比例 deep-ratio
				String settingType = "deep-ratio" + settingLevel;
				JhjSetting jhjSetting = settingService.selectBySettingType(settingType);
				if (jhjSetting != null) {
					BigDecimal settingValue = new BigDecimal(jhjSetting.getSettingValue());
					
					//如果多人派工，则需要做平均值
					searchVo = new OrderDispatchSearchVo();
					searchVo.setOrderNo(item.getOrderNo());
					searchVo.setDispatchStatus((short) 1);
										
					orderDispatchs = orderDispatchsService.selectBySearchVo(searchVo);
					
					int totalStaffs = orderDispatchs.size();
					
					BigDecimal orderMoneyAvg = MathBigDecimalUtil.div(orderMoney, new BigDecimal(totalStaffs));
					
					BigDecimal orderIncoming = orderMoneyAvg.multiply(settingValue);
					orderIncoming = MathBigDecimalUtil.round(orderIncoming, 2);
					vo.setOrderIncoming(orderIncoming);
				}
			}

			if (vo.getOrderType() == 2) {
				// 助理收入比例 am-ratio
				String settingType = "am-ratio" + settingLevel;
				JhjSetting jhjSetting = settingService.selectBySettingType(settingType);
				if (jhjSetting != null) {
					BigDecimal settingValue = new BigDecimal(jhjSetting.getSettingValue());
					BigDecimal orderIncoming = orderMoney.multiply(settingValue);
					orderIncoming = MathBigDecimalUtil.round(orderIncoming, 2);
					vo.setOrderIncoming(orderIncoming);
				}
			}
			if (vo.getOrderType() == 3) {
				// 配送服务收入比例 dispatch-ratio
				String settingType = "dis-ratio" + settingLevel;
				JhjSetting jhjSetting = settingService.selectBySettingType(settingType);
				if (jhjSetting != null) {
					BigDecimal settingValue = new BigDecimal(jhjSetting.getSettingValue());
					BigDecimal orderIncoming = orderMoney.multiply(settingValue);
					orderIncoming = MathBigDecimalUtil.round(orderIncoming, 2);
					vo.setOrderIncoming(orderIncoming);
				}
			}
		}

		// button_word
		vo.setButtonWord(OrderUtils.getButtonWordName(item.getOrderType(), item.getOrderStatus()));

		// 订单状态名称
		vo.setOrderStatusStr(OrderUtils.getOrderStatusName(item.getOrderType(), item.getOrderStatus()));

		// 服务地址
		vo.setServiceAddr("");
		// 服务地址经度
		vo.setServiceAddrLat("");
		// 服务地址经度
		vo.setServiceAddrLng("");
		if (item.getAddrId() > 0L) {
			UserAddrs userAddr = userAddrsService.selectByPrimaryKey(item.getAddrId());
			vo.setServiceAddr(userAddr.getName() + userAddr.getAddr());
			vo.setServiceAddrLat(userAddr.getLatitude());
			vo.setServiceAddrLng(userAddr.getLongitude());

		}
		// 取货地址
		vo.setPickAddr("");
		// 取货地址经度
		vo.setPickAddrLat("");
		// 取货地址纬度
		vo.setPickAddrLng("");

		// 距离服务地址多少米/千米
		if (orderDispatchs != null) {

			int userAddrDisance = orderDispatch.getUserAddrDistance();
			if (userAddrDisance < 0)
				userAddrDisance = 0;

			vo.setServiceAddrDistance(String.valueOf(userAddrDisance) + "米");
			if (orderDispatch.getUserAddrDistance() > 1000) {
				Double userAddrDisanceM = StringUtil.getKilometre(userAddrDisance);
				vo.setServiceAddrDistance(userAddrDisanceM.toString() + "千米");
			}

			// 取货地址
			vo.setPickAddr("");
			if (orderDispatch.getPickAddr() != null) {
				vo.setPickAddr(orderDispatch.getPickAddr());
			}

			// 距离多少米/千米
			int pickDistance = orderDispatch.getPickDistance();
			if (pickDistance < 0)
				pickDistance = 0;
			vo.setPickAddrDistance(String.valueOf(pickDistance) + "米");
			if (orderDispatch.getPickDistance() > 1000) {
				Double pickDistanceM = StringUtil.getKilometre(pickDistance);
				vo.setPickAddrDistance(pickDistanceM.toString() + "千米");
			}
			// 取货地址经度
			vo.setPickAddrLat("");
			if (orderDispatch.getPickAddrLat() != null) {
				vo.setPickAddrLat(orderDispatch.getPickAddrLat());
			}

			// 取货地址纬度
			vo.setPickAddrLng("");
			if (orderDispatch.getPickAddrLng() != null) {
				vo.setPickAddrLng(orderDispatch.getPickAddrLng());
			}

			// 如果为助理订单，则把pickAddr 也赋值到servcieAddr
			if (vo.getOrderType().equals((short) 2)) {
				vo.setServiceAddr(orderDispatch.getPickAddrName() + orderDispatch.getPickAddr());
				vo.setServiceAddrLat(orderDispatch.getPickAddrLat());
				vo.setServiceAddrLng(orderDispatch.getPickAddrLng());

			}
		}

		return vo;
	}

	@Override
	public OrderDetailVo getOrderDetailVo(Orders item, Long staffId) {

		OrderDetailVo result = new OrderDetailVo();
		OrderListVo vo = this.getOrderListVo(item, staffId);

		BeanUtilsExp.copyPropertiesIgnoreNull(vo, result);
		
		// 计算订单的收入比例
		result.setOrderRatio("");
		String settingType = "";
		if (vo.getOrderType() == 0) {
			// 钟点功能收入比例 hour-ratio
			settingType = "hour-ratio";
		}
		if (vo.getOrderType() == 1) {
			// 钟点功能收入比例 hour-ratio
			settingType = "deep-ratio";
		}
		if (vo.getOrderType() == 2) {
			// 配送服务收入比例 am-ratio
			settingType = "am-ratio";
		}

		if (vo.getOrderType() == 3) {
			// 配送服务收入比例 am-ratio
			settingType = "dis-ratio";
		}

		JhjSetting jhjSetting = settingService.selectBySettingType(settingType);
		if (jhjSetting != null) {
			result.setOrderRatio(jhjSetting.getSettingValue());
		}

		// 得出服务人员的客服电话

		jhjSetting = settingService.selectBySettingType("tell-staff");
		if (jhjSetting != null) {
			result.setTelStaff(jhjSetting.getSettingValue());
		}

		Long orgId = 0L;
		OrgStaffs orgStaffs = orgStaffsService.selectByPrimaryKey(staffId);

		if (orgStaffs != null) {
			orgId = orgStaffs.getOrgId();
		}

		if (orgId != null && orgId > 0L) {
			Orgs org = orgsService.selectByPrimaryKey(orgId);
			if (org != null) {
				result.setTelStaff(org.getOrgTel());
			}
		}

		return result;
	}

	/**
	 * 获得查询条件方法，要考虑几个点
	 * 1. 用户登录角色，店长
	 * 2. 时间
	 * 
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@Override
	public OrderSearchVo getOrderSearchVo(HttpServletRequest request, OrderSearchVo searchVo, Short orderType, Long sessionParentId){
		
		// 查询条件的组合，需要做一些逻辑判断
		// 1. 如果为运营人员，则可以看所有的门店和所有状态
		// 2. 如果为店长，则只能看当前门店和已派工到该门店的人员.
		
		// 判断是否为店长登陆，如果org > 0L ，则为某个店长，否则为运营人员.
		searchVo.setOrderType(orderType);
		if (sessionParentId > 0L)
			searchVo.setParentId(sessionParentId);
		// 处理查询条件云店--------------------------------开始
		// 1) 如果有查询条件云店org_id，则以查询条件的云店为准
		// 2) 如果没有查询条件，则判断是否为店长，并且只能看店长所在门店下的所有云店.

		Long parentId = 0L;
		String parentIdParam = request.getParameter("parentId");
		if (!StringUtil.isEmpty(parentIdParam))
			parentId = Long.valueOf(parentIdParam);

		if (parentId > 0L)
			searchVo.setParentId(parentId);

		Long orgId = 0L;
		String orgIdParam = request.getParameter("orgId");

		if (!StringUtil.isEmpty(orgIdParam))
			orgId = Long.valueOf(orgIdParam);

		if (orgId > 0L)
			searchVo.setOrgId(orgId);

		// 处理查询时间条件--------------------------------开始
		// 下单开始时间
		String startTimeStr = request.getParameter("startTimeStr");
		if (!StringUtil.isEmpty(startTimeStr)) {
			searchVo.setStartAddTime(TimeStampUtil.getMillisOfDay(startTimeStr) / 1000);
		}

		// 下单结束时间
		String endTimeStr = request.getParameter("endTimeStr");
		if (!StringUtil.isEmpty(endTimeStr)) {
			searchVo.setEndAddTime(TimeStampUtil.getMillisOfDay(endTimeStr) / 1000);
		}

		// 服务开始时间
		String serviceStartTime = request.getParameter("serviceStartTimeStr");
		if (!StringUtil.isEmpty(serviceStartTime)) {
			searchVo.setStartServiceTime(TimeStampUtil.getMillisOfDay(serviceStartTime) / 1000);
		}
		// 服务结束时间
		String serviceEndTimeStr = request.getParameter("serviceEndTimeStr");
		if (!StringUtil.isEmpty(serviceEndTimeStr)) {
			searchVo.setEndServiceTime(TimeStampUtil.getMillisOfDay(serviceEndTimeStr) / 1000);
		}
		// 处理查询时间条件--------------------------------结束

		// 处理查询状态条件--------------------------------开始
		if (searchVo.getOrderStatus() == null) {
			// 如果为店长只能看已派工状态之后的订单.
			if (sessionParentId > 0L) {
				List<Short> orderStatusList = new ArrayList<Short>();

				// 店长 可查看的 钟点工 订单状态列表： 已派工 之后的都可以查看
				// public static short ORDER_HOUR_STATUS_3=3;//已派工
				// public static short ORDER_HOUR_STATUS_5=5;//开始服务
				// public static short ORDER_HOUR_STATUS_7=7;//完成服务
				// public static short ORDER_HOUR_STATUS_8=8;//已评价
				// public static short ORDER_HOUR_STATUS_9=9;//已关闭

				orderStatusList.add(Constants.ORDER_HOUR_STATUS_3);
				orderStatusList.add(Constants.ORDER_HOUR_STATUS_5);
				orderStatusList.add(Constants.ORDER_HOUR_STATUS_7);
				orderStatusList.add(Constants.ORDER_HOUR_STATUS_8);
				orderStatusList.add(Constants.ORDER_HOUR_STATUS_9);

				searchVo.setOrderStatusList(orderStatusList);
			}
		}
		
		String addrName=searchVo.getAddrName();
		if(addrName!=null && addrName!=""){
			try {
				searchVo.setAddrName(new String(addrName.getBytes("ISO-8859-1") , "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return searchVo;
	}
}
