package com.ziheng.system.sysuser.domain.vo;


import com.ziheng.common.util.excel.annotation.ExcelColumn;
import lombok.Data;

import java.util.Date;

@Data
public class SysUserExportVO {
    @ExcelColumn(value = "用户编号", order = 0, width = 15)
    private Long userId;

    @ExcelColumn(value = "用户名", order = 1, width = 20)
    private String username;

    @ExcelColumn(value = "昵称", order = 2, width = 20)
    private String nickname;

    @ExcelColumn(value = "手机号", order = 3, width = 18)
    private String phone;

    @ExcelColumn(value = "邮箱", order = 4, width = 25)
    private String email;

    @ExcelColumn(value = "性别", order = 5, width = 12)
    private String sex;

    @ExcelColumn(value = "状态", order = 6, width = 12)
    private String status;

    @ExcelColumn(value = "部门", order = 7, width = 20)
    private String deptName;

    @ExcelColumn(value = "创建时间", order = 8, width = 22, format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
