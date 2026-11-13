package com.ziheng.ai.infrastructure.model.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.infrastructure.client.context.AiModelContext;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.domain.dto.AiModelQuery;
import com.ziheng.ai.infrastructure.model.domain.dto.AiModelSaveDTO;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelOptionVO;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelVO;
import com.ziheng.ai.infrastructure.model.mapper.AiModelMapper;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.ai.infrastructure.provider.service.AiProviderService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.enums.EnableStatusEnum;
import com.ziheng.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【ai_model(AI模型配置表)】的数据库操作Service实现
* @createDate 2026-11-08 21:50:44
*/
@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel>
    implements AiModelService{

    private final AiProviderService aiProviderService;

    public AiModelServiceImpl(AiProviderService aiProviderService) {
        this.aiProviderService = aiProviderService;
    }

    @Override
    public PageResult<AiModelVO> page(AiModelQuery query) {
        Page<AiModel> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<AiModel>()
                // 一个关键字同时模糊匹配模型名称和模型编码
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(AiModel::getModelName, query.getKeyword())
                        .or()
                        .like(AiModel::getModelCode, query.getKeyword()))
                .eq(StringUtils.hasText(query.getModelType()), AiModel::getModelType, query.getModelType())
                .eq(StringUtils.hasText(query.getEnabled()), AiModel::getEnabled, query.getEnabled())
                .orderByDesc(AiModel::getCreateTime)
                .orderByDesc(AiModel::getId);

        Page<AiModel> result = page(page, wrapper);
        List<AiModel> records = result.getRecords();
        Map<Long, String> providerNameMap = queryProviderNames(records);
        List<AiModelVO> rows = new ArrayList<>(records.size());
        for (AiModel item : records) {
            AiModelVO vo = new AiModelVO();
            BeanUtils.copyProperties(item, vo);
            vo.setProviderName(providerNameMap.get(item.getProviderId()));
            rows.add(vo);
        }
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 批量查询所属提供商名称
     */
    private Map<Long, String> queryProviderNames(List<AiModel> records) {
        List<Long> providerIds = new ArrayList<>();
        for (AiModel item : records) {
            if (item.getProviderId() != null && !providerIds.contains(item.getProviderId())) {
                providerIds.add(item.getProviderId());
            }
        }
        if (providerIds.isEmpty()) {
            return new HashMap<>();
        }
        List<AiProvider> providers = aiProviderService.listByIds(providerIds);
        Map<Long, String> names = new HashMap<>(providers.size());
        for (AiProvider provider : providers) {
            names.put(provider.getId(), provider.getProviderName());
        }
        return names;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("模型不存在");
        }
        removeById(id);
    }



    /**
     * 获取可用的模型上下文
     * @param modelType 模型类型
     * @param modelId 模型ID
     * @return 模型上下文
     */
    @Override
    public AiModelContext getAvailableModelContext(String modelType, Long modelId) {
        // 查询指定类型下所有已启用模型
        LambdaQueryChainWrapper<AiModel> query = lambdaQuery()
                .eq(AiModel::getModelType, modelType)
                .eq(AiModel::getEnabled, EnableStatusEnum.ENABLED.getCode());
        if (modelId != null) {
            query.eq(AiModel::getId, modelId);
        } else {
            query.orderByDesc(AiModel::getIsDefault)
                    .orderByAsc(AiModel::getId);
        }

        List<AiModel> models = query.list();

        // 按优先级依次检查模型所属提供商
        for (AiModel model : models) {
            AiProvider provider = aiProviderService.getById(model.getProviderId());
            // 提供商存在并且已启用，才认为模型可用
            if (provider != null && EnableStatusEnum.ENABLED.getCode().equals(provider.getEnabled())) {
                return new AiModelContext(model, provider);
            }
        }
        if (modelId != null) {
            throw new BusinessException("指定的 AI 模型不存在、已禁用或所属提供商不可用");
        }
        throw new BusinessException("当前没有可用的" + modelType + "模型");
    }

    @Override
    public List<AiModelOptionVO> listAvailableModels(String modelType) {
        List<AiModel> models = lambdaQuery()
                // modelType为空时查询所有类型的可用模型
                .eq(StringUtils.hasText(modelType), AiModel::getModelType, modelType)
                .eq(AiModel::getEnabled, EnableStatusEnum.ENABLED.getCode())
                .orderByDesc(AiModel::getIsDefault)
                .orderByAsc(AiModel::getId)
                .list();

        // 批量查询所属提供商，避免逐条查询
        List<Long> providerIds = new ArrayList<>();
        for (AiModel model : models) {
            if (model.getProviderId() != null && !providerIds.contains(model.getProviderId())) {
                providerIds.add(model.getProviderId());
            }
        }
        Map<Long, AiProvider> providerMap = new HashMap<>();
        if (!providerIds.isEmpty()) {
            List<AiProvider> providers = aiProviderService.listByIds(providerIds);
            for (AiProvider provider : providers) {
                providerMap.put(provider.getId(), provider);
            }
        }

        List<AiModelOptionVO> options = new ArrayList<>();
        for (AiModel model : models) {
            AiProvider provider = providerMap.get(model.getProviderId());
            // 提供商存在且已启用，模型才算可用
            if (provider == null || !EnableStatusEnum.ENABLED.getCode().equals(provider.getEnabled())) {
                continue;
            }
            AiModelOptionVO option = new AiModelOptionVO();
            option.setId(model.getId());
            option.setModelCode(model.getModelCode());
            options.add(option);
        }
        return options;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveModel(AiModelSaveDTO dto) {
        if (dto.getId() == null) {
            return insertModel(dto);
        }
        updateModel(dto);
        return dto.getId();
    }

    /**
     * 新增模型
     */
    private Long insertModel(AiModelSaveDTO dto) {
        checkProviderExists(dto.getProviderId());
        checkModelCodeUnique(dto.getProviderId(), dto.getModelCode(), null);
        if ("1".equals(dto.getIsDefault())) {
            clearOtherDefault(null, dto.getModelType());
        }
        AiModel model = new AiModel();
        BeanUtils.copyProperties(dto, model);
        save(model);
        return model.getId();
    }

    /**
     * 修改模型
     */
    private void updateModel(AiModelSaveDTO dto) {
        if (getById(dto.getId()) == null) {
            throw new BusinessException("模型不存在");
        }
        checkProviderExists(dto.getProviderId());
        checkModelCodeUnique(dto.getProviderId(), dto.getModelCode(), dto.getId());
        if ("1".equals(dto.getIsDefault())) {
            clearOtherDefault(dto.getId(), dto.getModelType());
        }
        AiModel model = new AiModel();
        BeanUtils.copyProperties(dto, model);
        updateById(model);
    }

    /**
     * 查询并取消其他默认模型（excludeId非空时排除自身）
     */
    private void clearOtherDefault(Long excludeId, String modelType) {
        List<AiModel> defaults = lambdaQuery()
                .eq(AiModel::getIsDefault, "1")
                .eq(AiModel::getModelType, modelType)
                .ne(excludeId != null, AiModel::getId, excludeId)
                .list();
        if (defaults.isEmpty()) {
            return;
        }
        List<AiModel> updates = new ArrayList<>(defaults.size());
        for (AiModel item : defaults) {
            AiModel update = new AiModel();
            update.setId(item.getId());
            update.setIsDefault("0");
            updates.add(update);
        }
        updateBatchById(updates);
    }

    /**
     * 校验所属提供商是否存在
     */
    private void checkProviderExists(Long providerId) {
        if (aiProviderService.getById(providerId) == null) {
            throw new BusinessException("所属提供商不存在");
        }
    }

    /**
     * 校验模型编码唯一（excludeId非空时排除自身）
     */
    private void checkModelCodeUnique(Long providerId, String modelCode, Long excludeId) {
        long count = lambdaQuery()
                .eq(AiModel::getProviderId, providerId)
                .eq(AiModel::getModelCode, modelCode)
                .ne(excludeId != null, AiModel::getId, excludeId)
                .count();

        if (count > 0) {
            throw new BusinessException("当前提供商下模型编码已存在");
        }
    }
}
