package com.ziheng.ai.infrastructure.knowledge.service;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledge;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeQuery;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeSaveDTO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDetailVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeListVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeStatsVO;
import com.ziheng.common.core.domain.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【ai_knowledge(AI知识库表)】的数据库操作Service
* @createDate 2026-11-14 21:07:01
*/
public interface AiKnowledgeService extends IService<AiKnowledge> {

    /**
     * 保存知识库（id为空时新增，id不为空时修改）
     *
     * @param dto 保存参数
     * @return 主键
     */
    Long saveKnowledge(AiKnowledgeSaveDTO dto);

    /**
     * 分页查询知识库列表
     *
     * @param query 查询参数（keyword 模糊匹配名称或描述）
     * @return 分页结果
     */
    PageResult<AiKnowledgeListVO> page(AiKnowledgeQuery query);

    /**
     * 统计知识库、文档、Chunk总数
     *
     * @return 统计结果
     */
    AiKnowledgeStatsVO stats();

    /**
     * 删除知识库
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 根据ID查询知识库详情
     *
     * @param id 主键
     * @return 详情
     */
    AiKnowledgeDetailVO detail(Long id);

}
