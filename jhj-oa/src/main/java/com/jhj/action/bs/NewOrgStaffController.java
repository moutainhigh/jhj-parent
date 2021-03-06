package com.jhj.action.bs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.github.pagehelper.PageInfo;
import com.jhj.action.admin.AdminController;
import com.jhj.common.ConstantMsg;
import com.jhj.common.ConstantOa;
import com.jhj.common.Constants;
import com.jhj.models.TreeModel;
import com.jhj.models.extention.TreeModelExtension;
import com.jhj.oa.auth.AuthHelper;
import com.jhj.oa.auth.AuthPassport;
import com.jhj.po.model.bs.OrgStaffAuth;
import com.jhj.po.model.bs.OrgStaffSkill;
import com.jhj.po.model.bs.OrgStaffTags;
import com.jhj.po.model.bs.OrgStaffs;
import com.jhj.po.model.bs.Orgs;
import com.jhj.po.model.order.OrderDispatchs;
import com.jhj.po.model.university.PartnerServiceType;
import com.jhj.po.model.user.UserTrailReal;
import com.jhj.service.bs.AuthIdCardService;
import com.jhj.service.bs.OrgStaffAuthService;
import com.jhj.service.bs.OrgStaffSkillService;
import com.jhj.service.bs.OrgStaffTagsService;
import com.jhj.service.bs.OrgStaffsService;
import com.jhj.service.bs.OrgsService;
import com.jhj.service.bs.TagsService;
import com.jhj.service.order.OrderDispatchsService;
import com.jhj.service.university.PartnerServiceTypeService;
import com.jhj.service.users.UserRefAmService;
import com.jhj.service.users.UserTrailRealService;
import com.jhj.vo.bs.NewStaffFormVo;
import com.jhj.vo.bs.NewStaffListVo;
import com.jhj.vo.order.OrderDispatchSearchVo;
import com.jhj.vo.staff.OrgStaffPoiVo;
import com.jhj.vo.staff.StaffSearchVo;
import com.jhj.vo.user.UserTrailSearchVo;
import com.meijia.utils.BeanUtilsExp;
import com.meijia.utils.DateUtil;
import com.meijia.utils.ImgServerUtil;
import com.meijia.utils.StringUtil;
import com.meijia.utils.TimeStampUtil;
import com.meijia.utils.common.extension.ArrayHelper;
import com.meijia.utils.common.extension.StringHelper;
import com.meijia.utils.vo.AppResultData;

/**
 *
 * @author :hulj
 * @Date : 2016年3月9日上午11:39:07
 * @Description: 
 *		
 *		jhj2.1   服务人员管理 ： 不再区分助理和服务人员, 统一管理为服务人员	
 */
@Controller
@RequestMapping(value = "/newbs")
public class NewOrgStaffController extends AdminController {
	
	@Autowired
	private OrgStaffsService staffService;
	
	@Autowired
	private OrgStaffTagsService orgStaTagService;
	@Autowired
	private TagsService tagService;
	
	@Autowired
	private OrgsService orgService;
	
	@Autowired
	private UserRefAmService userRefAmService;
	
	@Autowired
	private OrgStaffAuthService authService;
	
	@Autowired
	private OrgStaffSkillService skillService;
	
	@Autowired
	private PartnerServiceTypeService partService;
	
	@Autowired
	private UserTrailRealService userTrailRealService;
	
	@Autowired
	private OrderDispatchsService orderDispatchService;
	
	/**
	 * @throws UnsupportedEncodingException 
	 * @throws UnsupportedEncodingException 
	 *  @Title: newStaffList
	  * @Description: 
				服务人员列表页
	  * @param staffSearchVo
	  * @throws
	 */
	@AuthPassport
	@RequestMapping(value = "new_staff_list",method = {RequestMethod.GET,RequestMethod.POST})
	public String newStaffList(Model model, HttpServletRequest request,
			@RequestParam(value = "orgId",required = false,defaultValue = "0") Long orgId,
			@ModelAttribute("staffSearchVoModel")StaffSearchVo staffSearchVo) throws UnsupportedEncodingException{
		
		int pageNo = ServletRequestUtils.getIntParameter(request,
				ConstantOa.PAGE_NO_NAME, ConstantOa.DEFAULT_PAGE_NO);
		int pageSize = ServletRequestUtils.getIntParameter(request,
				ConstantOa.PAGE_SIZE_NAME, ConstantOa.DEFAULT_PAGE_SIZE);

		//得到 当前登录 的 门店id，并作为搜索条件
		Long sessionOrgId = AuthHelper.getSessionLoginOrg(request);
		
		if(sessionOrgId > 0L) {
			//未选择 门店， 且 当前 登录 用户 为 店长 （  session中的  orgId 不为 0）,设置搜索条件为  店长的门店
			staffSearchVo.setParentId(sessionOrgId);
		}
		if(staffSearchVo.getName()!=null){
			String name = staffSearchVo.getName();
			staffSearchVo.setName(new String(name.getBytes("ISO-8859-1"),"UTF-8"));
		}
		
		if (staffSearchVo.getStatus() == null) staffSearchVo.setStatus(1);
		
		PageInfo infoList = staffService.selectByListPage(staffSearchVo, pageNo, pageSize);
		List<OrgStaffs> list = infoList.getList();
		OrgStaffs orgStaff = null;
		for (int i = 0; i < list.size(); i++) {
			orgStaff = list.get(i);
			NewStaffListVo vo = staffService.transToNewStaffListVo(orgStaff);

			list.set(i, vo);
		}
		PageInfo result = new PageInfo(list);
		
		model.addAttribute("loginOrgId", sessionOrgId);	//当前登录的 id,动态显示搜索 条件
		model.addAttribute("staffModel", result);
		model.addAttribute("staffSearchVoModel", staffSearchVo);

		return "bs/newStaffList";
	}	
	
	/*
	 *  跳转form页
	 */
//	@AuthPassport
	@RequestMapping(value = "new_staff_form", method = RequestMethod.GET)
	public String toOrgStaffAsForm(Model model,HttpServletRequest request,
			@RequestParam("orgStaffId") Long orgStaffId) {

		NewStaffFormVo formVo = new NewStaffFormVo();
		
		String id = request.getParameter("orgStaffId");

		formVo = staffService.transToNewStaffFormVo(Long.valueOf(id));
		
		if(Long.valueOf(id) == 0L){
			//如果是新增，需要处理 几种 下拉框的 默认值
			formVo.setNation("0");
			formVo.setEdu("0");
			formVo.setBloodType("o"); //默认设置为o型血
		}
		
		List<Long> checkedAuthorityIds = new ArrayList<Long>();
		List<Integer> checkedAuthorityIntegers = new ArrayList<Integer>();
		
		List<PartnerServiceType> partServiceList = formVo.getPartServiceList();
		
		if(partServiceList!= null && partServiceList.size() > 0){
			
			for (PartnerServiceType partnerServiceType : partServiceList) {
				
				if(partnerServiceType != null ){
					Long parentId = partnerServiceType.getParentId();
					if(parentId != 0){
						checkedAuthorityIds.add(partnerServiceType.getServiceTypeId());
						checkedAuthorityIntegers.add(partnerServiceType.getServiceTypeId().intValue());
					}
				}
				
			}
			
		}
		if(!model.containsAttribute("newStaffFormVoModel")){
			Long[] checkedAuthorityIdsArray=new Long[checkedAuthorityIds.size()];
			checkedAuthorityIds.toArray(checkedAuthorityIdsArray);
			
			formVo.setSkillIds(checkedAuthorityIdsArray);
			
			formVo.setSkillIdsStr(ArrayHelper.LongtoString(checkedAuthorityIdsArray, ","));
			
			model.addAttribute("newStaffFormVoModel", formVo);
		}

		String expanded = ServletRequestUtils.getStringParameter(request, "expanded", null);
		List<TreeModel> children=TreeModelExtension.ToTreeModels(partService.getTreeList(), null, checkedAuthorityIntegers, StringHelper.toIntegerList( expanded, ","));
		
		model.addAttribute(treeDataSourceName, JSONArray.fromObject(children, new JsonConfig()).toString());
		
		//得到 当前登录 的 门店id，并作为搜索条件
		Long sessionOrgId = AuthHelper.getSessionLoginOrg(request);
		
		if(formVo.getOrgId()!=0){
			model.addAttribute("org", orgService.selectByPrimaryKey(formVo.getOrgId()));
		}

		model.addAttribute("loginOrgId", sessionOrgId);	//当前登录的 id,动态显示搜索 条件

		
		return "bs/newStaffForm";
	}

	/*
	 * 提交表单
	 */
//	@AuthPassport
	@RequestMapping(value = "new_staff_form", method = RequestMethod.POST)
	public String doOrgStaffAsForm(HttpServletRequest request, Model model,
			@ModelAttribute("newStaffFormVoModel") NewStaffFormVo formVo,
			BindingResult result) throws IOException {

		Long id = Long.valueOf(request.getParameter("staffId"));

		OrgStaffs orgStaffs = staffService.initOrgStaffs();
		
		if (id > 0L) {
			orgStaffs = staffService.selectByPrimaryKey(id);
		}
		
		BeanUtilsExp.copyPropertiesIgnoreNull(formVo, orgStaffs);

		// 解决 传递 日期参数（出生年月）的 typeMismatch 问题, 方法参数要有 BindingResult
		String birth = request.getParameter("birth");
		orgStaffs.setBirth(DateUtil.parse(birth));

		
		// 根据云店 ，设置门店Id
		Long orgId = formVo.getOrgId();
		Orgs org = orgService.selectByPrimaryKey(orgId);
		orgStaffs.setParentOrgId(org.getParentId());
		
		// 添加时： 处理上传头像
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		if (multipartResolver.isMultipart(request)) {
			// 判断 request 是否有文件上传,即多部分请求...
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) (request);
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				MultipartFile file = multiRequest.getFile(iter.next());
				if (file != null && !file.isEmpty()) {

					//在图片服务器上的 图片 存放位置
                	String url = Constants.IMG_SERVER_HOST + "/upload/";
                	
					String fileName = file.getOriginalFilename();
					String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
					fileType = fileType.toLowerCase();
					String sendResult = ImgServerUtil.sendPostBytes(url, file.getBytes(), fileType);
					
					ObjectMapper mapper = new ObjectMapper();

					HashMap<String, Object> o = mapper.readValue(sendResult, HashMap.class);

					String ret = o.get("ret").toString();

					HashMap<String, String> info = (HashMap<String, String>) o.get("info");

					String imgUrl = Constants.IMG_SERVER_HOST + "/" + info.get("md5").toString();

					orgStaffs.setHeadImg(imgUrl);
				}
			}
		}
		
		if (StringUtil.isEmpty(orgStaffs.getHeadImg().trim())) {
			orgStaffs.setHeadImg("http://www.jia-he-jia.com/u/img/default-head-img.png");
		}
		
		if (id > 0L) {
			orgStaffs.setUpdateTime(TimeStampUtil.getNow() / 1000);
			staffService.updateByPrimaryKeySelective(orgStaffs);
		} else {

			staffService.insertSelective(orgStaffs);
			
			Long staffId = orgStaffs.getStaffId();
			orgStaffs.setStaffCode(String.valueOf(1000 + staffId));
			staffService.updateByPrimaryKey(orgStaffs);
			
		}
		
		// 员工技能
		String skillIds = request.getParameter("skillId");
		
		if (!StringUtil.isEmpty(skillIds)) {
			
			skillService.delByStaffId(id);
			
			String[] skillArray = StringUtil.convertStrToArray(skillIds);

			for (int i = 0; i < skillArray.length; i++) {
				
				if (StringUtil.isEmpty(skillArray[i]))
					continue;
				
				OrgStaffSkill staffSkill = new OrgStaffSkill();
				
				staffSkill.setId(0L);
				staffSkill.setStaffId(orgStaffs.getStaffId());
				staffSkill.setServiceTypeId(Long.valueOf(skillArray[i]));
				staffSkill.setOrgId(orgStaffs.getOrgId());
				staffSkill.setParentId(orgStaffs.getParentOrgId());
				staffSkill.setAddTime(TimeStampUtil.getNowSecond());
				
				skillService.insert(staffSkill);
			}
		}
		
		/*
		 * 2016年1月22日19:14:09   录入认证信息
		 */
		String authIds = request.getParameter("authIds");
		
		if (!StringUtil.isEmpty(authIds)) {
			
			//对于 身份认证， 若是取消 所有认证，不和 正常 逻辑。
			authService.deleteByStaffId(id);
			
			String[] tagList = StringUtil.convertStrToArray(authIds);
			
			for (int i = 0; i < tagList.length; i++) {
				if (StringUtil.isEmpty(tagList[i])) continue;
				OrgStaffAuth staffAuth = authService.initOrgStaffAuth();
				
				staffAuth.setStaffId(id);
				staffAuth.setServiceTypeId(Long.valueOf(tagList[i]));
				staffAuth.setAutStatus(Constants.STAFF_AUTH_STATUS_SUCCESS);
				staffAuth.setUpdateTime(TimeStampUtil.getNowSecond());
				
				authService.insertSelective(staffAuth);
			}
		}
		
		//2016年4月16日12:01:34 存储标签
		String tagListStr = request.getParameter("tagIds");
		
		
		if (!StringUtil.isEmpty(tagListStr)) {
			
			/*
			 * 对于 修改时 将 标签  设置 为 全不选择。
			 * 	
			 *  即 此时 tagListStr == "", 不合规范，也不合逻辑！ 
			 */
			orgStaTagService.deleteByStaffId(id);
			
			String[] tagList = StringUtil.convertStrToArray(tagListStr);
			
			for (int i = 0; i < tagList.length; i++) {
				if (StringUtil.isEmpty(tagList[i])) continue;
				
				OrgStaffTags record = new OrgStaffTags();
				record.setId(0L);
				record.setStaffId(orgStaffs.getStaffId());
				record.setTagId(Long.valueOf(tagList[i]));
				record.setAddTime(TimeStampUtil.getNowSecond());
				orgStaTagService.insert(record);
			}
		}
		
		
		return "redirect:/newbs/new_staff_list";
	}
	
	
	
	/*
	 * 获取服务人员最新的地理位置
	 * */
	@AuthPassport
	@RequestMapping(value="/staffRegion",method={RequestMethod.GET,RequestMethod.POST})
	public String staffRegion(Model model,HttpServletRequest request,
			@RequestParam(value="mobile",required=false) String mobile){
		
		int pageNo = ServletRequestUtils.getIntParameter(request,
				ConstantOa.PAGE_NO_NAME, ConstantOa.DEFAULT_PAGE_NO);
		int pageSize = ServletRequestUtils.getIntParameter(request,
				ConstantOa.PAGE_SIZE_NAME, ConstantOa.DEFAULT_PAGE_SIZE);
		//获取员工的地理位置
		
//		UserTrailSearchVo searchVo = new UserTrailSearchVo();
//		searchVo.setUserType((short) 0);
//		
//		
//		
//		PageInfo pageList = userTrailRealService.selectByListPage(searchVo, pageNo, pageSize)
//		
//		PageInfo<Map> pageList = staffService.selectByListPage(mobile,pageNo,pageSize);
//		
//		
//		OrgSearchVo searchVo = new OrgSearchVo();
//		searchVo.setParentId(0L);
//		searchVo.setOrgStatus((short) 1);
//		//获取门店信息
//		List<Orgs> orgsList=orgService.selectBySearchVo(searchVo);
//		//获取云店信息
//		
//		searchVo = new OrgSearchVo();
//		searchVo.setIsCloud((short) 1);
//		List<Orgs> cloudOrgsList = orgService.selectBySearchVo(searchVo);
//		
//		model.addAttribute("orgsList", orgsList);
//		model.addAttribute("cloudOrgsList", cloudOrgsList);
//		model.addAttribute("pageInfo", pageList);
		
		return "bs/staffRegion";
	}
	
}
