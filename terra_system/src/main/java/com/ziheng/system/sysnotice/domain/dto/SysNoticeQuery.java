package com.ziheng.system.sysnotice.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeQuery extends PageQuery {

    /**
     * 公告标题（模糊查询）
     */
    private String title;

    /**
     * 公告类型：NOTICE通知、ANNOUNCEMENT公告
     */
    private String noticeType;

    /**
     * 状态：1待发布、2已发布、3已下线
     */
    private String status;

    /**
     * 发布时间-开始
     */
    private String startTime;

    /**
     * 发布时间-结束
     */
    private String endTime;
}
