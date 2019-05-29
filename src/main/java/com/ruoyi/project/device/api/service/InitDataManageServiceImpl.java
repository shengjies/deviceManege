package com.ruoyi.project.device.api.service;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.device.api.form.ApiWorkForm;
import com.ruoyi.project.device.api.form.WorkDataForm;
import com.ruoyi.project.device.devCompany.domain.DevCompany;
import com.ruoyi.project.device.devCompany.mapper.DevCompanyMapper;
import com.ruoyi.project.device.devDeviceCounts.service.IDevDeviceCountsService;
import com.ruoyi.project.device.devIo.domain.DevIo;
import com.ruoyi.project.device.devIo.mapper.DevIoMapper;
import com.ruoyi.project.device.devList.domain.DevList;
import com.ruoyi.project.device.devList.mapper.DevListMapper;
import com.ruoyi.project.production.devWorkData.domain.DevWorkData;
import com.ruoyi.project.production.devWorkData.mapper.DevWorkDataMapper;
import com.ruoyi.project.production.devWorkOrder.domain.DevWorkOrder;
import com.ruoyi.project.production.devWorkOrder.mapper.DevWorkOrderMapper;
import com.ruoyi.project.production.productionLine.domain.ProductionLine;
import com.ruoyi.project.production.productionLine.mapper.ProductionLineMapper;
import com.ruoyi.project.production.workExceptionList.domain.WorkExceptionList;
import com.ruoyi.project.production.workExceptionList.mapper.WorkExceptionListMapper;
import com.ruoyi.project.production.workExceptionType.domain.WorkExceptionType;
import com.ruoyi.project.production.workExceptionType.mapper.WorkExceptionTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class InitDataManageServiceImpl implements IInitDataManageService {

    @Autowired
    private DevListMapper devListMapper;

    @Autowired
    private DevIoMapper devIoMapper;

    @Autowired
    private ProductionLineMapper productionLineMapper;

    @Autowired
    private DevCompanyMapper devCompanyMapper;

    @Autowired
    private DevWorkOrderMapper workOrderMapper;

    @Autowired
    private IDevDeviceCountsService devDeviceCountsService;

    @Autowired
    private DevWorkDataMapper devWorkDataMapper;

    @Autowired
    private WorkExceptionTypeMapper exceptionTypeMapper;

    @Autowired
    private WorkExceptionListMapper workExceptionListMapper;

    /**
     * 根据硬件编码查询对应的工单信息
     * @param code 硬件编码
     * @return
     */
    @Override
    public Map<String, Object> workOrder(String code) {
        Map<String,Object> map = new HashMap<>();
        try {
            //判断对应的硬件编码是否存在
            DevList devList = devListMapper.selectDevListByCode(code);
            if(devList == null || devList.getCompanyId() ==null){
                map.put("code",3);//硬件编码不存在
                map.put("data",null);
                return  map;
            }
            //查询对应硬件产线配置信息
            DevIo devIo = devIoMapper.selectByDevId(devList.getId());
            if(devIo == null || devIo.getLineId() == null){
                map.put("code",4);//硬件未配置产线
                map.put("data",null);
                return  map;
            }
            ApiWorkForm workForm = findLineAndWork(devList,devIo);
            if(workForm == null){
                map.put("code",2);//硬件为归属公司或者硬件未配置产线
                map.put("data",null);
                return map;
            }
            map.put("code",1);
            map.put("data",workForm);
        }catch (Exception e){
               map.put("code",0);
               map.put("data",null);
        }
        return map;
    }

    @Override
    public Map<String, Object> workData(WorkDataForm data) {
        Map<String,Object> map = new HashMap<>();
        try {
            Integer ids[] ={data.getD1(),data.getD2(),data.getD3(),data.getD4(),data.getD5(),data.getD6(),data.getD7(),data.getD8()};
            devDeviceCountsService.insertDevDeviceCounts(data.getCode(),ids);
            //判断对应的硬件编码是否存在
            DevList devList = devListMapper.selectDevListByCode(data.getCode());
            if(devList == null || devList.getCompanyId() ==null){
                map.put("code",3);//硬件编码不存在
                map.put("status",0);//没有正在进行的工单
                map.put("num",0);//没有正在进行的工单
                return  map;
            }
            //查询对应硬件产线配置信息
            DevIo devIo = devIoMapper.selectByDevId(devList.getId());
            if(devIo == null || devIo.getLineId() == null){
                map.put("code",4);//硬件未配置产线
                map.put("status",0);//没有正在进行的工单
                map.put("num",0);//没有正在进行的工单
                return  map;
            }
            ApiWorkForm workForm = findLineAndWork(devList,devIo);
            if(workForm == null || StringUtils.isEmpty(workForm.getWorkCode())){
                map.put("code",2);//硬件为归属公司或者硬件未配置产线
                map.put("status",0);//没有正在进行的工单
                map.put("num",0);//没有正在进行的工单
                return map;
            }
            map.put("code",1);//成功
            map.put("status",0);//没有正在进行的工单
            map.put("num",0);//没有正在进行的工单
            //查询对应工单的累计产量
            DevWorkData workData = devWorkDataMapper.selectWorkDataByCompanyLineWorkDev(workForm.getCompanyId(),workForm.getLineId(),
                    workForm.getWorkId(),devList.getId(),devIo.getId());
            if(workData != null && workData.getCumulativeNum() != null){
                map.put("num",workData.getCumulativeNum());//没有正在进行的工单
                map.put("status",1);//工单正在进行
            }
            return map;
        }catch (Exception e){
            map.put("code",0);//异常错误
            map.put("status",0);//没有正在进行的工单
            map.put("num",0);//没有正在进行的工单
        }
        return map;
    }

    /**
     * 异常上报
     * @param code 硬件编码
     * @return
     */
    @Override
    public Map<String, Object> workEx(String code) {
        Map<String,Object> map = new HashMap<>();
        try {
            //判断对应的硬件编码是否存在
            DevList devList = devListMapper.selectDevListByCode(code);
            if(devList == null || devList.getCompanyId() ==null){
                map.put("code",3);//硬件编码不存在
                map.put("status",0);//上报失败
                return  map;
            }
            //查询对应硬件产线配置信息
            DevIo devIo = devIoMapper.selectByDevId(devList.getId());
            if(devIo == null || devIo.getLineId() == null){
                map.put("code",4);//硬件未配置产线
                map.put("status",0);//上报失败
                return  map;
            }
            ApiWorkForm workForm = findLineAndWork(devList,devIo);
            if(workForm == null || workForm.getWorkId() == null){
                map.put("code",2);//不存在对应工单信息
                map.put("status",0);//上报失败
                return  map;
            }
            //查询对应异常类型是否存在
            WorkExceptionType exceptionType = exceptionTypeMapper.selectByCompanyAndTypeName(workForm.getCompanyId(),"异常上报");
            if(exceptionType == null){
                exceptionType = new WorkExceptionType();
                exceptionType.setCompanyId(workForm.getCompanyId());
                exceptionType.setTypeName("异常上报");
                exceptionType.setDefId(0);
                exceptionType.setCreateTime(new Date());
                exceptionTypeMapper.insertWorkExceptionType(exceptionType);
            }
            //添加异常信息
            WorkExceptionList exceptionList = new WorkExceptionList();
            exceptionList.setCompanyId(workForm.getCompanyId());
            exceptionList.setExceStatut(1);
            exceptionList.setExceType(exceptionType.getId());
            exceptionList.setWorkId(workForm.getWorkId());
            exceptionList.setRemark("设备ID："+code+",上报异常");
            exceptionList.setCreateTime(new Date());
            workExceptionListMapper.insertWorkExceptionList(exceptionList);
            map.put("code",1);//创建成功
            map.put("status",1);//上报成功
        }catch (Exception e){
            map.put("code",0);//异常错误
            map.put("status",0);//上报失败
        }
        return map;
    }

    /**
     * 根据硬件查询对应的产线信息和正在进行的工单信息
     * @param devIo
     * @return
     */
    private ApiWorkForm findLineAndWork(DevList devList,DevIo devIo) throws Exception{
        ApiWorkForm workForm = new ApiWorkForm();
        //查询公司信息
        DevCompany devCompany = devCompanyMapper.selectDevCompanyById(devList.getCompanyId());
        if(devCompany ==null)return null;
        workForm.setCompanyId(devCompany.getCompanyId());
        workForm.setCompanyLogo(devCompany.getComLogo());
        workForm.setCompanyName(devCompany.getComName());
        //查询对应的产线信息
        ProductionLine line = productionLineMapper.selectProductionLineById(devIo.getLineId());
        if(line == null)return  null;
        workForm.setLineName(line.getLineName());
        workForm.setLineId(line.getId());
        //查询对应产线正在进行的工单信息
        DevWorkOrder workOrder = workOrderMapper.selectWorkByCompandAndLine(devCompany.getCompanyId(),line.getId());
        if(workOrder != null){
            workForm.setWorkId(workOrder.getId());
            workForm.setWorkCode(workOrder.getWorkorderNumber());//工单号
            workForm.setProductCode(workOrder.getProductCode());//产品编码
            workForm.setProductName(workOrder.getProductName());//产品名称
            workForm.setWorkNumber(workOrder.getProductNumber());
            workForm.setWorkorderStatus(workOrder.getWorkorderStatus()); // 生产状态
        }
        // 查询对应工单累计生产产量信息
        DevWorkData workData = devWorkDataMapper.selectWorkDataByIosign(devCompany.getCompanyId(),workOrder.getId(),line.getId(),devList.getId());
        if (workData != null) {
            workForm.setActualNum(workData.getCumulativeNum());
        }
        return workForm;
    }

}
