package com.ziheng.system.sysnotice.domain.vo;

import com.ziheng.system.sysnotice.domain.SysNotice;
import lombok.Data;

import java.util.Date;

/**
 * 公告列表行
 */
@Data
public class SysNoticeVo {

    /**
     * 主键（前端表格 row-key，操作列用）
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
     * 状态：1待发布、2已发布、3已下线
     */
    private String status;

    /**
     * 发布时间（yyyy-MM-dd HH:mm:ss）
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    /**
     * 失效时间（yyyy-MM-dd HH:mm:ss）
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 浏览次数
     */
    private Long viewCount;

    /**
     * 是否置顶：0否、1是
     */
    private String isTop;

    public static SysNoticeVo from(SysNotice notice) {
        SysNoticeVo vo = new SysNoticeVo();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setNoticeType(notice.getNoticeType());
        vo.setStatus(notice.getStatus());
        vo.setPublishTime(notice.getPublishTime());
        vo.setExpireTime(notice.getExpireTime());
        vo.setViewCount(notice.getViewCount());
        vo.setIsTop(notice.getIsTop());
        return vo;
    }
}
