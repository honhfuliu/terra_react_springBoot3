package com.ziheng.system.sysnotice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.system.sysnotice.domain.SysNotice;
import com.ziheng.system.sysnotice.domain.dto.SysNoticeAddDTO;
import com.ziheng.system.sysnotice.domain.dto.SysNoticeQuery;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeDetailVo;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeLatestVO;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeVo;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_notice(系统公告表)】的数据库操作Service
* @createDate 2026-09-09 21:43:47
*/
public interface SysNoticeService extends IService<SysNotice> {

    /**
     * 新增或修改公告（id 为空则新增，不为空则修改）
     *
     * @return 公告ID
     */
    Long addNotice(SysNoticeAddDTO dto);

    /**
     * 分页查询公告列表
     */
    PageResult<SysNoticeVo> listNotices(SysNoticeQuery query);

    /**
     * 查询公告详情（编辑回显）
     */
    SysNoticeDetailVo getNoticeDetail(Long id);

    /**
     * 删除公告
     */
    void deleteNotice(Long id);

    /**
     * 置顶公告（同一时间只允许一条置顶，原置顶会被自动取消）
     */
    void topNotice(Long id);

    /**
     * 批量发布公告（状态置为「已发布」，同时记录发布时间）
     */
    void publishNotices(List<Long> ids);

    /**
     * 下架公告（状态置为「已下线」，同时记录失效时间）
     */
    void offlineNotice(Long id);

    /**
     * 查询最新 5 条公告（置顶优先、按发布时间倒序），用于前端 Header 展示
     *
     * @return 最新公告列表（最多 5 条）
     */
    List<SysNoticeLatestVO> latestNotices();
}
