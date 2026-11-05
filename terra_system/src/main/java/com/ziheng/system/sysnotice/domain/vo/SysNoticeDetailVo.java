package com.ziheng.system.sysnotice.domain.vo;

import com.ziheng.system.sysnotice.domain.SysNotice;
import lombok.Data;

import java.util.Date;

/**
 * 公告详情 VO（编辑回显用）
 */
@Data
public class SysNoticeDetailVo {

    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告类型：NOTICE通知、ANNOUNCEMENT公告
     */
    private String noticeType;

    /**
     * 公告摘要
     */
    private String summary;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 发布时间（yyyy-MM-dd HH:mm:ss，未发布时为 null，前端展示 "-"）
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    /**
     * 失效时间（yyyy-MM-dd HH:mm:ss，未设置时为 null，前端展示 "-"）
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 浏览次数
     */
    private Long viewCount;

    public static SysNoticeDetailVo from(SysNotice notice) {
        SysNoticeDetailVo vo = new SysNoticeDetailVo();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setNoticeType(notice.getNoticeType());
        vo.setSummary(notice.getSummary());
        vo.setContent(notice.getContent());
        vo.setPublishTime(notice.getPublishTime());
        vo.setExpireTime(notice.getExpireTime());
        vo.setViewCount(notice.getViewCount());
        return vo;
    }
}
