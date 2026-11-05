package com.ziheng.system.sysdicttype.domain.vo;

import com.ziheng.system.sysdicttype.domain.SysDictType;
import lombok.Data;

import java.util.Date;

/**
 * 字典类型列表 VO（不分页，返回 id/名称/编码/状态/更新时间/数量）
 */
@Data
public class SysDictTypeListVO {

    /**
     * 字典ID
     */
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典编码/类型
     */
    private String dictType;

    /**
     * 状态 0正常(启用) 1停用
     */
    private String status;

    /**
     * 更新时间
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 字典值数量（该字典类型下的字典数据条数）
     */
    private Long count;


     private String remark;

    public static SysDictTypeListVO from(SysDictType dictType) {
        SysDictTypeListVO vo = new SysDictTypeListVO();
        vo.setDictId(dictType.getDictId());
        vo.setDictName(dictType.getDictName());
        vo.setDictType(dictType.getDictType());
        vo.setStatus(dictType.getStatus());
        vo.setUpdateTime(dictType.getUpdateTime());
        vo.setRemark(dictType.getRemark());
        return vo;
    }
}
