package com.jhj.service.users;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.jhj.po.model.order.OrderCards;
import com.jhj.po.model.order.OrderPriceExt;
import com.jhj.po.model.order.OrderPrices;
import com.jhj.po.model.order.Orders;
import com.jhj.po.model.user.UserDetailPay;
import com.jhj.po.model.user.Users;
import com.jhj.vo.user.AppUserDetailPayVo;
import com.jhj.vo.user.UserDetailSearchVo;

public interface UserDetailPayService {
	
    int deleteByPrimaryKey(Long id);

    int insert(UserDetailPay record);

    int insertSelective(UserDetailPay record);

    UserDetailPay selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDetailPay record);

    int updateByPrimaryKey(UserDetailPay record);
    
	PageInfo searchVoListPage(UserDetailSearchVo searchVo,int pageNo,int pageSize);

	UserDetailPay addUserDetailPayForOrder(Users user, Orders order, OrderPrices orderPrice, String tradeStatus, String tradeNo, String payAccount);

	void updateByPayAccount(String tradeNo, String payAccount);

	UserDetailPay selectByTradeNo(String tradeNo);

	UserDetailPay addUserDetailPayForOrderCard(Users user, OrderCards orderCard, String tradeStatus, String tradeNo, String payAccount);
	
	List<UserDetailPay> appSelectByListPage(UserDetailSearchVo searchVo, int page, int pageSize);

	List<AppUserDetailPayVo> transToListVo(List<UserDetailPay> list);
	
	AppUserDetailPayVo initVo(UserDetailPay pay);
	
	UserDetailPay initUserDetailPay();
	
	List<UserDetailPay> selectBySearchVo(UserDetailSearchVo searchVo);

	UserDetailPay addUserDetailPayForOrderPayExt(Users user, Orders order, OrderPriceExt orderPriceExt, String tradeStatus, String tradeNo, String payAccount);
	
	UserDetailSearchVo transVo(UserDetailPay userDetailPay);
}
