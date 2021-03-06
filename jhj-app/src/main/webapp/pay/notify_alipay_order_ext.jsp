<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.alipay.config.*"%>
<%@ page import="com.alipay.util.*"%>
<%
	//获取支付宝POST过来反馈信息
	Map<String,String> params = new HashMap<String,String>();
	Map<String,String> notify_params = new HashMap<String,String>();
	Map requestParams = request.getParameterMap();
	for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
		String name = (String) iter.next();
		String[] values = (String[]) requestParams.get(name);
		String valueStr = "";
		for (int i = 0; i < values.length; i++) {
			valueStr = (i == values.length - 1) ? valueStr + values[i]
					: valueStr + values[i] + ",";
		}
		//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
		valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
		notify_params.put(name, valueStr);
	}


    AlipayCore.logResult(notify_params.toString());
	//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表

	// payOrderType 订单支付类型 0 = 订单支付 1= 充值支付 2 = 手机话费类充值 3 = 订单补差价	
	params.put("pay_order_type", "3");

	//通知ID
	String notify_id = new String(request.getParameter("notify_id").getBytes("ISO-8859-1"),"UTF-8");
	params.put("notify_id", notify_id);

	//通知时间
	String notify_time = new String(request.getParameter("notify_time").getBytes("ISO-8859-1"),"UTF-8");
	params.put("notify_time", notify_time);

	//商户订单号
	String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
	params.put("order_no_ext", out_trade_no);

	//买家账号
	String buyer_email = new String(request.getParameter("buyer_email").getBytes("ISO-8859-1"),"UTF-8");
	params.put("pay_account", buyer_email);

	//支付宝交易号
	String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
	params.put("trade_no", trade_no);

	//交易状态
	String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");

	params.put("trade_status", trade_status);

	//支付方式 0 = 余额支付 1 = 支付宝 2 = 微信支付 3 = 智慧支付 4 = 上门刷卡（保留，站位）
	params.put("pay_type", "1");
	
	String shareUserId = new String(request.getParameter("body").getBytes("ISO-8859-1"),"UTF-8");
	params.put("body",shareUserId);

	//通知参数
	//params.put("notify_params", notify_params.toString());

//	if(AlipayNotify.verify(notify_params)) {//验证成功
		String url = "http://localhost/jhj-app/app/order/online_pay_notify_ext.json";
		com.meijia.utils.AppSubmit.appAliay(url, params);
		out.println("success");	//请不要修改或删除
//	} else {//验证失败
//		out.println("fail");
//	}

%>
