package com.dark.aiagent.infrastructure.persistence.prompt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dark.aiagent.infrastructure.persistence.handler.PostgresJacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Prompt 版本明细数据对象
 */
@Data
@TableName(value = "ms_prompt_version", autoResultMap = true)
public class PromptVersionDO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer templateId;
    private String versionTag;
    private String content;
    
    @TableField(typeHandler = PostgresJacksonTypeHandler.class)
    private Map<String, Object> variables;
    
    @TableField(typeHandler = PostgresJacksonTypeHandler.class)
    private Map<String, Object> modelConfig;
    
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
