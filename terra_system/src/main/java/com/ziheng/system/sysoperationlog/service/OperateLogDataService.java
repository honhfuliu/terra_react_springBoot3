package com.ziheng.system.sysoperationlog.service;


public interface OperateLogDataService {
    /**
     * 根据ID获取数据
     *
     * @param serviceClass service类
     * @param id           ID
     * @return 对象
     */
    Object getByIds(Class<?> serviceClass, Object id);

}
