package com.dark.aiagent.domain.knowledge.entity;

import com.dark.aiagent.domain.knowledge.valueobject.KnowledgeConfig;
import java.util.Date;
import java.util.Objects;

/**
 * 知识文档领域实体 (充血模型)
 */
public class KnowledgeDocument {
    private final String id;
    private String topicId;
    private String title;
    private String status;
    private String author;
    private String filePath;
    private KnowledgeConfig config;
    private Date createTime;
    private Date updateTime;

    // 必须提供私有的无参构造函数（为了反射兼容）
    private KnowledgeDocument() {
        this.id = null;
    }

    // 公开的实例化必须通过包含所有必填属性的构造函数或工厂方法
    public KnowledgeDocument(String id, String topicId, String title, String author, String filePath, KnowledgeConfig config) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID cannot be null or empty");
        this.id = id;
        this.topicId = topicId;
        this.title = title;
        this.author = author;
        this.filePath = filePath;
        this.config = config;
        this.status = "CREATED"; // 初始业务状态
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    // 行为封装：状态的改变必须通过具有业务语义的方法进行
    public void publish() {
        this.status = "PUBLISHED";
        this.updateTime = new Date();
    }

    public void assignConfig(KnowledgeConfig newConfig) {
        if (newConfig == null) throw new IllegalArgumentException("Config cannot be null");
        this.config = newConfig;
        this.updateTime = new Date();
    }

    public void process() {
        this.status = "PROCESSING";
        this.updateTime = new Date();
    }

    public void markAsFailed() {
        this.status = "FAILED";
        this.updateTime = new Date();
    }
    
    // 仅提供 Getter
    public String getId() { return id; }
    public String getTopicId() { return topicId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getAuthor() { return author; }
    public String getFilePath() { return filePath; }
    public KnowledgeConfig getConfig() { return config; }
    public Date getCreateTime() { return createTime; }
    public Date getUpdateTime() { return updateTime; }

    // equals() 和 hashCode() 只能且必须基于 ID 字段进行重写
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeDocument that = (KnowledgeDocument) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
