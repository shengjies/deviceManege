package com.ruoyi.project.device.devList.service;

import com.ruoyi.project.device.devList.domain.DevList;
import java.util.List;

/**
 * 硬件 服务层
 * 
 * @author ruoyi
 * @date 2019-04-08
 */
public interface IDevListService 
{
	/**
     * 查询硬件信息
     * 
     * @param id 硬件ID
     * @return 硬件信息
     */
	public DevList selectDevListById(Integer id);
	
	/**
     * 查询硬件列表
     * 
     * @param devList 硬件信息
     * @return 硬件集合
     */
	public List<DevList> selectDevListList(DevList devList);
	
	/**
     * 新增硬件
     * 
     * @param
     * @return 结果
     */
	public int insertDevList(int devModelId);
	
	/**
     * 修改硬件
     * 
     * @param devList 硬件信息
     * @return 结果
     */
	public int updateDevList(DevList devList);

	/**
	 * 用户添加硬件
	 * @param devList 硬件信息
	 * @return
	 */
	int addSave(DevList devList);
		
	/**
     * 删除硬件信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
	public int deleteDevListByIds(String ids);

	/**
	 * 查询对应的硬件信息和对应的IO数据
	 * @param id
	 * @return
	 */
	DevList selectDevListAndIoById(Integer id);

	/**
	 * 获取前20个没有配置的硬件编号
	 * @return
	 */
	List<String> selectNoConfigDevice();

	/**
	 * 查询所以的硬件信息
	 * @return
	 */
	List<DevList> selectAll();
	/**
	 * 验证硬件
	 */
	int  deviceValidate(String code);
}
