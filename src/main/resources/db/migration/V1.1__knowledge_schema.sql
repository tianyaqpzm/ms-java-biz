-- V1.1__knowledge_schema.sql

CREATE TABLE IF NOT EXISTS knowledge_topic (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon VARCHAR(64),
    description TEXT,
    visible_scope VARCHAR(64) DEFAULT 'public',
    template_name VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_document (
    id VARCHAR(64) PRIMARY KEY,
    topic_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(64) DEFAULT '未处理',
    author VARCHAR(128),
    file_path TEXT,
    config_json JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_document_topic ON knowledge_document(topic_id);
