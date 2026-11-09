package com.ziheng.ai.infrastructure.model.service;

import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.domain.dto.AiModelQuery;
import com.ziheng.ai.infrastructure.model.domain.dto.AiModelSaveDTO;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelVO;
import com.ziheng.common.core.domain.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【ai_model(AI模型配置表)】的数据库操作Service
* @createDate 2026-11-08 21:50:44
*/
public interface AiModelService extends IService<AiModel> {

    /**
     * 分页查询模型
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<AiModelVO> page(AiModelQuery query);

    /**
     * 保存模型（新增或修改，id为空时新增，id不为空时修改）
     *
     * @param dto 保存参数
     * @return 主键
     */
    Long saveModel(AiModelSaveDTO dto);

    /**
     * 删除模型（物理删除）
     *
     * @param id 主键
     */
    void delete(Long id);



    /**
     * 根据模型类型获取当前可用模型
     *
     * @param modelType 模型类型 CHAT、EMBEDDING、RERANK
     * @return 可用模型
     */
    AiModel getAvailableModel(String modelType);

}
