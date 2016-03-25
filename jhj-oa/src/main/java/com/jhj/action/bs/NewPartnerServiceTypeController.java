package com.jhj.action.bs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import com.jhj.action.admin.AdminController;
import com.jhj.common.Constants;
import com.jhj.models.TreeModel;
import com.jhj.models.extention.TreeModelExtension;
import com.jhj.po.model.university.PartnerServiceType;
import com.jhj.service.university.PartnerServiceTypeService;
import com.meijia.utils.BeanUtilsExp;
import com.meijia.utils.ImgServerUtil;
import com.meijia.utils.common.extension.StringHelper;

/**
 *
 * @author :hulj
 * @Date : 2016年3月10日上午11:09:50
 * @Description: 
 *		
 *		服务类别管理 -- jhj2.1, 技能类别
 */
@Controller
@RequestMapping(value = "/newbs")
public class NewPartnerServiceTypeController extends AdminController {
	
	
	@Autowired
	private PartnerServiceTypeService partService;
	
	
	/**
	 * 
	 *  @Title: serviceTypeList
	  * @Description: 
	  * 		jhj2.1 服务类别树形展示		
	 */
	@RequestMapping(value = "/service_type_list", method = { RequestMethod.GET })
	public String serviceTypeList(HttpServletRequest request, Model model) {
		if (!model.containsAttribute("contentModel")) {
			String expanded = ServletRequestUtils.getStringParameter(request,"expanded", null);
			List<TreeModel> children = TreeModelExtension.ToTreeModels(partService.getTreeList(), null, null,
					StringHelper.toIntegerList(expanded, ","));
			List<TreeModel> treeModels = new ArrayList<TreeModel>(Arrays.asList(new TreeModel("0", "0", "根节点", false, false,
							false, children)));
			String jsonString = JSONArray.fromObject(treeModels,new JsonConfig()).toString();
			model.addAttribute("contentModel", jsonString);
		}
		model.addAttribute("requestUrl", request.getServletPath());
		model.addAttribute("requestQuery", request.getQueryString());

		return "bs/serviceType/serviceList";
	}
	
	/**
	 * 
	  * @Description: 
			跳转到 添加 同级或子级结点 页面	
	  * @param @param id
	  * @param @return    设定文件
	  * @return String    返回类型
	  * @throws
	 */
	@RequestMapping(value = "/add/{id}", method = { RequestMethod.GET })
	public String toAddServiceForm(HttpServletRequest request, Model model,
			@PathVariable(value = "id") Integer id) {
		if (!model.containsAttribute("contentModelForm")) {
			
			PartnerServiceType partner = partService.initPartner();
			partner.setParentId(id.longValue());
			
			model.addAttribute("contentModelForm", partner);
		}
		
		List<TreeModel> treeModels;
		String expanded = ServletRequestUtils.getStringParameter(request,"expanded", null);
		if (id != null && id > 0) {
			List<TreeModel> children = TreeModelExtension.ToTreeModels(	partService.getTreeList(), id, null,
					StringHelper.toIntegerList(expanded, ","));
			treeModels = new ArrayList<TreeModel>(Arrays.asList(new TreeModel("0", "0", "根节点", false, false, false, children)));
		} else {
			List<TreeModel> children = TreeModelExtension.ToTreeModels(
					partService.getTreeList(), null, null,
					StringHelper.toIntegerList(expanded, ","));
			treeModels = new ArrayList<TreeModel>(Arrays.asList(new TreeModel(
					"0", "0", "根节点", false, true, false, children)));
		}
		model.addAttribute(treeDataSourceName,JSONArray.fromObject(treeModels, new JsonConfig()).toString());
		
		return "bs/serviceType/serviceForm";
	}
	
	/**
	 * 根据页面选择的id,增加新节点
	 * @param request
	 * @param model
	 * @param adminAuthorityVo
	 * @param id
	 * @param result
	 * @return 权限的树形展示页面
	 * @throws IOException 
	 */
	@RequestMapping(value = "/add/{id}", method = { RequestMethod.POST })
	public String addService(HttpServletRequest request,Model model,
			@Valid @ModelAttribute("contentModelForm") PartnerServiceType paramServiceType,
			BindingResult result,
			@PathVariable(value = "id") String id) throws IOException {
		
		String returnUrl = ServletRequestUtils.getStringParameter(request,
				"returnUrl", null);

		PartnerServiceType partner = partService.initPartner();
		
		BeanUtilsExp.copyPropertiesIgnoreNull(paramServiceType, partner);
		
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


					HashMap<String, String> info = (HashMap<String, String>) o.get("info");

					String imgUrl = Constants.IMG_SERVER_HOST + "/" + info.get("md5").toString();
					
					partner.setServiceImgUrl(imgUrl);
				}
			}
		}
		
		partService.insert(partner);
		
		if (returnUrl == null)	returnUrl = "newbs/service_type_list";
		
		return "redirect:" + returnUrl;
	}
	
	
	/**
	 * 根据id编辑对应的权限
	 * @param request
	 * @param model
	 * @param id
	 * @return 跳转到编辑页面
	 */
	@RequestMapping(value = "/edit/{id}", method = { RequestMethod.GET })
	public String edit(HttpServletRequest request, Model model,
			@PathVariable(value = "id") Long id) {
		
		if (!model.containsAttribute("contentModelForm")) {
			
			PartnerServiceType serviceType = partService.selectByPrimaryKey(id);
			
			model.addAttribute("contentModelForm", serviceType);
		}
		List<TreeModel> treeModels;
		PartnerServiceType editModel = (PartnerServiceType) model.asMap().get("contentModelForm");
		String expanded = ServletRequestUtils.getStringParameter(request,"expanded", null);
		if (editModel.getParentId() != null && editModel.getParentId() > 0) {
			List<TreeModel> children = TreeModelExtension.ToTreeModels(
					partService.getTreeList(), editModel.getParentId()
							.intValue(), null, StringHelper.toIntegerList(
							expanded, ","));
			treeModels = new ArrayList<TreeModel>(Arrays.asList(new TreeModel(
					"0", "0", "根节点", false, false, false, children)));
		} else {
			List<TreeModel> children = TreeModelExtension.ToTreeModels(
					partService.getTreeList(), null, null,
					StringHelper.toIntegerList(expanded, ","));
			treeModels = new ArrayList<TreeModel>(Arrays.asList(new TreeModel(
					"0", "0", "根节点", false, true, false, children)));
		}
		model.addAttribute("treeDataSource",
				JSONArray.fromObject(treeModels, new JsonConfig()).toString());
		
		
		return "bs/serviceType/serviceForm";
	}
	
	/**
	 *根据id更新权限
	
	
		2016年3月16日14:16:48 
			1.处理 typeMismatch ,注意  bindResult 必须紧跟在  @modelAttribute 注解后边
			
			2.第二种方法，可以使用 @initBinder 注解处理 date、double等类型 在 springmvc的处理
	 */
	@RequestMapping(value = "/edit/{id}", method = { RequestMethod.POST })
	public String editIt(HttpServletRequest request,	Model model,
			@Valid @ModelAttribute("contentModelForm") PartnerServiceType partnerServiceType,
			BindingResult result,
			@PathVariable(value = "id") Long id) throws JsonParseException, JsonMappingException, IOException {
		
		
//		if (result.hasErrors()) 
//			return edit(request, model, id);
		
		String returnUrl = ServletRequestUtils.getStringParameter(request,"returnUrl", null);
		
		if(partnerServiceType!=null){
			
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

						HashMap<String, String> info = (HashMap<String, String>) o.get("info");

						String imgUrl = Constants.IMG_SERVER_HOST + "/" + info.get("md5").toString();
						
						partnerServiceType.setServiceImgUrl(imgUrl);
					}
				}
			}
			
			partnerServiceType.setServiceTypeId(id);
			partService.updateByPrimaryKeySelective(partnerServiceType);
		}
		
		if (returnUrl == null) returnUrl = "newbs/service_type_list";
		
		return "redirect:" + returnUrl;
	}
	
	/**
	 * 根据id删除权限
	 * @param request
	 * @param model
	 * @param id
	 * @return 跳转到权限树形展示
	 */
	@RequestMapping(value = "/delete/{id}", method = { RequestMethod.GET })
	public String delete(HttpServletRequest request, Model model,@PathVariable(value = "id") String id) {
		
		Long ids = Long.valueOf(id.trim());
		
		//根据id查找出对应的该权限对象
		partService.deleteByPrimaryKey(ids);
		
		String returnUrl = ServletRequestUtils.getStringParameter(request,
				"returnUrl", null);
		if (returnUrl == null)
			returnUrl = "newbs/service_type_list";
		return "redirect:" + returnUrl;
	}

}