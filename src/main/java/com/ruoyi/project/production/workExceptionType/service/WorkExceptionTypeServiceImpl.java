package com.ruoyi.project.production.workExceptionType.service;

import java.util.Date;
import java.util.List;

import com.ruoyi.common.utils.security.ShiroUtils;
import com.ruoyi.project.system.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project.production.workExceptionType.mapper.WorkExceptionTypeMapper;
import com.ruoyi.project.production.workExceptionType.domain.WorkExceptionType;
import com.ruoyi.project.production.workExceptionType.service.IWorkExceptionTypeService;
import com.ruoyi.common.support.Convert;

/**
 * 工单工单异常类型 服务层实现
 *
 * @author zqm
 * @date 2019-04-16
 */
@Service("exceType")
public class WorkExceptionTypeServiceImpl implements IWorkExceptionTypeService {
    @Autowired
    private WorkExceptionTypeMapper workExceptionTypeMapper;

    /**
     * 查询工单工单异常类型信息
     *
     * @param id 工单工单异常类型ID
     * @return 工单工单异常类型信息
     */
    @Override
    public WorkExceptionType selectWorkExceptionTypeById(Integer id) {
        return workExceptionTypeMapper.selectWorkExceptionTypeById(id);
    }

    /**
     * 查询工单工单异常类型列表
     *
     * @param workExceptionType 工单工单异常类型信息
     * @return 工单工单异常类型集合
     */
    @Override
    public List<WorkExceptionType> selectWorkExceptionTypeList(WorkExceptionType workExceptionType) {
        if (User.isSys(ShiroUtils.getSysUser()) == false) { // 非系统用户，只能查看本公司的异常类型
            workExceptionType.setCompanyId(ShiroUtils.getSysUser().getCompanyId());
        }
        return workExceptionTypeMapper.selectWorkExceptionTypeList(workExceptionType);
    }

    /**
     * 新增工单工单异常类型
     *
     * @param workExceptionType 工单工单异常类型信息
     * @return 结果
     */
    @Override
    public int insertWorkExceptionType(WorkExceptionType workExceptionType) {
        workExceptionType.setCompanyId(ShiroUtils.getSysUser().getCompanyId()); // 设置异常类型所属公司
        workExceptionType.setCreateTime(new Date()); // 设置异常类型的创建时间
        return workExceptionTypeMapper.insertWorkExceptionType(workExceptionType);
    }

    /**
     * 修改工单工单异常类型
     *
     * @param workExceptionType 工单工单异常类型信息
     * @return 结果
     */
    @Override
    public int updateWorkExceptionType(WorkExceptionType workExceptionType) {
        return workExceptionTypeMapper.updateWorkExceptionType(workExceptionType);
    }

    /**
     * 删除工单工单异常类型对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteWorkExceptionTypeByIds(String ids) {
        return workExceptionTypeMapper.deleteWorkExceptionTypeByIds(Convert.toStrArray(ids));
    }

    /**
     * 查询所有的异常类型
     * @return
     */
    public List<WorkExceptionType> findExceTypeAll(){
        return workExceptionTypeMapper.findExceTypeAll(ShiroUtils.getSysUser().getCompanyId());
    }
}
