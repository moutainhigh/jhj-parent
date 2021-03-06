
//表单验证规则===================================================
$('#orderViewForm').validate({
	errorElement : 'span', // default input error message container
	errorClass : 'help-block', // default input error message class
	focusInvalid : false, // do not focus the last invalid input
	rules : {

		orderOpFrom : {
			required : true,
		},
		
		groupCode : {
			digits : true,
		}
	},
	
	messages : {
		
		orderOpFrom : {
			required : "请选择订单来源",
		},
		
		groupCode : {
			digits : "团购卷都为数字",
		}
	},
	
	highlight : function(element) { // hightlight error inputs
		$(element).closest('.form-group').addClass('has-error'); // set error
		// class to
		// the
		// control
		// group
	},
	
	success : function(label) {
		label.closest('.form-group').removeClass('has-error');
		label.remove();
	},
	
	errorPlacement : function(error, element) {
		error.insertAfter(element.parents(".col-md-5"));
	}
});


$('.form_datetime').datetimepicker({
	format : "yyyy-mm-dd hh:ii",
	language : "zh-CN",
	autoclose : true,
	todayBtn : true,
	minuteStep : 30
});

$('.form_datetime').datetimepicker('setStartDate', new Date());

$('#selectedStaffs').tagsinput(
		{
			itemValue : 'id',
			itemText : 'label',
			tagClass : function(item) {
				var labelArray = [ 'label-primary', 'label-danger', 'label-success',
						'label-default', 'label-warning' ];
				var index = Math.floor((Math.random() * labelArray.length));
				var v = labelArray[index];
				return 'label ' + v;
			},
		});

$('#selectedStaffs').on('itemRemoved', function(event) {
	// event.item: contains the item
	var staffId = event.item.id;
	$("input[name='select-staff']").each(function(k, v) {
		
		var selectStaffId = $(this).parent().find("#selectStaffId").val();
		
		var selectStaffName = $(this).parent().find("#selectStaffName").val();
		
		var distanceValue = $(this).parent().find("#distanceValue").val();
		
		if (selectStaffId == staffId) {
			// 如果该行被选中
			
			$(this).removeAttr("checked");
			
		}
	});
});




// 提交派工修改
$("#submitForm").on('click', function() {
	if (confirm("请确认提交？")) {
		$('#submitForm').attr('disabled', "true");
		var orderStatus = $("#orderStatus").val();
		
		// 只有 已支付 和 已派工 的订单，可以有 调整派工操作
		
		if (orderStatus != 2 && orderStatus != 3) {
			
			alert("只有 已支付 或 已派工状态的 订单 ,可以进行调整 派工操作");
			$('#submitForm').removeAttr("disabled");
			return false;
		}
		
		var orderDate = $("#serviceDateStr").val();
		var orderDateTimeStamp = new Date(orderDate).getTime();
		// 服务时间戳
		var paramStamp = orderDateTimeStamp / 1000;
		
		// 人工 选择的 派工 人员.如果没选。默认为0
		var selectStaffIds = $("#selectedStaffs").val();
		console.log("selectStaffIds = " + selectStaffIds);
		
		// 距离 数字。。省去处理派工时，重新查表，计算距离
		
		var distanceValue = 0;
		
		var orderId = $("#id").val();
		
		// 如果 未选择派工 。直接 返回列表页
		if (selectStaffIds == undefined || selectStaffIds == "") {
			
			alert("没有选择派工人员.");
			$('#submitForm').removeAttr("disabled");
			
			return false;
		}
		
		
		// 判断派工人员个数是否一致，用于派工调整，涉及到金额
		if (orderStatus == 3 || orderStatus == 5) {
			var staffNums = $("#staffNums").val();
			if (staffNums != "" && staffNums != undefined && staffNums > 0) {
				var staffAry = selectStaffIds.split(",");
				
				if (staffAry.length != staffNums) {
					alert("派工人数应为2人，请确认派工人数是否正确");
					$('#submitForm').removeAttr("disabled");
					return false;
				}
			}
		}
		var adminId = $("#adminId").val();
		var adminName = $("#adminName").val();
		
		if (adminId == undefined || adminId == null || adminId == "" || adminId == 0) {
			alert("由于你长时间未操作或者服务器更新, 请重新登录之后再操作.");
			return false;
		}
		
		if (adminName == undefined || adminName == null || adminName == "" || adminName == 0) {
			alert("由于你长时间未操作或者服务器更新, 请重新登录之后再操作.");
			return false;
		}
		
		
		$.ajax({
			type : 'post',
			url : '/jhj-app/app/order/save_order_dispatch.json',
			data : {
				"selectStaffIds" : selectStaffIds,
				"orderId" : orderId,
				"newServiceDate" : orderDate,
//				"distanceValue" : distanceValue,
				"orderType" : 0,
				"adminId" : adminId,
				"adminName" : adminName,
			},
			dataType : 'json',
			success : function(data, status, xhr) {
				var status = data.status;
				if (status == "999") {
					alert(data.msg);
					$('#submitForm').removeAttr("disabled");
					return false;
				}
				alert("保存成功");
				$('#submitForm').removeAttr("disabled");
				var rootPath = getRootPath();
				window.location.replace(rootPath + "/order/order-list");
			},
			error : function() {
				$('#submitForm').removeAttr("disabled");
				alert("网络错误");
			}
		})
	}
});

function getRootPath() {
	var strFullPath = window.document.location.href;
	var strPath = window.document.location.pathname;
	var pos = strFullPath.indexOf(strPath);
	var prePath = strFullPath.substring(0, pos);
	var postPath = strPath.substring(0, strPath.substr(1).indexOf('/') + 1);
	return (prePath + postPath);
}

var loadStaffDynamic = function(data, status, xhr) {
	
	var result = data;
	
	// 清空原有数据
	$("#allStaff").html("");
	
	var tdHtml = "";
	
	var orderStatus = $("#orderStatus").val();
	if (orderStatus != 7 && orderStatus != 8) {
		if (result.length == 0) {
			alert("暂无可用派工");
		}
	}
	
	
	for (var i = 0, j = result.length; i < j; i++) {
		
		var item = data[i];
		
		var selectInput = "";
		selectInput = "<input name='select-staff' type='checkbox' onclick='doSelectStaff("
				+ item.staff_id + ")' value=" + item.staff_id + ">";
		
		var htmlStr = "<tr>";
		
		htmlStr += "<td>";
		if (item.dispath_sta_flag == 1) {
			htmlStr += selectInput;
		}
		var sexName = "男";
		if (item.sex == 1) sexName = "女";
		
		htmlStr += "<input  type='hidden' id='selectStaffId' name='selectStaffId' value="+ item.staff_id + ">";
		htmlStr += "<input type='hidden' id='distanceValue' value="+ item.distance_value + ">";
		htmlStr += "<input type='hidden' id='selectStaffName' value="+ item.name + "></td>";
		htmlStr += "<td>" + item.parent_org_name + "</td>";
		htmlStr += "<td>" + item.org_name + "</td>";
		htmlStr += "<td>" + item.org_distance_text + "</td>";
		htmlStr += "<td>" + item.name + "</td>";
		htmlStr += "<td>" + item.mobile + "</td>";
		htmlStr += "<td>" + sexName + "</td>";
		htmlStr += "<td>" + item.distance_text + "</td>";
		htmlStr += "<td>" + item.today_order_num + "</td>";
		htmlStr += "<td>" + item.pre_day_order_num + "</td>";
		htmlStr += "<td>" + item.dispath_sta_str + "</td>";
		htmlStr += "<td>" + item.reason + "</td>";
		htmlStr += "<td>" + item.allocate_reason + "</td>";
		htmlStr += "</tr>";
		
		tdHtml += htmlStr;
		
	}
	
	$("#allStaff").append(tdHtml);
	
	return false;
	
}

// 改变 服务时间时,动态获取派工
$('.form_datetime').datetimepicker().on('changeDate', function(ev) {
	
	// 时间戳 ms值
	var date = ev.date.valueOf();
	// 东八区 减去 8小时
	var newServiceDate = date / 1000 - 8 * 3600;
	
	loadStaffsByServiceDate(newServiceDate);
	
});

function loadStaffsByServiceDate(serviceDate) {
	// 根据 服务 时间, 动态获取 有无 可用派工
	var orderId = $("#id").val();
	var orderStatus = $("#orderStatus").val();
	var addrId = $("#addrId").val();
	var serviceHour = $("#serviceHour").val();
	var serviceTypeId = $("#serviceType").val();
	if (orderStatus < 2) return false;
	
	$.ajax({
		type : "get",
		url : "/jhj-oa/new_dispatch/load_staff_by_change_service_date.json",
		data : {
			"addrId" : addrId,
			"serviceTypeId" : serviceTypeId,
			"orderStatus" : orderStatus,
			"serviceHour" : serviceHour,
			"serviceDate" : serviceDate
		},
		dataType : "json",
		success : loadStaffDynamic
	});
}

function doSelectStaff(staffId) {
	console.log("doSelectStaff");
	var selectStaffId = "";
	var selectStaffName = "";
	var distanceValue = "";
	$("input[name='select-staff']").each(function(k, v) {
		
		var selectStaffId = $(this).parent().find("#selectStaffId").val();
		
		var selectStaffName = $(this).parent().find("#selectStaffName").val();
		
		var distanceValue = $(this).parent().find("#distanceValue").val();
		
		if (selectStaffId == staffId) {
			console.log("selectStaffName = " + selectStaffName);
			// 如果该行被选中
			if (this.checked) {
				addSelectedStaffs(selectStaffId, selectStaffName, distanceValue);
			} else {
				$('#selectedStaffs').tagsinput('remove', {
					id : selectStaffId,
					label : selectStaffName,
					distanceValue : distanceValue
				});
			}
		}
		
	});
	
	console.log($('#selectedStaffs').val());
}

/*
 * 选择 云店时，动态 展示 对应云店的 阿姨 派工状态
 */
$("#parentId").on('change', function() {
	loadStaffs();
});

$("#orgId").on('change', function() {
	loadStaffs();
});

function loadStaffs() {
	
	// 当前选中的 云店id
	var parentId = $("#parentId").val();
	
	// 根据 服务 时间, 动态获取 有无 可用派工
	var orderId = $("#id").val();
	
	// 如果未选择门店，直接返回
	if (parentId == 0) {
		return false;
	}
	
	// 获取当前选择的订单时间
	var serviceDateStr = $("#serviceDateStr").val() + ":00";
	var serviceDate = moment(serviceDateStr).unix();
	
	var orgId = $("#orgId").val();
	var orderStatus = $("#orderStatus").val();
	var addrId = $("#addrId").val();
	var serviceHour = $("#serviceHour").val();
	var serviceTypeId = $("#serviceType").val();
	
	$.ajax({
		type : "get",
		url : "/jhj-oa/new_dispatch/load_staff_by_change_cloud_org.json",
		data : {
			"parentId" : parentId,
			"orgId" : orgId,
			"addrId" : addrId,
			"serviceTypeId" : serviceTypeId,
			"orderStatus" : orderStatus,
			"serviceHour" : serviceHour,
			"serviceDate" : serviceDate
		},
		dataType : "json",
		success : loadStaffDynamic
	});
}

// 点击选择 调整派工方案
$("input[name='disWay']").on("change", function() {
	
	var thisVal = $("input[name='disWay']:checked").val();
	console.log("disway =" + thisVal);
	if (thisVal == 0) {
		$("#disWayOne").show();
		// $("#disWayTwo").hide();
		$("#div-org-id").hide();
		$("#div-cloud-id").hide();
	}
	
	if (thisVal == 1) {
		$("#disWayOne").hide();
		// $("#disWayTwo").show();
		
		$("#div-org-id").show();
		$("#div-cloud-id").show();
	}
})

// 页面加载时， 回显 已选中的 派工结果
var selectStaff = function() {
	
	var staffId = $("#staffId").val();
	$("input[name='sample-radio']").each(function(k, v) {
		
		var selectStaffId = $(this).val();
		
		if (selectStaffId == staffId) {
			
			$(this).attr("checked", true);
		}
		console.log("selectStaffId = " + selectStaffId);
		console.log("staffId = " + staffId);
	});
	
	$("input[name='disWay']").trigger("change");
	
	var disWay = $("input[name='disWay']:checked").val();
	
	if (disWay == 0) {
		var serviceDateStr = $("#serviceDateStr").val();
		
		if (serviceDateStr.length == 16) serviceDateStr += ":00";
		
		var newServiceDate = moment(serviceDateStr).unix();
		
		// newServiceDate = newServiceDate / 1000 - 8 * 3600;
		
		console.log("newServiceDate =" + newServiceDate);
		
		loadStaffsByServiceDate(newServiceDate);
	}
	
	if (disWay == 1) {
		$("#parentId").trigger("change");
	}
	
}

// 取消订单
$("#cancleForm").on("click", function() {
	var id = $("#id").val();
	var remarks = $("#remark").val();
	if(remarks==undefined || remarks==null || remarks==''){
		$('#remark-error').text("取消原因不能为空！").css("color","red");
		return false;
	} 
	if (confirm("请确认取消订单吗？")) {
		console.log("asdfasdfasdf");
		$("#cancleOrder").attr("disabled",false);
		var params = {};
		params.order_id = id;
		params.remarks = remarks;
		$.ajax({
			type : "POST",
			url : "/jhj-oa/order/cancelOrder.json",
			dataType : "json",
			data:params,
			success : function(data) {
				$("#cancleOrder").attr("disabled",true);
				var result = data.data;
				if (result.status == 999) {
					alert(result.msg);
					return;
				}
				alert("订单取消成功.");
				var rootPath = getRootPath();
				window.location.replace(rootPath + "/order/order-list");
			}
		});
	}
});

function addSelectedStaffs(selectStaffId, selectStaffName, distanceValue) {
	console.log("addSelectedStaffs");
	var selectStaffIds = $("#selectedStaffs").val();
	selectStaffIds += ",";
	console.log("selectStaffIds = " + selectStaffIds);
	if (selectStaffIds.indexOf(selectStaffId + ",") >= 0) return false;
	
	$('#selectedStaffs').tagsinput('add', {
		id : selectStaffId,
		label : selectStaffName,
		distanceValue : distanceValue
	});
}

function remove(selectStaffId, selectStaffName, distanceValue) {
	var selectStaffIds = $("#selectedStaffs").val();
	
	if (selectStaffIds.indexOf(selectStaffId) >= 0) return false;
	
	$('#selectedStaffs').tagsinput('add', {
		id : selectStaffId,
		label : selectStaffName,
		distanceValue : distanceValue
	});
}

window.onload = selectStaff;

//查看订单日志
$("#checkOrderLog").on('click',function(){
	var orderNo = $("#orderNo").val();
	if(orderNo==undefined || orderNo ==null || orderNo == '') return false;
	
	$.ajax({
		type:"GET",
		url:"/jhj-app/app/orderLog/orderLog-list.json",
		data:{
			"order_no":orderNo
		},
		dataType:"json",
		success:function(data){
			if(data.status==0){
				var result = data.data;
				if(result.length>0){
					var html = ''; 
					for(var i=0,len=result.length;i<len;i++){
						var orderLog = result[i];
						var htmlText="";
						htmlText+="<tr><td>"+(i+1)+"</td><td>"+orderLog.action+"</td><td>"+orderLog.real_name+"</td><td>"+
						orderLog.user_type_name+"</td><td>"+orderLog.remarks+"</td><td>"+orderLog.add_time_str+"</td></tr>";
						html += htmlText;
					}
					$("#showOrderLog").html("").html(html);
				}
			}else if(data.status==999){
				alert("没有数据！");
			}
		}
	});
});

$(function(){
	var validateCode = $("#validateCode").val();
	if(validateCode=='1'){
		document.getElementById("groupCode").disabled = true;
	}
});

//验码
$("#btn-validate").on('click',function(){
	var orderId = $("#id").val();
	var validateCode = $("#validateCode").val();
	if(validateCode=='0'){
		validateCode = '1';
	}else{
		validateCode = '0';
	}
	$.ajax({
		type:"POST",
		url:"validate-code",
		data:{
			"orderId":orderId,
			"validateCode":validateCode
		},
		dataType:"json",
		success:function(data){
			$("#validateCode").val(data.validateCode);
			$("#btn-validate").text(data.btnValue);
			if(data.validateCode=='1'){
				$("#groupCode").attr("disabled","disabled");
			}else{
				document.getElementById("groupCode").disabled = false;
			}
		}
	});
});

$("#btn-submit").on('click',function(e){
	e.preventDefault();
	var param = {};
	param.orderNo = $("#orderNo").val();
	param.orderOpFrom = $("#orderOpFrom").val();
	param.groupCode = $("#groupCode").val();
	$.ajax({
		type:"POST",
		url:"update-order",
		data:param,
		dataType:"json",
		success:function(data){
			layer.alert("修改成功",{icon: 1});
		}
	});
});

