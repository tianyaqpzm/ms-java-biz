package com.dark.aiagent.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dark.aiagent.infrastructure.persistence.entity.McpToolCacheDO;

@Mapper
public interface McpToolCacheMapper extends BaseMapper<McpToolCacheDO> {
}
