package com.ziheng.system.sysdicttype.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.system.sysdicttype.domain.SysDictType;
import com.ziheng.system.sysdicttype.domain.dto.SysDictTypeAddDTO;
import com.ziheng.system.sysdicttype.domain.vo.SysDictTypeListVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_dict_type(字典类型表)】的数据库操作Service
* @createDate 2026-09-03 16:35:21
*/
public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 新增或修改字典类型（dictId 为空表示新增，不为空表示修改）
     * @param dto 字典类型信息
     * @return 字典ID
     */
    Long addDictType(SysDictTypeAddDTO dto);

    /**
     * 查询字典类型列表（不分页，可按字典名称模糊搜索）
     * @param dictName 字典名称（可选）
     * @return 字典类型列表
     */
    List<SysDictTypeListVO> listDictTypes(String dictName);

    /**
     * 删除字典类型（该类型下存在字典值时不允许删除）
     * @param dictId 字典类型ID
     */
    void deleteDictType(Long dictId);

    /**
     * 根据字典类型ID查询字典类型编码
     * @param dictId 字典类型ID
     * @return 字典类型编码
     */
    String getDictTypeById(Long dictId);



    /**
     * 根据字典类型编码查询字典类型
     */
    SysDictType getByDictType(String dictType);
}
