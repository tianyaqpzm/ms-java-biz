package com.dark.aiagent.infrastructure.persistence.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("knowledge_topic")
public class KnowledgeTopicDO {
    @TableId
    private String id;
    private String name;
    private String icon;
    private String description;
    private String visibleScope;
    private String templateName;
    private Date createTime;
    private Date updateTime;
}
