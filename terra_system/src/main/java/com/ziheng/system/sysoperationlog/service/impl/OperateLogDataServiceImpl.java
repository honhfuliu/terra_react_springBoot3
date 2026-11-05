package com.ziheng.system.sysoperationlog.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.system.sysoperationlog.aspect.OperateLogAspect;
import com.ziheng.system.sysoperationlog.service.OperateLogDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class OperateLogDataServiceImpl implements OperateLogDataService {
    private static final Logger log = LoggerFactory.getLogger(OperateLogDataServiceImpl.class);


    private final ApplicationContext applicationContext;

    public OperateLogDataServiceImpl(
            ApplicationContext applicationContext
    ) {
        this.applicationContext =
                applicationContext;
    }
    /**
     * 根据ID获取数据
     *
     * @param serviceClass service类
     * @param id           ID
     * @return 对象
     */
    @Override
    public Object getByIds(Class<?> serviceClass, Object id) {
        // 1. 参数校验
        if (serviceClass == null) {
            throw new IllegalArgumentException("serviceClass 不能为空");
        }
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        if (!(id instanceof Serializable)) {
            throw new IllegalArgumentException("id 必须实现 Serializable，实际类型: " + id.getClass());
        }
        // 2. 获取 bean
        Object service;
        try {
            service = applicationContext.getBean(serviceClass);
        } catch (Exception e) {
            throw new IllegalArgumentException("找不到 Bean: " + serviceClass.getName(), e);
        }
        // 3. 类型校验
        if (!(service instanceof IService<?> iService)) {
            throw new IllegalArgumentException(serviceClass.getName() + " 必须实现 IService");
        }

        // 4. 判断单条 / 批量
        if (id instanceof Collection<?> ids) {
            if (ids.isEmpty()) {
                return Collections.emptyList();
            }
            List<Serializable> serializableIds = ids.stream()
                .map(item -> {
                    if (!(item instanceof Serializable)) {
                        throw new IllegalArgumentException("id 必须实现 Serializable，实际类型: " + item.getClass());
                    }
                    return (Serializable) item;
                })
                .toList();
            // 批量查询
            log.debug("批量删除ID: {}", serializableIds);
            return iService.listByIds(serializableIds);
        }

        // 5. 单条查询
        return iService.getById((Serializable) id);
    }
}
