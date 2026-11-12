package com.ziheng.ai.capability.chat.service;

import com.ziheng.ai.capability.chat.domain.dto.AiChatSessionQuery;
import com.ziheng.ai.capability.chat.domain.dto.AiChatSessionRenameDTO;
import com.ziheng.ai.capability.chat.domain.entity.AiChatSession;
import com.ziheng.ai.capability.chat.domain.vo.AiChatSessionVO;
import com.ziheng.common.core.domain.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【ai_chat_session(AI聊天会话表)】的数据库操作Service
* @createDate 2026-11-09 21:46:26
*/
public interface AiChatSessionService extends IService<AiChatSession> {

    /**
     * 获取或创建会话
     * @param sessionId 会话ID，如果为空则创建新会话
     * @param userId 用户ID
     * @param message 消息内容
     * @return 会话信息
     */
    AiChatSession getOrCreateSession(Long sessionId, Long userId, String message);

    /**
     * 更新会话时间
     * @param session 会话信息
     */
    void updateSessionTime(AiChatSession session);

    /**
     * 分页查询当前用户的会话（按更新时间倒序）
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<AiChatSessionVO> pageSessions(AiChatSessionQuery query);

    /**
     * 修改会话名称（仅本人会话可修改）
     *
     * @param dto 会话ID与新标题
     */
    void renameSession(AiChatSessionRenameDTO dto);

    /**
     * 删除会话（仅本人会话可删除，同时物理删除该会话下所有聊天消息）
     *
     * @param sessionId 会话ID
     */
    void deleteSession(Long sessionId);
}
