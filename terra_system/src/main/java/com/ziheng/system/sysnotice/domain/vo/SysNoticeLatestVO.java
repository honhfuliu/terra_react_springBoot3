package com.ziheng.system.sysnotice.domain.vo;

import com.ziheng.system.sysnotice.domain.SysNotice;
import lombok.Data;

import java.util.Date;

/**
 * 最新公告（前端 Header 展示用，置顶优先，取最近 5 条）
 */
@Data
public class SysNoticeLatestVO {

    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告类型：NOTICE通知、ANNOUNCEMENT公告
     */
    private String noticeType;

    /**
     * 公告摘要
     */
    private String summary;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否置顶：0否、1是
     */
    private String isTop;

    /**
     * 将公告实体转换为最新公告 VO
     */
    public static SysNoticeLatestVO from(SysNotice notice) {
        SysNoticeLatestVO vo = new SysNoticeLatestVO();
        vo.setId(notice.getId());
        vo.setNoticeType(notice.getNoticeType());
        vo.setSummary(notice.getSummary());
        vo.setCreateTime(notice.getCreateTime());
        vo.setIsTop(notice.getIsTop());
        return vo;
    }
}
