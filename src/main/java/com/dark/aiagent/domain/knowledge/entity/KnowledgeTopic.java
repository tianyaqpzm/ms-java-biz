package com.dark.aiagent.domain.knowledge.entity;

import lombok.Builder;
import lombok.Getter;
import java.util.Date;

/**
 * 知识库主题领域实体
 */
@Getter
@Builder
public class KnowledgeTopic {
    private String id;
    private String name;
    private String icon;
    private String description;
    private String visibleScope;
    private String templateName;
    private Date createTime;
    private Date updateTime;
}
