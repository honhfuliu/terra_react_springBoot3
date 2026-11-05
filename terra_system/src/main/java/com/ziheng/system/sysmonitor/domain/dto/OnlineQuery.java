package com.ziheng.system.sysmonitor.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OnlineQuery extends PageQuery {
    private String username;
    private String ip;
}
