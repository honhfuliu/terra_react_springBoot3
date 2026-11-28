package com.ziheng.ai.infrastructure.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledge;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeQuery;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeSaveDTO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDetailVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeListVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeOptionVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeStatsVO;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeService;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeMapper;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.ai.infrastructure.provider.service.AiProviderService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【ai_knowledge(AI知识库表)】的数据库操作Service实现
* @createDate 2026-11-14 21:07:01
*/
@Service
public class AiKnowledgeServiceImpl extends ServiceImpl<AiKnowledgeMapper, AiKnowledge>
    implements AiKnowledgeService {

    /**
     * 默认切片长度
     */
    private static final int DEFAULT_CHUNK_SIZE = 500;

    /**
     * 默认切片重叠长度
     */
    private static final int DEFAULT_CHUNK_OVERLAP = 0;

    /**
     * 默认状态（1启用）
     */
    private static final String DEFAULT_STATUS = "1";

    private final AiModelService aiModelService;
    private final AiProviderService aiProviderService;

    public AiKnowledgeServiceImpl(AiModelService aiModelService, AiProviderService aiProviderService) {
        this.aiModelService = aiModelService;
        this.aiProviderService = aiProviderService;
    }

    /**
     * 统计知识库、文档、Chunk总数（文档数和切片数取自 ai_knowledge 表的冗余计数列）
     */
    @Override
    public AiKnowledgeStatsVO stats() {
        AiKnowledgeStatsVO stats = new AiKnowledgeStatsVO();
        stats.setKnowledgeCount(count());
        stats.setDocumentCount(baseMapper.sumDocumentCount());
        stats.setChunkCount(baseMapper.sumChunkCount());
        return stats;
    }

    /**
     * 查询所有启用知识库的选项（供前端选择框使用）
     */
    @Override
    public List<AiKnowledgeOptionVO> listKnowledgeOptions() {
        List<AiKnowledge> knowledgeList = lambdaQuery()
                .select(AiKnowledge::getId, AiKnowledge::getName,
                        AiKnowledge::getDocumentCount, AiKnowledge::getChunkCount,
                        AiKnowledge::getEmbeddingModelId, AiKnowledge::getStatus)
                .eq(AiKnowledge::getStatus, DEFAULT_STATUS)
                .orderByAsc(AiKnowledge::getId)
                .list();
        Map<Long, String> modelCodeMap = queryEmbeddingModelCodes(knowledgeList);

        List<AiKnowledgeOptionVO> result = new ArrayList<>(knowledgeList.size());
        for (AiKnowledge knowledge : knowledgeList) {
            AiKnowledgeOptionVO vo = new AiKnowledgeOptionVO();
            vo.setId(knowledge.getId());
            vo.setName(knowledge.getName());
            vo.setDocumentCount(knowledge.getDocumentCount());
            vo.setChunkCount(knowledge.getChunkCount());
            vo.setEmbeddingModelCode(knowledge.getEmbeddingModelId() == null
                    ? null : modelCodeMap.get(knowledge.getEmbeddingModelId()));
            vo.setStatus(knowledge.getStatus());
            result.add(vo);
        }
        return result;
    }

    /**
     * 批量查询 Embedding 模型编码（key：模型ID）
     */
    private Map<Long, String> queryEmbeddingModelCodes(List<AiKnowledge> knowledgeList) {
        List<Long> modelIds = new ArrayList<>();
        for (AiKnowledge knowledge : knowledgeList) {
            Long modelId = knowledge.getEmbeddingModelId();
            if (modelId != null && !modelIds.contains(modelId)) {
                modelIds.add(modelId);
            }
        }
        if (modelIds.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, String> result = new HashMap<>(modelIds.size());
        for (AiModel model : aiModelService.listByIds(modelIds)) {
            result.put(model.getId(), model.getModelCode());
        }
        return result;
    }

    /**
     * 删除知识库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("知识库不存在");
        }
        removeById(id);
    }

    /**
     * 根据ID查询知识库详情
     */
    @Override
    public AiKnowledgeDetailVO detail(Long id) {
        AiKnowledge knowledge = getById(id);
        if (knowledge == null) {
            throw new BusinessException("知识库不存在");
        }
        AiKnowledgeDetailVO vo = new AiKnowledgeDetailVO();
        BeanUtils.copyProperties(knowledge, vo);
        if (knowledge.getEmbeddingModelId() != null) {
            AiModel model = aiModelService.getById(knowledge.getEmbeddingModelId());
            if (model != null) {
                if (!StringUtils.hasText(vo.getEmbeddingModelName())) {
                    vo.setEmbeddingModelName(model.getModelName());
                }
                if (model.getProviderId() != null) {
                    AiProvider provider = aiProviderService.getById(model.getProviderId());
                    if (provider != null) {
                        vo.setProviderName(provider.getProviderName());
                    }
                }
            }
        }
        return vo;
    }

    /**
     * 分页查询知识库列表（keyword 模糊匹配名称或描述）
     */
    @Override
    public PageResult<AiKnowledgeListVO> page(AiKnowledgeQuery query) {
        Page<AiKnowledge> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiKnowledge> wrapper = new LambdaQueryWrapper<AiKnowledge>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(AiKnowledge::getName, query.getKeyword())
                        .or()
                        .like(AiKnowledge::getDescription, query.getKeyword()))
                .orderByDesc(AiKnowledge::getCreateTime)
                .orderByDesc(AiKnowledge::getId);

        Page<AiKnowledge> result = page(page, wrapper);
        List<AiKnowledge> records = result.getRecords();
        Map<Long, String> providerNameMap = queryProviderNames(records);
        List<AiKnowledgeListVO> rows = new ArrayList<>(records.size());
        for (AiKnowledge item : records) {
            AiKnowledgeListVO vo = new AiKnowledgeListVO();
            BeanUtils.copyProperties(item, vo);
            vo.setProviderName(providerNameMap.get(item.getEmbeddingModelId()));
            rows.add(vo);
        }
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 批量查询Embedding模型所属提供商名称（key：模型ID）
     */
    private Map<Long, String> queryProviderNames(List<AiKnowledge> records) {
        List<Long> modelIds = new ArrayList<>();
        for (AiKnowledge item : records) {
            if (item.getEmbeddingModelId() != null && !modelIds.contains(item.getEmbeddingModelId())) {
                modelIds.add(item.getEmbeddingModelId());
            }
        }
        if (modelIds.isEmpty()) {
            return new HashMap<>();
        }
        List<AiModel> models = aiModelService.listByIds(modelIds);
        List<Long> providerIds = new ArrayList<>();
        Map<Long, Long> modelProviderMap = new HashMap<>(models.size());
        for (AiModel model : models) {
            modelProviderMap.put(model.getId(), model.getProviderId());
            if (model.getProviderId() != null && !providerIds.contains(model.getProviderId())) {
                providerIds.add(model.getProviderId());
            }
        }
        Map<Long, String> providerNameMap = new HashMap<>();
        if (!providerIds.isEmpty()) {
            List<AiProvider> providers = aiProviderService.listByIds(providerIds);
            for (AiProvider provider : providers) {
                providerNameMap.put(provider.getId(), provider.getProviderName());
            }
        }
        Map<Long, String> result = new HashMap<>(modelIds.size());
        for (Map.Entry<Long, Long> entry : modelProviderMap.entrySet()) {
            result.put(entry.getKey(), providerNameMap.get(entry.getValue()));
        }
        return result;
    }

    /**
     * 新增或修改知识库（id为空则新增，不为空则修改）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveKnowledge(AiKnowledgeSaveDTO dto) {
        checkNameUnique(dto.getName(), dto.getId());
        AiModel model = aiModelService.getById(dto.getEmbeddingModelId());
        if (model == null) {
            throw new BusinessException("Embedding模型不存在");
        }
        if (dto.getId() == null) {
            return insertKnowledge(dto, model);
        }
        updateKnowledge(dto, model);
        return dto.getId();
    }

    /**
     * 新增知识库
     */
    private Long insertKnowledge(AiKnowledgeSaveDTO dto, AiModel model) {
        int chunkSize = dto.getChunkSize() == null ? DEFAULT_CHUNK_SIZE : dto.getChunkSize();
        int chunkOverlap = dto.getChunkOverlap() == null ? DEFAULT_CHUNK_OVERLAP : dto.getChunkOverlap();
        checkChunkConfig(chunkSize, chunkOverlap);

        AiKnowledge knowledge = new AiKnowledge();
        BeanUtils.copyProperties(dto, knowledge);
        if (!StringUtils.hasText(knowledge.getEmbeddingModelName())) {
            knowledge.setEmbeddingModelName(model.getModelName());
        }
        if (!StringUtils.hasText(knowledge.getStatus())) {
            knowledge.setStatus(DEFAULT_STATUS);
        }
        knowledge.setChunkSize(chunkSize);
        knowledge.setChunkOverlap(chunkOverlap);
        Date now = new Date();
        knowledge.setCreateTime(now);
        knowledge.setUpdateTime(now);
        save(knowledge);
        return knowledge.getId();
    }

    /**
     * 修改知识库
     */
    private void updateKnowledge(AiKnowledgeSaveDTO dto, AiModel model) {
        AiKnowledge old = getById(dto.getId());
        if (old == null) {
            throw new BusinessException("知识库不存在");
        }
        int chunkSize = dto.getChunkSize() != null ? dto.getChunkSize()
                : (old.getChunkSize() == null ? DEFAULT_CHUNK_SIZE : old.getChunkSize());
        int chunkOverlap = dto.getChunkOverlap() != null ? dto.getChunkOverlap()
                : (old.getChunkOverlap() == null ? DEFAULT_CHUNK_OVERLAP : old.getChunkOverlap());
        checkChunkConfig(chunkSize, chunkOverlap);

        AiKnowledge knowledge = new AiKnowledge();
        BeanUtils.copyProperties(dto, knowledge);
        // 未传模型名称时按模型ID补全快照
        if (!StringUtils.hasText(knowledge.getEmbeddingModelName())) {
            knowledge.setEmbeddingModelName(model.getModelName());
        }
        // 未传状态时保留原值（updateById 忽略 null 字段）
        if (!StringUtils.hasText(knowledge.getStatus())) {
            knowledge.setStatus(old.getStatus());
        }
        knowledge.setUpdateTime(new Date());
        updateById(knowledge);
    }

    /**
     * 校验知识库名称唯一
     */
    private void checkNameUnique(String name, Long excludeId) {
        Long count = lambdaQuery()
                .eq(AiKnowledge::getName, name)
                .ne(excludeId != null, AiKnowledge::getId, excludeId)
                .count();
        if (count != null && count > 0) {
            throw new BusinessException("知识库名称已存在");
        }
    }

    /**
     * 校验切片配置：重叠长度必须小于切片长度
     */
    private void checkChunkConfig(int chunkSize, int chunkOverlap) {
        if (chunkOverlap >= chunkSize) {
            throw new BusinessException("切片重叠长度必须小于切片长度");
        }
    }
}
