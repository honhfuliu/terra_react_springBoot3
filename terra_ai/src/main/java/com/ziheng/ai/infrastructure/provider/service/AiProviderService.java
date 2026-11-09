package com.ziheng.ai.infrastructure.provider.service;

import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.ai.infrastructure.provider.domain.dto.AiProviderQuery;
import com.ziheng.ai.infrastructure.provider.domain.dto.AiProviderSaveDTO;
import com.ziheng.ai.infrastructure.provider.domain.vo.AiProviderOptionVO;
import com.ziheng.ai.infrastructure.provider.domain.vo.AiProviderVO;
import com.ziheng.common.core.domain.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_provider(AI模型提供商表)】的数据库操作Service
* @createDate 2026-11-07 22:52:53
*/
public interface AiProviderService extends IService<AiProvider> {

    /**
     * 分页查询提供商
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<AiProviderVO> page(AiProviderQuery query);

    /**
     * 查询所有提供商下拉选项（仅id和名称）
     *
     * @return 下拉选项列表
     */
    List<AiProviderOptionVO> listOptions();


    /**
     * 保存提供商（新增或修改，id为空时新增，id不为空时修改）
     *
     * @param dto 保存参数
     * @return 主键
     */
    Long saveProvider(AiProviderSaveDTO dto);

    /**
     * 删除提供商
     *
     * @param id 主键
     */
    void delete(Long id);

}
