package com.ziheng.system.sysdictdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.framework.cache.CacheKey;
import com.ziheng.framework.cache.RedisCacheService;
import com.ziheng.system.sysdictdata.domain.SysDictData;
import com.ziheng.system.sysdictdata.domain.dto.SysDictDataAddDTO;
import com.ziheng.system.sysdictdata.domain.vo.SysDictDataEditVO;
import com.ziheng.system.sysdictdata.domain.vo.SysDictDataListVO;
import com.ziheng.system.sysdictdata.domain.vo.SysDictOptionVO;
import com.ziheng.system.sysdictdata.mapper.SysDictDataMapper;
import com.ziheng.system.sysdictdata.service.SysDictDataService;
import com.ziheng.system.sysdicttype.domain.SysDictType;
import com.ziheng.system.sysdicttype.mapper.SysDictTypeMapper;
import com.ziheng.system.sysdicttype.service.SysDictTypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【sys_dict_data(字典数据表)】的数据库操作Service实现
* @createDate 2026-09-03 16:35:21
*/
@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData>
    implements SysDictDataService {


    private final RedisCacheService redisCacheService;
    private final SysDictTypeService sysDictTypeService;

    public SysDictDataServiceImpl(RedisCacheService redisCacheService, SysDictTypeService sysDictTypeService) {
        this.redisCacheService = redisCacheService;
        this.sysDictTypeService = sysDictTypeService;
    }

    /**
     * 新增或修改字典值（dictCode 为空则新增，不为空则修改）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDictData(SysDictDataAddDTO dto) {
        // 校验所属字典类型存在
        SysDictType dictType = sysDictTypeService.getById(dto.getDictId());
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        Long dictCode = dto.getDictCode();
        if (dictCode == null) {
            dictCode = insertDictData(dto);
        } else {
            updateDictData(dto);
        }
        // 新增、修改后都清除字典缓存
        redisCacheService.delete(CacheKey.DICT, dictType.getDictType());
        return dictCode;
    }

    /**
     * 新增字典值
     */
    private Long insertDictData(SysDictDataAddDTO dto) {
        SysDictData data = new SysDictData();
        BeanUtils.copyProperties(dto, data);
        // 是否默认：为空默认 N
        if (!StringUtils.hasText(data.getIsDefault())) {
            data.setIsDefault("N");
        }
        // 状态为空默认启用(0正常)
        if (!StringUtils.hasText(data.getStatus())) {
            data.setStatus("0");
        }
        Date now = new Date();
        data.setCreateTime(now);
        data.setUpdateTime(now);
        save(data);
        return data.getDictCode();
    }

    /**
     * 修改字典值
     */
    private void updateDictData(SysDictDataAddDTO dto) {
        Long dictCode = dto.getDictCode();
        SysDictData old = getById(dictCode);
        if (old == null) {
            throw new BusinessException("字典值不存在");
        }
        SysDictData data = new SysDictData();
        BeanUtils.copyProperties(dto, data);
        // 未传是否默认/状态时保留原值（MP updateById 忽略 null 字段）
        if (!StringUtils.hasText(data.getIsDefault())) {
            data.setIsDefault(old.getIsDefault());
        }
        if (!StringUtils.hasText(data.getStatus())) {
            data.setStatus(old.getStatus());
        }
        data.setUpdateTime(new Date());
        updateById(data);
    }

    /**
     * 根据字典类型ID查询字典值列表（按排序升序，同排序按ID升序）
     */
    @Override
    public List<SysDictDataListVO> listDictDatas(Long dictId) {
        if (dictId == null) {
            throw new BusinessException("所属字典类型不能为空");
        }
        return lambdaQuery()
                .eq(SysDictData::getDictId, dictId)
                .orderByAsc(SysDictData::getDictSort)
                .orderByAsc(SysDictData::getDictCode)
                .list()
                .stream()
                .map(SysDictDataListVO::from)
                .toList();
    }

    /**
     * 根据字典类型编码查询启用的字典值选项（供前端页面下拉框/标签展示使用）
     */
    @Override
    public List<SysDictOptionVO> listDictOptions(String dictType) {
        // 查询缓存
        List<SysDictOptionVO> cached = redisCacheService.get(CacheKey.DICT, dictType, new TypeReference<>() {});
        if (cached != null) {
            return cached;
        }

        SysDictType dictTypeRow = sysDictTypeService.getByDictType(dictType);
        if (dictTypeRow == null) {
            throw new BusinessException("字典类型不存在：" + dictType);
        }
        // 仅返回启用的字典值（停用的值不展示到页面）
        List<SysDictOptionVO> result = lambdaQuery()
                .eq(SysDictData::getDictId, dictTypeRow.getDictId())
                .eq(SysDictData::getStatus, "1")
                .orderByAsc(SysDictData::getDictSort)
                .orderByAsc(SysDictData::getDictCode)
                .list()
                .stream()
                .map(SysDictOptionVO::from)
                .collect(Collectors.toList());
        redisCacheService.set(CacheKey.DICT, dictType, result);

        return result;
    }

    /**
     * 根据字典数据ID查询详情（编辑回显）
     */
    @Override
    public SysDictDataEditVO getDictDataDetail(Long dictCode) {
        SysDictData data = getById(dictCode);
        if (data == null) {
            throw new BusinessException("字典值不存在");
        }
        SysDictDataEditVO vo = new SysDictDataEditVO();
        BeanUtils.copyProperties(data, vo);
        return vo;
    }

    /**
     * 删除字典值（物理删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long dictCode) {
        SysDictData old = getById(dictCode);
        if (old == null) {
            throw new BusinessException("字典值不存在");
        }
        // 根据 dictId 获取字典类型编码
        String dictType = sysDictTypeService.getDictTypeById(old.getDictId());

        removeById(dictCode);

        // 删除对应字典缓存
        redisCacheService.delete(CacheKey.DICT, dictType);

    }
}
