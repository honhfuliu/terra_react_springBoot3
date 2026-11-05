package com.ziheng.system.sysnotice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.system.sysnotice.domain.SysNotice;
import com.ziheng.system.sysnotice.domain.dto.SysNoticeAddDTO;
import com.ziheng.system.sysnotice.domain.dto.SysNoticeQuery;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeDetailVo;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeVo;
import com.ziheng.system.sysnotice.mapper.SysNoticeMapper;
import com.ziheng.system.sysnotice.service.SysNoticeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* @author Administrator
* @description 针对表【sys_notice(系统公告表)】的数据库操作Service实现
* @createDate 2026-09-09 21:43:47
*/
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice>
    implements SysNoticeService {

    /** 公告状态：1 待发布（新增时的默认状态，也是唯一允许编辑的状态） */
    private static final String STATUS_PENDING = "1";

    /** 公告状态：2 已发布 */
    private static final String STATUS_PUBLISHED = "2";

    /** 公告状态：3 已下线 */
    private static final String STATUS_OFFLINE = "3";

    /** 是否置顶：1 是、0 否 */
    private static final String TOP_YES = "1";
    private static final String TOP_NO = "0";

    /**
     * 新增或修改公告（id 为空则新增，不为空则修改）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addNotice(SysNoticeAddDTO dto) {
        if (dto.getId() == null) {
            return insertNotice(dto);
        }
        updateNotice(dto);
        return dto.getId();
    }

    /**
     * 新增公告：状态固定为「待发布」，后续发布/下线由状态流转接口负责
     */
    private Long insertNotice(SysNoticeAddDTO dto) {
        SysNotice notice = new SysNotice();
        BeanUtils.copyProperties(dto, notice);
        notice.setStatus(STATUS_PENDING);
        if (notice.getSort() == null) {
            notice.setSort(0);
        }
        notice.setViewCount(0L);
        Date now = new Date();
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        notice.setCreateBy(StpUtil.getLoginIdAsLong());
        save(notice);
        return notice.getId();
    }

    /**
     * 修改公告：只有「待发布」状态的公告才能编辑，已发布/已下线的不允许再改
     * （状态流转请走发布、下线接口）
     *
     * <p>表单没传的字段（状态、发布时间、创建人等）保持原值不动：
     * MyBatis-Plus updateById 默认忽略 null 字段，DTO 里没有的字段取出来就是 null。</p>
     */
    private void updateNotice(SysNoticeAddDTO dto) {
        SysNotice old = getById(dto.getId());
        if (old == null) {
            throw new BusinessException("公告不存在");
        }
        if (!STATUS_PENDING.equals(old.getStatus())) {
            throw new BusinessException("公告不是待发布状态，不允许修改");
        }
        SysNotice notice = new SysNotice();
        BeanUtils.copyProperties(dto, notice);
        notice.setUpdateTime(new Date());
        notice.setUpdateBy(StpUtil.getLoginIdAsLong());
        updateById(notice);
    }

    /**
     * 分页查询公告列表
     * 排序：置顶优先，再按排序值升序，最后用主键倒序兜底保证翻页顺序稳定
     */
    @Override
    public PageResult<SysNoticeVo> listNotices(SysNoticeQuery query) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .like(StringUtils.hasText(query.getTitle()), SysNotice::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getNoticeType()), SysNotice::getNoticeType, query.getNoticeType())
                .eq(StringUtils.hasText(query.getStatus()), SysNotice::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getStartTime()), SysNotice::getPublishTime, query.getStartTime())
                .le(StringUtils.hasText(query.getEndTime()), SysNotice::getPublishTime, resolveEnd(query.getEndTime()))
                .orderByDesc(SysNotice::getIsTop)
                .orderByAsc(SysNotice::getSort)
                .orderByDesc(SysNotice::getId);

        Page<SysNotice> result = page(Page.of(query.getPageNum(), query.getPageSize()), wrapper);
        List<SysNoticeVo> rows = result.getRecords().stream()
                .map(SysNoticeVo::from)
                .toList();
        return PageResult.of(result.getTotal(), rows);
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
     * 查询公告详情（编辑回显）
     */
    @Override
    public SysNoticeDetailVo getNoticeDetail(Long id) {
        SysNotice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return SysNoticeDetailVo.from(notice);
    }

    /**
     * 删除公告（物理删除，表中无逻辑删除字段）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("公告不存在");
        }
        removeById(id);
    }

    /**
     * 置顶公告：同一时间只允许一条置顶
     * 先批量清掉原置顶（is_top = 1 → 0），再把当前公告置顶
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topNotice(Long id) {
        SysNotice old = getById(id);
        if (old == null) {
            throw new BusinessException("公告不存在");
        }
        if (TOP_YES.equals(old.getIsTop())) {
            // 已经是置顶，幂等返回，避免无意义的更新
            return;
        }
        update(new LambdaUpdateWrapper<SysNotice>()
                .set(SysNotice::getIsTop, TOP_NO)
                .eq(SysNotice::getIsTop, TOP_YES));

        SysNotice notice = new SysNotice();
        notice.setId(id);
        notice.setIsTop(TOP_YES);
        notice.setUpdateTime(new Date());
        notice.setUpdateBy(StpUtil.getLoginIdAsLong());
        updateById(notice);
    }

    /**
     * 批量发布公告：状态置为「已发布」，同时记录发布时间
     *
     * <p>只有「待发布」的公告可以发布，只要有一条不满足就整批回滚，
     * 避免前端选择了混合状态的数据时出现"部分成功"的模糊结果。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNotices(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要发布的公告");
        }
        Set<Long> idSet = new HashSet<>(ids);
        List<SysNotice> notices = list(new LambdaQueryWrapper<SysNotice>().in(SysNotice::getId, idSet));
        if (notices.size() != idSet.size()) {
            throw new BusinessException("部分公告不存在，请刷新列表后重试");
        }
        for (SysNotice notice : notices) {
            if (!STATUS_PENDING.equals(notice.getStatus())) {
                throw new BusinessException("公告「" + notice.getTitle() + "」不是待发布状态，不能发布");
            }
        }

        Date now = new Date();
        update(new LambdaUpdateWrapper<SysNotice>()
                .set(SysNotice::getStatus, STATUS_PUBLISHED)
                .set(SysNotice::getPublishTime, now)
                .set(SysNotice::getUpdateTime, now)
                .set(SysNotice::getUpdateBy, StpUtil.getLoginIdAsLong())
                .in(SysNotice::getId, idSet));
    }

    /**
     * 下架公告：只有「已发布」的公告才能下架，下架时把失效时间记为当前时间
     * （publishTime 保留不动，便于追溯历史发布时间）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineNotice(Long id) {
        SysNotice old = getById(id);
        if (old == null) {
            throw new BusinessException("公告不存在");
        }
        if (!STATUS_PUBLISHED.equals(old.getStatus())) {
            throw new BusinessException("公告不是已发布状态，不能下架");
        }
        Date now = new Date();
        SysNotice notice = new SysNotice();
        notice.setId(id);
        notice.setStatus(STATUS_OFFLINE);
        notice.setExpireTime(now);
        notice.setUpdateTime(now);
        notice.setUpdateBy(StpUtil.getLoginIdAsLong());
        updateById(notice);
    }
}




