package com.jhj.service.impl.university;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.jhj.po.dao.university.PartnerServiceTypeMapper;
import com.jhj.po.model.university.PartnerServiceType;
import com.jhj.service.university.PartnerServiceTypeService;
import com.jhj.vo.PartnerServiceTypeSearchVo;
import com.jhj.vo.PartnerServiceTypeVo;
import com.jhj.vo.app.AmSkillVo;
import com.jhj.vo.bs.NewPartnerServiceVo;
import com.jhj.vo.university.OaPartnerServiceTypeVo;
import com.meijia.utils.BeanUtilsExp;

@Service
public class PartnerServiceTypeImpl implements PartnerServiceTypeService {
	
	@Autowired
	private PartnerServiceTypeMapper  partMapper;
	
	@Override
	public int deleteByPrimaryKey(Long serviceTypeId) {
		return partMapper.deleteByPrimaryKey(serviceTypeId);
	}

	@Override
	public int insert(PartnerServiceType record) {
		return partMapper.insert(record);
	}

	@Override
	public int insertSelective(PartnerServiceType record) {
		return partMapper.insertSelective(record);
	}

	@Override
	public PartnerServiceType selectByPrimaryKey(Long serviceTypeId) {
		return partMapper.selectByPrimaryKey(serviceTypeId);
	}

	@Override
	public int updateByPrimaryKeySelective(PartnerServiceType record) {
		return partMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int updateByPrimaryKey(PartnerServiceType record) {
		return partMapper.updateByPrimaryKey(record);
	}

	@Override
	public List<PartnerServiceType> selectByListPage(int pageNo, int pageSize) {
		
		PageHelper.startPage(pageNo, pageSize);
		List<PartnerServiceType> list = partMapper.selectByListPage();
		return list;
	}

	@Override
	public PartnerServiceType initPartner() {
		
		PartnerServiceType type = new PartnerServiceType();
		
		type.setServiceTypeId(0L);
		type.setNo((short)0);
		type.setParentId(0L);
		type.setName("");
		type.setViewType((short)0);  //0 = 类别   1=商品
		
		
		type.setUnit("");
		type.setDefaultNum((short)0);
		type.setPrice(new BigDecimal(0));
		type.setStaffPrice(new BigDecimal(0));
		type.setMprice(new BigDecimal(0));
		type.setStaffMpprice(new BigDecimal(0));
		type.setPprice(new BigDecimal(0));
		type.setStaffPprice(new BigDecimal(0));
		type.setAprice(new BigDecimal(0));
		type.setStaffApprice(new BigDecimal(0));
		type.setApprice(new BigDecimal(0));
		type.setStaffApprice(new BigDecimal(0));
		
		
		
		type.setRemarks("");
		
		type.setServiceImgUrl("");
		
		type.setEnable((short)1); //0=不可用   1=可用
		
		
		type.setServiceProperty((short)0); // 0= 单品  1=全年定制
		
		type.setServiceTimes(0.0); // 每周定制次数
		
		type.setServiceContent("");
		
		type.setIsAuto((short) 1);
		
		type.setIsMulti((short) 0);
		return type;
	}

	@Override
	public List<PartnerServiceType> selectAll() {
		return partMapper.selectAll();
	}

	@Override
	public OaPartnerServiceTypeVo completeVo(PartnerServiceType partner) {
		
		Long parentId = partner.getParentId();
		
		PartnerServiceType serviceType = selectByPrimaryKey(parentId);
		
		OaPartnerServiceTypeVo typeVo = new OaPartnerServiceTypeVo();
		
		BeanUtilsExp.copyPropertiesIgnoreNull(partner, typeVo);
		
		if(serviceType !=null){
			
			typeVo.setParentName(serviceType.getName());
		}else{
			typeVo.setParentName("");
		}
		
		return typeVo;
	}
	
	
	/*
	 * 所有 一级 serviceType  (parentId == 0)
	 * 	
	 * 	如  钟点工、助理、快送
	 */
	@Override
	public List<Long> selectNoParentServiceId() {
		
		List<PartnerServiceType> list = partMapper.selectNoParentService();
		
		List<Long> serviceTypeIdList = new ArrayList<Long>();
		
		for (PartnerServiceType partnerServiceType : list) {
			
			Long serviceTypeId = partnerServiceType.getServiceTypeId();
			
			serviceTypeIdList.add(serviceTypeId);
		}
		
		return serviceTypeIdList;
	}
	
	
	@Override
	public List<PartnerServiceType> selectNoParentServiceObj() {
		
		return partMapper.selectNoParentService();
	}
	
	@Override
	public List<NewPartnerServiceVo> getTreeList() {
		
		List<NewPartnerServiceVo> listVo = new ArrayList<NewPartnerServiceVo>();
		//根据parentId=0 查询出所用的父节点
		PartnerServiceTypeVo serviceTypeVo=new PartnerServiceTypeVo();
		serviceTypeVo.setParentId(0L);
		serviceTypeVo.setEnable((short)1);
		List<PartnerServiceType> list = partMapper.selectByPartnerServiceTypeVo(serviceTypeVo);
		
		Iterator<PartnerServiceType> iterator = list.iterator(); 
		
		while(iterator.hasNext()) {
			
			PartnerServiceType serviceType = iterator.next();
			
			NewPartnerServiceVo vo2 = transServiceToTree(serviceType.getServiceTypeId());
			
			listVo.add(vo2);
		}
		return listVo;
	}
	
	@Override
	public NewPartnerServiceVo transServiceToTree(Long id) {
		
		NewPartnerServiceVo initVo = initVo();
		
		//根据id查出某对象
		PartnerServiceType serviceType = partMapper.selectByPrimaryKey(id);
		
		//赋值给 树形 VO	
		BeanUtilsExp.copyPropertiesIgnoreNull(serviceType, initVo);

		
		initVo.setId(serviceType.getServiceTypeId().intValue());
		initVo.setVersion(0);
		
		
		//查询 该 结点下的所有  子节点,(不包含孙子节点)
		PartnerServiceTypeVo serviceTypeVo=new PartnerServiceTypeVo();
		serviceTypeVo.setParentId(id);
		serviceTypeVo.setEnable((short)1);
//		List<PartnerServiceType> list = partMapper.selectByParentId(id);
		
		List<PartnerServiceType> list = partMapper.selectByPartnerServiceTypeVo(serviceTypeVo);
		
		for (PartnerServiceType partnerServiceType : list) {
			
			if (partnerServiceType !=null && partnerServiceType.getServiceTypeId() !=null) {
				
				NewPartnerServiceVo vo = transServiceToTree(partnerServiceType.getServiceTypeId());
				
				//递归调用, 得到 某个 根节点 及其 叶子子节点
				initVo.getChildren().add(vo);
			}
		}
		return initVo;
	}
	
	@Override
	public NewPartnerServiceVo initVo() {
		
		NewPartnerServiceVo serviceVo = new NewPartnerServiceVo();
		
		PartnerServiceType partner = initPartner();
		
		BeanUtilsExp.copyPropertiesIgnoreNull(partner, serviceVo);
		
		serviceVo.setUrl("");
		serviceVo.setMatchUrl("");
		serviceVo.setItemIcon("");
		serviceVo.setParentId(0L);
		serviceVo.setChildList(new ArrayList<NewPartnerServiceVo>());
		
		return serviceVo;
	}
	
	@Override
	public List<PartnerServiceType> selectByIds(List<Long> ids) {
		return partMapper.selectByIds(ids);
	}
	
	@Override
	public List<Long> selectChildIdByParentId(Long id) {
		
		List<Long> list = new ArrayList<Long>();
		
//		List<PartnerServiceType> list2 = selectByParentId(id);
		PartnerServiceTypeVo serviceTypeVo=new PartnerServiceTypeVo();
		serviceTypeVo.setParentId(id);
		serviceTypeVo.setEnable((short)1);
		List<PartnerServiceType> list2 = partMapper.selectByPartnerServiceTypeVo(serviceTypeVo);
		
		for (PartnerServiceType partnerServiceType : list2) {
			list.add(partnerServiceType.getServiceTypeId());
		}
		
		return list;
	}
	
	@Override
	public List<AmSkillVo> selectSkillNameAndParent(List<Long> childServiceIdList) {
		return partMapper.selectSkillNameAndParent(childServiceIdList);
	}

	@Override
	public List<PartnerServiceType> selectByPartnerServiceTypeVo(
			PartnerServiceTypeVo vo) {
		return partMapper.selectByPartnerServiceTypeVo(vo);
	}
	
	@Override
	public PartnerServiceType findServiceType(List<PartnerServiceType> list, Long serviceTypeId) {
		PartnerServiceType serviceType = null;
		
		for (PartnerServiceType item : list) {
			if (item.getServiceTypeId().equals(serviceTypeId)) {
				serviceType = item;
				break;
			}
		}
		
		return serviceType;
	}
	
	@Override
	public List<PartnerServiceType> selectBySearchVo(PartnerServiceTypeSearchVo searchVo) {
		return partMapper.selectBySearchVo(searchVo);
	}
	
}
