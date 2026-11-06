package com.ziheng.system.sysdicttype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.framework.cache.CacheKey;
import com.ziheng.framework.cache.RedisCacheService;
import com.ziheng.system.sysdictdata.domain.SysDictData;
import com.ziheng.system.sysdictdata.domain.enums.DictStatus;
import com.ziheng.system.sysdictdata.mapper.SysDictDataMapper;
import com.ziheng.system.sysdicttype.domain.SysDictType;
import com.ziheng.system.sysdicttype.domain.dto.SysDictTypeAddDTO;
import com.ziheng.system.sysdicttype.domain.vo.SysDictTypeListVO;
import com.ziheng.system.sysdicttype.mapper.SysDictTypeMapper;
import com.ziheng.system.sysdicttype.service.SysDictTypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description  针对表【sys_dict_type(字典类型表)】的数据库操作Service实现
* @createDate 2026-09-03 16:35:21
*/
@Service
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType>
    implements SysDictTypeService {

    private final SysDictDataMapper sysDictDataMapper;
    private final RedisCacheService redisCacheService;

    public SysDictTypeServiceImpl(SysDictDataMapper sysDictDataMapper, RedisCacheService redisCacheService) {
        this.sysDictDataMapper = sysDictDataMapper;
        this.redisCacheService = redisCacheService;
    }

    /**
     * 新增或修改字典类型（dictId 为空则新增，不为空则修改）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addDictType(SysDictTypeAddDTO dto) {
        Long dictId = dto.getDictId();
        if (dictId == null) {
            dictId = insertDictType(dto);
        } else {
            updateDictType(dto);
        }
        return dictId;
    }

    /**
     * 新增字典类型
     */
    private Long insertDictType(SysDictTypeAddDTO dto) {
        checkDictTypeUnique(dto.getDictType(), null);
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(dto, dictType);
        // 状态为空默认启用(0正常)
        if (!StringUtils.hasText(dictType.getStatus())) {
            dictType.setStatus(DictStatus.NORMAL.getCode());
        }
        Date now = new Date();
        dictType.setCreateTime(now);
        dictType.setUpdateTime(now);
        save(dictType);
        return dictType.getDictId();
    }

    /**
     * 修改字典类型
     */
    private void updateDictType(SysDictTypeAddDTO dto) {
        Long dictId = dto.getDictId();
        SysDictType old = getById(dictId);
        if (old == null) {
            throw new BusinessException("字典不存在");
        }
        checkDictTypeUnique(dto.getDictType(), dictId);
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(dto, dictType);
        // 未传状态时保留原值（MP updateById 忽略 null 字段）
        if (!StringUtils.hasText(dictType.getStatus())) {
            dictType.setStatus(old.getStatus());
        }
        dictType.setUpdateTime(new Date());
        updateById(dictType);
        // 删除缓存
        redisCacheService.delete(CacheKey.DICT, old.getDictType());
    }

    /**
     * 查询字典类型列表（不分页，按字典名称模糊搜索，按更新时间倒序）
     * 并统计每个字典类型下的字典值数量
     */
    @Override
    public List<SysDictTypeListVO> listDictTypes(String dictName) {
        List<SysDictType> types = lambdaQuery()
                .like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName)
                .orderByDesc(SysDictType::getUpdateTime)
                .list();
        List<SysDictTypeListVO> rows = types.stream()
                .map(SysDictTypeListVO::from)
                .toList();
        if (!rows.isEmpty()) {
            Map<Long, Long> countMap = countDictDataByDictId(
                    types.stream().map(SysDictType::getDictId).toList());
            rows.forEach(vo -> vo.setCount(countMap.getOrDefault(vo.getDictId(), 0L)));
        }
        return rows;
    }

    /**
     * 删除字典类型（仅允许删除不含字典值的类型，物理删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long dictId) {
        SysDictType dictType = getById(dictId);
        if (dictType == null) {
            throw new BusinessException("字典不存在");
        }
        long count = sysDictDataMapper.selectCount(
                new LambdaQueryWrapper<SysDictData>()
                        .eq(SysDictData::getDictId, dictId)
        );
        if (count > 0) {
            throw new BusinessException("该字典类型下还有 " + count + " 个字典值，请先删除字典值后再删除字典类型");
        }
        removeById(dictId);
        // 删除缓存
        redisCacheService.delete(CacheKey.DICT, dictType.getDictType());
    }

    @Override
    public String getDictTypeById(Long dictId) {
        LambdaQueryWrapper<SysDictType> last = new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictId, dictId)
                .select(SysDictType::getDictType);
        SysDictType sysDictType = baseMapper.selectOne(last);
        if (sysDictType == null) {
            return null;
        }
        return sysDictType.getDictType();
    }

    /**
     * 根据字典类型编码查询字典类型
     * @param dictType
     * @return
     */
    @Override
    public SysDictType getByDictType(String dictType) {
        if (!StringUtils.hasText(dictType)) {
            return null;
        }
        return lambdaQuery()
                .eq(SysDictType::getDictType, dictType)
                .one();
    }

    /**
     * 按字典类型ID分组统计字典值数量，返回 dictId -> 数量
     */
    private Map<Long, Long> countDictDataByDictId(List<Long> dictIds) {
        QueryWrapper<SysDictData> wrapper = new QueryWrapper<SysDictData>()
                .in("dict_id", dictIds)
                .select("dict_id", "COUNT(*) AS cnt")
                .groupBy("dict_id");

        return sysDictDataMapper.selectMaps(wrapper)
                .stream()
                .collect(Collectors.toMap(
                        m -> ((Number) m.get("dict_id")).longValue(),
                        m -> ((Number) m.get("cnt")).longValue()
                ));
    }

    /**
     * 校验字典编码唯一（修改时排除自身）
     */
    private void checkDictTypeUnique(String dictType, Long excludeId) {
        boolean exists = lambdaQuery()
                .eq(SysDictType::getDictType, dictType)
                .ne(excludeId != null, SysDictType::getDictId, excludeId)
                .count() > 0;
        if (exists) {
            throw new BusinessException("字典编码已存在");
        }
    }
}
