package com.dark.aiagent.infrastructure.persistence.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dark.aiagent.infrastructure.persistence.prompt.entity.PromptVersionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptMapper extends BaseMapper<PromptVersionDO> {

    /**
     * 根据 Slug 获取当前生效的 Prompt 版本 (SQL 已迁移至 XML)
     */
    PromptVersionDO findActiveBySlug(String slug);
}
