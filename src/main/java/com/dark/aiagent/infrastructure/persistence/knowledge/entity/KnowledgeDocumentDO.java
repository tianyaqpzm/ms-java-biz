package com.dark.aiagent.infrastructure.persistence.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dark.aiagent.infrastructure.persistence.handler.PostgresJacksonTypeHandler;
import com.dark.aiagent.domain.knowledge.valueobject.KnowledgeConfig;
import lombok.Data;
import java.util.Date;

/**
 * 知识库文档持久化数据对象 (Data Object)
 * 仅用于框架的 ORM 映射，不包含任何业务方法
 */
@Data
@TableName(value = "ms_knowledge_document", autoResultMap = true)
public class KnowledgeDocumentDO {
    @TableId
    private String id;
    private String topicId;
    private String title;
    private String status;
    private String author;
    private String filePath;

    @TableField(typeHandler = PostgresJacksonTypeHandler.class)
    private KnowledgeConfig configJson;

    private Date createTime;
    private Date updateTime;
}
