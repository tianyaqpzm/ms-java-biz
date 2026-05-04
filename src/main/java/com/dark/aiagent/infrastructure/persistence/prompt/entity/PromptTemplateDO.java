package com.dark.aiagent.infrastructure.persistence.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.OffsetDateTime;

/**
 * Prompt 模板主表数据对象
 */
@Data
@TableName("ms_prompt_template")
public class PromptTemplateDO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String slug;
    private String type;
    private String description;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
