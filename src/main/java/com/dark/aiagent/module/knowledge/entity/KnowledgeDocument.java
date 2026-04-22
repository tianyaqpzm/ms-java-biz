package com.dark.aiagent.module.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.util.Date;

@Data
@TableName(value = "knowledge_document", autoResultMap = true)
public class KnowledgeDocument {
    @TableId
    private String id;
    private String topicId;
    private String title;
    private String status;
    private String author;
    private String filePath;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private KnowledgeConfig configJson;

    private Date createTime;
    private Date updateTime;
}
