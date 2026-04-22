package com.dark.aiagent.module.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("knowledge_topic")
public class KnowledgeTopic {
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
