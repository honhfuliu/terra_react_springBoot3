package com.ziheng.system.sysloginlog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.enums.ExecuteStatus;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.util.excel.ExcelExportUtils;
import com.ziheng.system.sysloginlog.domain.SysLoginLog;
import com.ziheng.system.sysloginlog.domain.dto.SysLoginLogQuery;
import com.ziheng.system.sysloginlog.domain.vo.SysLoginLogExportVO;
import com.ziheng.system.sysloginlog.domain.vo.SysLoginLogVo;
import com.ziheng.system.sysloginlog.mapper.SysLoginLogMapper;
import com.ziheng.system.sysloginlog.service.SysLoginLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_login_log(登录日志表)】的数据库操作Service实现
* @createDate 2026-09-09 21:33:19
*/
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog>
    implements SysLoginLogService {

    /**
     * 分页查询登录日志
     */
    @Override
    public PageResult<SysLoginLogVo> listLoginLogs(SysLoginLogQuery query) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getStatus()), SysLoginLog::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getStartTime()), SysLoginLog::getLoginTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysLoginLog::getLoginTime, resolveEnd(query.getEndTime()))
                // 日志按时间倒序，同一时间（秒级相同）再用主键兜底，保证翻页顺序稳定
                .orderByDesc(SysLoginLog::getLoginTime)
                .orderByDesc(SysLoginLog::getId);

        Page<SysLoginLog> result = page(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        List<SysLoginLogVo> rows = result.getRecords().stream()
                .map(SysLoginLogVo::from)
                .toList();
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 导出登录日志（Excel）
     * @param query 查询条件
     * @param response 响应输出流
     */
    @Override
    public void export(SysLoginLogQuery query, HttpServletResponse response) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getStatus()), SysLoginLog::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getStartTime()), SysLoginLog::getLoginTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysLoginLog::getLoginTime, resolveEnd(query.getEndTime()))
                .orderByDesc(SysLoginLog::getLoginTime)
                .orderByDesc(SysLoginLog::getId);
        List<SysLoginLog> sysLoginLogs = baseMapper.selectList(wrapper);
        List<SysLoginLogExportVO> vos = SysLoginLogExportVO.fromList(sysLoginLogs);
        try {
            ExcelExportUtils.export(vos, SysLoginLogExportVO.class, "登录日志", response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("导出失败");
        }
    }

    /**
     * 解析结束时间，仅日期(yyyy-MM-dd)时补 23:59:59
     * 否则当天 00:00:00 之后的数据会查不到
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

    /**
     * 查询某用户最近一条成功登录记录。
     *
     * <p>只取状态为成功的（CHAR(1) 的 "1"），按登录时间倒序取最新一条。</p>
     */
    @Override
    public SysLoginLog getLatestSuccess(Long userId) {
        if (userId == null) {
            return null;
        }
        return lambdaQuery()
                .eq(SysLoginLog::getUserId, userId)
                .eq(SysLoginLog::getStatus, ExecuteStatus.SUCCESS.getCode())
                .orderByDesc(SysLoginLog::getLoginTime)
                .last("LIMIT 1")
                .one();
    }
}
