package com.ziheng.ai.infrastructure.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.mapper.AiModelMapper;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.ai.infrastructure.provider.domain.dto.AiProviderQuery;
import com.ziheng.ai.infrastructure.provider.domain.dto.AiProviderSaveDTO;
import com.ziheng.ai.infrastructure.provider.domain.vo.AiProviderOptionVO;
import com.ziheng.ai.infrastructure.provider.domain.vo.AiProviderVO;
import com.ziheng.ai.infrastructure.provider.mapper.AiProviderMapper;
import com.ziheng.ai.infrastructure.provider.service.AiProviderService;
import com.ziheng.ai.infrastructure.security.AiApiKeyEncryptor;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_provider(AI模型提供商表)】的数据库操作Service实现
* @createDate 2026-11-07 22:52:53
*/
@Service
public class AiProviderServiceImpl extends ServiceImpl<AiProviderMapper, AiProvider>
    implements AiProviderService {

    private final AiApiKeyEncryptor aiApiKeyEncryptor;

    private final AiModelMapper aiModelMapper;

    public AiProviderServiceImpl(AiApiKeyEncryptor aiApiKeyEncryptor, AiModelMapper aiModelMapper) {
        this.aiApiKeyEncryptor = aiApiKeyEncryptor;
        this.aiModelMapper = aiModelMapper;
    }

    @Override
    public PageResult<AiProviderVO> page(AiProviderQuery query) {
        Page<AiProvider> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiProvider> wrapper = new LambdaQueryWrapper<AiProvider>()
                // 一个关键字同时模糊匹配提供商编码和提供商名称
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(AiProvider::getProviderCode, query.getKeyword())
                        .or()
                        .like(AiProvider::getProviderName, query.getKeyword()))
                .eq(StringUtils.hasText(query.getProtocol()), AiProvider::getProtocol, query.getProtocol())
                .orderByDesc(AiProvider::getCreateTime)
                .orderByDesc(AiProvider::getId);

        Page<AiProvider> result = page(page, wrapper);
        List<AiProviderVO> rows = new ArrayList<>(result.getRecords().size());
        for (AiProvider item : result.getRecords()) {
            AiProviderVO vo = new AiProviderVO();
            BeanUtils.copyProperties(item, vo);
            vo.setApiKey(maskApiKey(item.getApiKey()));
            rows.add(vo);
        }
        return PageResult.of(result.getTotal(), rows);
    }

    @Override
    public List<AiProviderOptionVO> listOptions() {
        List<AiProvider> providers = lambdaQuery()
                .select(AiProvider::getId, AiProvider::getProviderName)
                .orderByAsc(AiProvider::getId)
                .list();
        List<AiProviderOptionVO> options = new ArrayList<>(providers.size());
        for (AiProvider item : providers) {
            AiProviderOptionVO option = new AiProviderOptionVO();
            option.setId(item.getId());
            option.setProviderName(item.getProviderName());
            options.add(option);
        }
        return options;
    }

    /**
     * 解密API Key并脱敏（保留前4位和后4位）
     */
    private String maskApiKey(String ciphertext) {
        if (!StringUtils.hasText(ciphertext)) {
            return null;
        }
        String plaintext;
        try {
            plaintext = aiApiKeyEncryptor.decrypt(ciphertext);
        } catch (IllegalStateException e) {
            // 密文异常时不影响列表查询，仅返回占位符
            return "****";
        }
        if (!StringUtils.hasText(plaintext)) {
            return null;
        }
        if (plaintext.length() <= 8) {
            return plaintext.charAt(0) + "****";
        }
        return plaintext.substring(0, 4) + "****" + plaintext.substring(plaintext.length() - 4);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveProvider(AiProviderSaveDTO dto) {
        if (dto.getId() == null) {
            return insertProvider(dto);
        }
        updateProvider(dto);
        return dto.getId();
    }

    /**
     * 新增提供商
     */
    private Long insertProvider(AiProviderSaveDTO dto) {
        checkProviderCodeUnique(dto.getProviderCode(), null);
        if (StringUtils.hasText(dto.getIsDefault()) && "1".equals(dto.getIsDefault())){
            clearOtherDefault(null);
        }
        AiProvider provider = new AiProvider();
        BeanUtils.copyProperties(dto, provider);
        // API Key 加密后入库
        provider.setApiKey(aiApiKeyEncryptor.encrypt(dto.getApiKey()));
        save(provider);
        return provider.getId();
    }

    /**
     * 查询并取消其他默认提供商（excludeId非空时排除自身）
     */
    private void clearOtherDefault(Long excludeId) {
        List<AiProvider> defaults = lambdaQuery()
                .eq(AiProvider::getIsDefault, "1")
                .ne(excludeId != null, AiProvider::getId, excludeId)
                .list();
        if (defaults.isEmpty()) return;
        ArrayList<AiProvider> list = new ArrayList<>();
        for (AiProvider item : defaults) {
            AiProvider update = new AiProvider();
            update.setId(item.getId());
            update.setIsDefault("0");
            list.add(update);
        }
        baseMapper.updateById(list);
    }

    /**
     * 修改提供商
     */
    private void updateProvider(AiProviderSaveDTO dto) {
        AiProvider old = getById(dto.getId());
        if (old == null) {
            throw new BusinessException("提供商不存在");
        }
        checkProviderCodeUnique(dto.getProviderCode(), dto.getId());
        if (StringUtils.hasText(dto.getIsDefault()) && "1".equals(dto.getIsDefault())){
            clearOtherDefault(dto.getId());
        }
        AiProvider provider = new AiProvider();
        BeanUtils.copyProperties(dto, provider);
        // API Key 为空时不覆盖原值（MyBatis-Plus 更新时忽略null字段），否则加密后入库
        if (StringUtils.hasText(dto.getApiKey())) {
            provider.setApiKey(aiApiKeyEncryptor.encrypt(dto.getApiKey()));
        } else {
            provider.setApiKey(null);
        }
        updateById(provider);
    }

    /**
     * 校验提供商编码唯一（excludeId非空时排除自身）
     */
    private void checkProviderCodeUnique(String providerCode, Long excludeId) {
        long count = lambdaQuery()
                .eq(AiProvider::getProviderCode, providerCode)
                .ne(excludeId != null, AiProvider::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BusinessException("提供商编码已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("提供商不存在");
        }
        // 删除供应商下的所有模型
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModel::getProviderId, id);
        aiModelMapper.delete(wrapper);

        removeById(id);
    }


}
