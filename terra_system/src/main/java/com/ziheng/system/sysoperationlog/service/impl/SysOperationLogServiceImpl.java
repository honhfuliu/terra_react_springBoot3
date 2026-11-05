package com.ziheng.system.sysoperationlog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.util.excel.ExcelExportUtils;
import com.ziheng.system.sysoperationlog.domain.SysOperationLog;
import com.ziheng.system.sysoperationlog.domain.dto.SysOperationLogQuery;
import com.ziheng.system.sysoperationlog.domain.vo.SysOperationLogExportVO;
import com.ziheng.system.sysoperationlog.domain.vo.SysOperationLogVo;
import com.ziheng.system.sysoperationlog.mapper.SysOperationLogMapper;
import com.ziheng.system.sysoperationlog.service.SysOperationLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_operation_log(系统操作审计日志)】的数据库操作Service实现
* @createDate 2026-09-09 21:40:43
*/
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
    implements SysOperationLogService{


    /**
     * 分页查询操作日志
     */
    @Override
    public PageResult<SysOperationLogVo> listOperationLogs(SysOperationLogQuery query) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .like(StringUtils.hasText(query.getUsername()), SysOperationLog::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getModule()), SysOperationLog::getModule, query.getModule())
                .eq(StringUtils.hasText(query.getOperation()), SysOperationLog::getOperation, query.getOperation())
                .eq(StringUtils.hasText(query.getStatus()), SysOperationLog::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getRequestIp()), SysOperationLog::getRequestIp, query.getRequestIp())
                .ge(StringUtils.hasText(query.getStartTime()), SysOperationLog::getCreateTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysOperationLog::getCreateTime, resolveEnd(query.getEndTime()))
                // 日志按时间倒序，同一毫秒（大批量导入时会出现）再用主键兜底，保证翻页顺序稳定
                .orderByDesc(SysOperationLog::getCreateTime)
                .orderByDesc(SysOperationLog::getId);

            Page<SysOperationLog> result = page(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        List<SysOperationLogVo> rows = result.getRecords().stream()
                .map(SysOperationLogVo::from)
                .toList();
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 清空所有操作日志。
     *
     * <p>用 DELETE 而不是 TRUNCATE：TRUNCATE 是 DDL，会隐式提交、不受事务保护，
     * 还会把自增 ID 归零，日志清理这种"出问题要能回滚"的场景不合适；
     * 代价是数据量大时比 TRUNCATE 慢，真到了千万级再单独做归档清理。</p>
     *
     * <p>注意：这里没有加 BlockAttackInnerInterceptor 之类的全表删除防护，
     * 接口靠 {@code system:operationLog:clear} 权限控制，菜单里别配给普通角色。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int clearOperationLogs() {
        return baseMapper.delete(new LambdaQueryWrapper<>());
    }

    /**
     * 批量删除操作日志。
     *
     * <p>ID 列表直接进 SQL 的 IN 条件，所以要做两道校验：
     * 空列表直接拒绝（否则 MyBatis-Plus 会拼出 {@code IN ()} 这种语法错误的 SQL）；
     * 条数设上限，避免一次传几万个 ID 把 SQL 撑爆。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOperationLogs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的日志");
        }
        return baseMapper.deleteByIds(ids);
    }

    /**
     * 导出操作日志（Excel）
     * @param query 查询条件
     * @param response 响应输出流
     */
    @Override
    public void export(SysOperationLogQuery query, HttpServletResponse response) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .select(
                        SysOperationLog::getId, SysOperationLog::getUsername,
                        SysOperationLog::getModule, SysOperationLog::getOperation,
                        SysOperationLog::getTargetType, SysOperationLog::getTargetId,
                        SysOperationLog::getTargetName, SysOperationLog::getRequestMethod,
                        SysOperationLog::getRequestUrl, SysOperationLog::getRequestIp,
                        SysOperationLog::getStatus, SysOperationLog::getCostTime, SysOperationLog::getCreateTime
                )
                .like(StringUtils.hasText(query.getUsername()), SysOperationLog::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getModule()), SysOperationLog::getModule, query.getModule())
                .eq(StringUtils.hasText(query.getOperation()), SysOperationLog::getOperation, query.getOperation())
                .eq(StringUtils.hasText(query.getStatus()), SysOperationLog::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getRequestIp()), SysOperationLog::getRequestIp, query.getRequestIp())
                .ge(StringUtils.hasText(query.getStartTime()), SysOperationLog::getCreateTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysOperationLog::getCreateTime, resolveEnd(query.getEndTime()))
                .orderByDesc(SysOperationLog::getCreateTime)
                .orderByDesc(SysOperationLog::getId);
        List<SysOperationLogExportVO> voList = SysOperationLogExportVO.fromList(list(wrapper));
        try {
            ExcelExportUtils.export(voList, SysOperationLogExportVO.class, "操作日志", response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("导出失败");
        }
    }

    /**
     * 解析结束时间：仅日期(yyyy-MM-dd)时补 23:59:59，
     * 否则当天 00:00:00 之后的数据全都查不到
     */
    private String resolveEnd(String endTime) {
        if (endTime == null) {
            return null;
        }
        if (endTime.length() == 10) {
            return endTime + " 23:59:59";
        }
        return endTime;
    }
}
