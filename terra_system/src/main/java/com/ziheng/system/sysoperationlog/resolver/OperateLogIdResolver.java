package com.ziheng.system.sysoperationlog.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * 操作日志ID解析器
 */
@Component
public class OperateLogIdResolver {
    private static final Logger log = LoggerFactory.getLogger(OperateLogIdResolver.class);
    private final ObjectMapper objectMapper;

    public OperateLogIdResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析操作日志ID
     * @param request 请求对象
     * @param joinPoint 连接点
     * @param operateLog 操作日志注解
     * @return 解析得到的操作日志ID
     */
    public Object resolve(HttpServletRequest request, ProceedingJoinPoint joinPoint, OperateLog operateLog) {
        String idField = operateLog.idField();
        IdSource idSource = operateLog.idSource();

        if (idSource == IdSource.PATH) {
            return resolvePath(request, idField);
        }
        if (idSource == IdSource.PARAM) {
            return resolveParam(request, joinPoint, idField);
        }
        if (idSource == IdSource.BODY) {
            return resolveBody(joinPoint, idField);
        }
        return resolveAuto(request, joinPoint, idField);
    }

    /**
     * 从路径中解析操作日志ID
     * @param request 请求对象
     * @param idField ID字段
     * @return 解析得到的操作日志ID
     */
    private Object resolvePath(HttpServletRequest request, String idField) {
        if (idField == null || idField.isEmpty()) {
            return null;
        }
        Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attr instanceof Map)) {
            return null;
        }
        Object value = ((Map<?, ?>) attr).get(idField);
        if (value == null) {
            return null;
        }
        log.debug("resolvePath idField: {}, id: {}", idField, value);
        return value;
    }

    /**
     * 从请求参数中解析操作日志ID
     * @param request 请求对象
     * @param idField ID字段
     * @return 解析得到的操作日志ID
     */
    private Object resolveParam(HttpServletRequest request, ProceedingJoinPoint joinPoint, String idField) {
        // 1. 从 Controller 方法参数中，按照参数名寻找
        Object value = resolveParamFromArgs(joinPoint, idField);
        if (value != null) {
            log.debug("resolveParam from args, idField: {}, id: {}", idField, value);
            return value;
        }
        return request.getParameter(idField);
    }

    /**
     * 从方法参数中解析操作日志ID
     * @param joinPoint 连接点
     * @param idField ID字段
     * @return 解析得到的操作日志ID
     */
    private Object resolveParamFromArgs(ProceedingJoinPoint joinPoint, String idField) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            log.info("requestParamKey: {}, requestParamValue: {}", requestParam.name(), requestParam.value());
            if (requestParam != null) {
                String parameterName = requestParam.name();
                if (!StringUtils.hasText(parameterName)) {
                    parameterName = requestParam.value();
                }
                if (idField.equals(parameterName)) {
                    return args[i];
                }
            }
        }

        return null;
    }

    /**
     * 从请求体中解析操作日志ID
     * @param joinPoint 连接点
     * @param idField ID字段
     * @return 解析得到的操作日志ID
     */
    private Object resolveBody(ProceedingJoinPoint joinPoint, String idField) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            JsonNode node = objectMapper.valueToTree(arg);
            JsonNode idNode = node.get(idField);
            if (node.isArray()) {
                log.debug("resolveBody body array, idField: {}, id: {}", idField, node);
                return objectMapper.convertValue(node, List.class);
            }
            if (idNode != null && !idNode.isNull()) {
                log.debug("resolveBody idField: {}, id: {}", idField, idNode.asText());
                // return idNode.asText();
                if (idNode.isNumber()) {
                    return idNode.numberValue();   // 返回 Number（Integer/Long/Double）
                }
                if (idNode.isTextual()) {
                    return idNode.textValue();     // 返回 String
                }

                if (idNode.isArray()) { // 批量ID
                    return objectMapper.convertValue(
                            idNode,
                            List.class
                    );
                }
            }
        }
        return null;
    }
    /**
     * 自动解析操作日志ID
     * @param request 请求对象
     * @param joinPoint 连接点
     * @param idField ID字段
     * @return 解析得到的操作日志ID
     */
    private Object resolveAuto(HttpServletRequest request, ProceedingJoinPoint joinPoint, String idField) {
        Object id = resolvePath(request, idField);// PATH
        if (id != null) return id;

        id = resolveParam(request, joinPoint, idField); // PARAM
        if (id != null) return id;

        return resolveBody(joinPoint, idField);// BODY
    }

}
