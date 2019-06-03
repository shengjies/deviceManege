package com.ruoyi.project.production.devWorkDayHour.service;

import com.ruoyi.common.support.Convert;
import com.ruoyi.common.utils.TimeUtil;
import com.ruoyi.project.device.devDeviceCounts.domain.DataLogTask;
import com.ruoyi.project.device.devDeviceCounts.domain.DevDataLog;
import com.ruoyi.project.device.devDeviceCounts.mapper.DevDataLogMapper;
import com.ruoyi.project.device.devIo.mapper.DevIoMapper;
import com.ruoyi.project.device.devList.mapper.DevListMapper;
import com.ruoyi.project.production.devWorkDayHour.domain.DevWorkDayHour;
import com.ruoyi.project.production.devWorkDayHour.mapper.DevWorkDayHourMapper;
import com.ruoyi.project.production.devWorkOrder.domain.DevWorkOrder;
import com.ruoyi.project.production.devWorkOrder.mapper.DevWorkOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 工单各个IO口每天每小时数据 服务层实现
 *
 * @author zqm
 * @date 2019-04-16
 */
@Service
public class DevWorkDayHourServiceImpl implements IDevWorkDayHourService {

    /**
     * 工单每小时数据日志
     */
    private static final Logger log = LoggerFactory.getLogger(DevWorkDayHourServiceImpl.class);

    @Autowired
    private DevWorkDayHourMapper devWorkDayHourMapper;

    @Autowired
    private DevDataLogMapper dataLogMapper; // 数据上报日志

    @Autowired
    private DevWorkOrderMapper workOrderMapper; // 工单

    @Autowired
    private DevListMapper devListMapper; // 硬件列表

    @Autowired
    private DevIoMapper devIoMapper; // IO口列表


    /**
     * 查询工单各个IO口每天每小时数据信息
     *
     * @param hourId 工单各个IO口每天每小时数据ID
     * @return 工单各个IO口每天每小时数据信息
     */
    @Override
    public DevWorkDayHour selectDevWorkDayHourById(Integer hourId) {
        return devWorkDayHourMapper.selectDevWorkDayHourById(hourId);
    }

    /**
     * 查询工单各个IO口每天每小时数据列表
     *
     * @param devWorkDayHour 工单各个IO口每天每小时数据信息
     * @return 工单各个IO口每天每小时数据集合
     */
    @Override
    public List<DevWorkDayHour> selectDevWorkDayHourList(DevWorkDayHour devWorkDayHour) {
        return devWorkDayHourMapper.selectDevWorkDayHourList(devWorkDayHour);
    }

    /**
     * 新增工单各个IO口每天每小时数据
     *
     * @param devWorkDayHour 工单各个IO口每天每小时数据信息
     * @return 结果
     */
    @Override
    public int insertDevWorkDayHour(DevWorkDayHour devWorkDayHour) {
        return devWorkDayHourMapper.insertDevWorkDayHour(devWorkDayHour);
    }

    /**
     * 修改工单各个IO口每天每小时数据
     *
     * @param devWorkDayHour 工单各个IO口每天每小时数据信息
     * @return 结果
     */
    @Override
    public int updateDevWorkDayHour(DevWorkDayHour devWorkDayHour) {
        return devWorkDayHourMapper.updateDevWorkDayHour(devWorkDayHour);
    }

    /**
     * 删除工单各个IO口每天每小时数据对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteDevWorkDayHourByIds(String ids) {
        return devWorkDayHourMapper.deleteDevWorkDayHourByIds(Convert.toStrArray(ids));
    }

    /**
     * 执行每小时定时任务，统计每小时每个IO口的数据，
     */
    @Override
    public void selectCalcDataLog() {

        Date sysDateTime = TimeUtil.getLastSecondOfLastHour(); // 定时任务当前系统时间-1S XX:59:59
        Date sysDateTimeOld = TimeUtil.getLastHour(); // 定时任务当前系统时间-1H XX:00:00

        DataLogTask dataLogTask = null; // 查询数据返回包装类集合
        DevWorkDayHour devWorkDayHour = null; // 硬件IO口24小时记录对象
        List<DevDataLog> dataLogs = null; // 各个IO口上传数据记录列表集合

        try {
            // 所有正在生产或者已经完成的工单
            List<DevWorkOrder> workOrders = workOrderMapper.selectWorkOrderAllBeInOrFinish();
            for (DevWorkOrder workOrder : workOrders) { // 循环每个工单
                // 上传数据各个公司各个IO口记录集合
                dataLogs = dataLogMapper.selectDevDataLogByWorkId(workOrder.getId(),workOrder.getCompanyId());
                // 上传数据列表
                for (DevDataLog dataLog : dataLogs) {
                    try {
                        // 查询出当前系统时间与前一个小时的上传数据
                        dataLogTask = dataLogMapper.selectDataLogBeInOrFinish(dataLog.getDevId(),dataLog.getIoId(), dataLog.getWorkId(), sysDateTimeOld, sysDateTime);
                        if (dataLogTask != null && dataLogTask.getSumHour() != 0) {
                            /**
                             * 获取IO口数据的上传日期，通过上传日期判断硬件IO口24小时记录表有没有存在数据<br>
                             *     没有记录则增加硬件IO口24小时记录
                             */
                            // 查询工单各个IO口每天24小时记录是否存在
                            devWorkDayHour = devWorkDayHourMapper.selectWorkDayHourListByDate(dataLogTask.getWorkId(),dataLogTask.getDevId(), dataLogTask.getIoId(), dataLogTask.getCreateDate());
                            if (devWorkDayHour == null) { // 不存在该工单指定IO口当天的记录

                                devWorkDayHour = new DevWorkDayHour();
                                devWorkDayHour.setCompanyId(dataLogTask.getCompanyId()); // 公司ID
                                devWorkDayHour.setCreateTime(new Date()); // 创建时间
                                devWorkDayHour.setDataTime(new Date());
                                devWorkDayHour.setLineId(dataLogTask.getLineId()); // 产线ID
                                devWorkDayHour.setWorkId(dataLogTask.getWorkId()); // 工单ID
                                devWorkDayHour.setIoId(dataLogTask.getIoId()); // IO口ID
                                devWorkDayHour.setDevId(dataLogTask.getDevId()); // 硬件ID
                                devWorkDayHour.setIoOrder(dataLogTask.getIoOrder()); // IO口排序
                                devWorkDayHour.setDevName(devListMapper.selectDevListById(dataLogTask.getDevId()).getDeviceName()); // 硬件名称
                                devWorkDayHour.setIoName(devIoMapper.selectDevIoById(dataLogTask.getIoId()).getIoName()); //IO口名称
                                devWorkDayHourMapper.insertDevWorkDayHour(devWorkDayHour); // 增加记录

                                updateWorkHourData(dataLogTask); // 更新IO口每天每小时数据

                            } else { // 数据库中存在记录直接更新数据

                                updateWorkHourData(dataLogTask); // 更新IO口每天每小时数据
                            }
                        }
                    } catch (Exception e) {
                        log.info("======定时任务>>>>>统计工单每小时数据出现了异常======");
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            log.info("======定时任务>>>>>统计工单每小时数据出现了异常======");
            e.printStackTrace();
        }

    }

    /**
     * 更新硬件IO口24小时每小时数据
     * @param dataLogTask
     */
    private void updateWorkHourData(DataLogTask dataLogTask) {
        devWorkDayHourMapper.updateDevWorkDayHour2(dataLogTask.getHour() == null ? 0 : dataLogTask.getHour(),
                (dataLogTask.getSumHour() == null ? 0 : dataLogTask.getSumHour()),
                dataLogTask.getWorkId(),
                dataLogTask.getDevId(),
                dataLogTask.getIoId(),
                dataLogTask.getCreateDate() == null ? new Date() : dataLogTask.getCreateDate());
    }

}
