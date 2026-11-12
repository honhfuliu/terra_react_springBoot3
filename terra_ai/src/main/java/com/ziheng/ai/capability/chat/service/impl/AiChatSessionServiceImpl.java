package com.ziheng.ai.capability.chat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.capability.chat.domain.dto.AiChatSessionQuery;
import com.ziheng.ai.capability.chat.domain.dto.AiChatSessionRenameDTO;
import com.ziheng.ai.capability.chat.domain.entity.AiChatMessage;
import com.ziheng.ai.capability.chat.domain.entity.AiChatSession;
import com.ziheng.ai.capability.chat.domain.vo.AiChatSessionVO;
import com.ziheng.ai.capability.chat.mapper.AiChatMessageMapper;
import com.ziheng.ai.capability.chat.mapper.AiChatSessionMapper;
import com.ziheng.ai.capability.chat.service.AiChatSessionService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_chat_session(AI聊天会话表)】的数据库操作Service实现
* @createDate 2026-11-09 21:46:26
*/
@Service
public class AiChatSessionServiceImpl extends ServiceImpl<AiChatSessionMapper, AiChatSession>
    implements AiChatSessionService {

    private final AiChatMessageMapper aiChatMessageMapper;

    public AiChatSessionServiceImpl(AiChatMessageMapper aiChatMessageMapper) {
        this.aiChatMessageMapper = aiChatMessageMapper;
    }

    @Override
    public AiChatSession getOrCreateSession(Long sessionId, Long userId, String message) {
        // 第一次发送消息，创建新会话
        if (sessionId == null) {
            AiChatSession session = new AiChatSession();
            session.setUserId(userId);
            session.setTitle(generateTitle(message));
            baseMapper.insert(session);
            // insert 后，MyBatis-Plus 会将自增主键回填到实体
            return session;
        }
        // 已有会话，根据 ID 查询
        AiChatSession session = baseMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("聊天会话不存在");
        }
        // 校验会话归属
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException("无权访问该聊天会话");
        }
        // 更新会话时间
        session.setUpdateTime(new Date());
        baseMapper.updateById(session);
        return session;
    }

    /**
     * 更新会话时间
     * @param session 会话信息
     */
    @Override
    public void updateSessionTime(AiChatSession session) {
        session.setUpdateTime(new Date());
        baseMapper.updateById(session);
    }

    @Override
    public PageResult<AiChatSessionVO> pageSessions(AiChatSessionQuery query) {
        // 只查询当前登录用户的会话
        Long userId = StpUtil.getLoginIdAsLong();
        Page<AiChatSession> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, userId)
                .like(StringUtils.hasText(query.getTitle()), AiChatSession::getTitle, query.getTitle())
                .orderByDesc(AiChatSession::getUpdateTime)
                .orderByDesc(AiChatSession::getId);

        Page<AiChatSession> result = page(page, wrapper);
        List<AiChatSessionVO> rows = new ArrayList<>(result.getRecords().size());
        for (AiChatSession session : result.getRecords()) {
            AiChatSessionVO vo = new AiChatSessionVO();
            vo.setSessionId(session.getId());
            vo.setTitle(session.getTitle());
            vo.setUpdateTime(session.getUpdateTime());
            rows.add(vo);
        }
        return PageResult.of(result.getTotal(), rows);
    }

    @Override
    public void renameSession(AiChatSessionRenameDTO dto) {
        // 校验是否本人会话
        Long userId = StpUtil.getLoginIdAsLong();
        AiChatSession session = baseMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getId, dto.getSessionId())
                .eq(AiChatSession::getUserId, userId));
        if (session == null) {
            throw new BusinessException("聊天会话不存在");
        }
        // 只更新标题，不修改会话更新时间
        AiChatSession update = new AiChatSession();
        update.setId(dto.getSessionId());
        update.setTitle(dto.getTitle());
        baseMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        // 校验是否本人会话，非本人或不存在都不允许删除
        Long userId = StpUtil.getLoginIdAsLong();
        AiChatSession session = baseMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getId, sessionId)
                .eq(AiChatSession::getUserId, userId));
        if (session == null) {
            throw new BusinessException("聊天会话不存在");
        }
        // 先物理删除该会话下的所有聊天消息
        aiChatMessageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId));
        // 再物理删除会话本身
        baseMapper.deleteById(sessionId);
    }


    /**
     * 根据首条消息生成会话标题
     */
    private String generateTitle(String message) {
        int maxLength = 30;
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
    }
}
