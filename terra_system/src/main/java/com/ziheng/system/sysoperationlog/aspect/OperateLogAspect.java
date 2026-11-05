package com.ziheng.system.sysoperationlog.aspect;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziheng.common.core.domain.R;
import com.ziheng.common.core.enums.ExecuteStatus;
import com.ziheng.common.core.enums.ResultCode;
import com.ziheng.common.util.ServletUtils;
import com.ziheng.system.security.util.LoginUserUtils;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.domain.SysOperationLog;
import com.ziheng.system.sysoperationlog.mask.OperateLogDataMasker;
import com.ziheng.system.sysoperationlog.resolver.OperateLogIdResolver;
import com.ziheng.system.sysoperationlog.service.OperateLogDataService;
import com.ziheng.system.sysoperationlog.service.SysOperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Component
public class OperateLogAspect {
    private static final Logger log = LoggerFactory.getLogger(OperateLogAspect.class);
    private final OperateLogIdResolver idResolver;
    private final OperateLogDataService dataService;
    private final ObjectMapper objectMapper;
    private final SysOperationLogService sysOperationLogService;
    private final OperateLogDataMasker operateLogDataMasker;


    public OperateLogAspect(OperateLogIdResolver idResolver, OperateLogDataService dataService, ObjectMapper objectMapper,
                            SysOperationLogService sysOperationLogService, OperateLogDataMasker operateLogDataMasker) {
        this.idResolver = idResolver;
        this.dataService = dataService;
        this.objectMapper = objectMapper;
        this.sysOperationLogService = sysOperationLogService;
        this.operateLogDataMasker = operateLogDataMasker;
    }

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperateLog operateLog) throws Throwable {
        // 执行前
        // 获取请求对象
        HttpServletRequest request = ServletUtils.getRequest();
        // 获取业务ID
        Object businessId = idResolver.resolve(request, joinPoint, operateLog);
        log.error("****************************************************************************");
        log.info("业务businessId: {}, type: {}", businessId, businessId == null ? null : businessId.getClass().getName());
        log.error("****************************************************************************");

        SysOperationLog entity = new SysOperationLog();
        long start = System.currentTimeMillis();
        // 先采集请求侧信息，保证失败场景也有据可查
        fillRequestInfo(entity, joinPoint.getArgs());
        fillOperationInfo(entity, operateLog);
        entity.setUserId(LoginUserUtils.getUserId());
        entity.setUsername(LoginUserUtils.getUsername());
        entity.setTargetId(businessId == null ? null : businessId.toString());

        // 操作前
        Object beforeData = null;
        if (operateLog.recordBefore() && businessId != null) {
            beforeData = dataService.getByIds(operateLog.service(), businessId);
        }
        entity.setBeforeData(toJson(operateLogDataMasker.mask(beforeData)));
        log.info("beforeData: {}", beforeData);
        try {
            // 执行方法
            Object result = joinPoint.proceed();
            // 新增：执行后重新获取业务ID
            if (businessId == null && operateLog.recordAfter()) {
                businessId = resolveAfterBusinessId(joinPoint, result, operateLog);
                entity.setTargetId(businessId == null ? null : businessId.toString());
                log.info("after businessId: {}", businessId);
            }
            String failMessage = null;
            if (result instanceof R<?> r && r.getCode() != ResultCode.SUCCESS.getCode()) {
                failMessage = r.getMessage();
            }
            entity.setStatus(failMessage == null ? ExecuteStatus.SUCCESS.getCode() : ExecuteStatus.FAIL.getCode());
            entity.setErrorMessage(failMessage);
            return result;
        } catch (Throwable e) {
            entity.setStatus(ExecuteStatus.FAIL.getCode());
            entity.setErrorMessage(StrUtil.blankToDefault(e.getMessage(), e.getClass().getSimpleName()));
            throw e;
        } finally {
            entity.setCostTime(System.currentTimeMillis() - start);
            log.info("操作日志记录完毕，耗时：{} ms", entity.getCostTime());
            // 操作后
            Object afterData = null;
            if (operateLog.recordAfter() && businessId != null) {
                afterData = dataService.getByIds(operateLog.service(), businessId);
            }
            entity.setAfterData(toJson(operateLogDataMasker.mask(afterData)));
            log.info("afterData: {}", afterData);
            saveQuietly(entity);
        }


    }
    private Object resolveAfterBusinessId(ProceedingJoinPoint joinPoint, Object result, OperateLog operateLog) {
        // 新增成功后，优先从返回值获取
        if (result instanceof R<?> r) {
            return r.getData();
        }
        return null;
    }

    /**
     * 将对象转换为 JSON 字符串
     * @param data
     * @return
     */
    private String toJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("操作日志数据序列化失败", e);
            return null;
        }
    }
    /**
     * 填充注解上的业务模块与操作类型。
     */
    private void fillOperationInfo(SysOperationLog entity, OperateLog annotation) {
        if (annotation == null) {
            return;
        }
        entity.setModule(annotation.module().getCode());
        entity.setOperation(annotation.type().getCode());
    }

    /**
     * 采集请求方法、URL、IP、UA 与请求参数。
     */
    private void fillRequestInfo(SysOperationLog entity, Object[] args) {
        // 请求参数
        if (args != null && args.length > 0) {
            entity.setRequestParams(toJson(operateLogDataMasker.mask(args)));
        }

        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            // 非 Web 线程调用（定时任务、单元测试）没有请求上下文，只留参数
            return;
        }
        entity.setRequestMethod(request.getMethod()); // 请求方法
        entity.setRequestUrl(request.getRequestURI()); // 请求URL
        entity.setRequestIp(StrUtil.blankToDefault(ServletUtils.getClientIp(request), "")); // 请求IP
        entity.setRequestLocation(ServletUtils.getIpLocation(entity.getRequestIp())); // 请求位置
        entity.setUserAgent(ServletUtils.getUserAgent(request)); // 用户代理
    }


    /**
     * 写库并兜住异常：操作日志属于旁路信息，写失败不能影响业务主流程。
     */
    private void saveQuietly(SysOperationLog entity) {
        try {
            sysOperationLogService.save(entity);
        } catch (Exception e) {
            log.warn("操作日志写入失败，module={}，operation={}，原因={}",
                    entity.getModule(), entity.getOperation(), e.getMessage());
        }
    }

}
