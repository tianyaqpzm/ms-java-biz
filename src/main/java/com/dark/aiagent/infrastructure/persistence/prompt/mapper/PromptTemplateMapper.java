package com.dark.aiagent.infrastructure.persistence.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dark.aiagent.infrastructure.persistence.prompt.entity.PromptTemplateDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplateDO> {
}
