-- V1.5__create_prompt_management_schema.sql

-- A. Prompt 模板主表 (ms_prompt_template)
CREATE TABLE IF NOT EXISTS ms_prompt_template (
    id SERIAL PRIMARY KEY,
    slug VARCHAR(128) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL DEFAULT 'System',
    description TEXT,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ms_prompt_template IS 'Prompt 模板主表';
COMMENT ON COLUMN ms_prompt_template.id IS '主键ID';
COMMENT ON COLUMN ms_prompt_template.slug IS '业务唯一标识';
COMMENT ON COLUMN ms_prompt_template.type IS '类型（System/User/Tool）';
COMMENT ON COLUMN ms_prompt_template.description IS '用途描述';

-- B. Prompt 版本明细表 (ms_prompt_version)
CREATE TABLE IF NOT EXISTS ms_prompt_version (
    id SERIAL PRIMARY KEY,
    template_id INTEGER NOT NULL,
    version_tag VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    variables JSONB,
    model_config JSONB,
    is_active BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_template FOREIGN KEY (template_id) REFERENCES ms_prompt_template(id) ON DELETE CASCADE
);

CREATE INDEX idx_prompt_version_template_id ON ms_prompt_version(template_id);
CREATE INDEX idx_prompt_version_active ON ms_prompt_version(template_id) WHERE is_active = TRUE;

COMMENT ON TABLE ms_prompt_version IS 'Prompt 版本明细表';
COMMENT ON COLUMN ms_prompt_version.id IS '主键ID';
COMMENT ON COLUMN ms_prompt_version.template_id IS '关联模板ID';
COMMENT ON COLUMN ms_prompt_version.version_tag IS '版本号';
COMMENT ON COLUMN ms_prompt_version.content IS 'Prompt 原文（含占位符）';
COMMENT ON COLUMN ms_prompt_version.variables IS '变量定义列表';
COMMENT ON COLUMN ms_prompt_version.model_config IS '模型参数配置 (JSON)';
COMMENT ON COLUMN ms_prompt_version.is_active IS '是否为当前生产生效版本';

-- 初始化数据：将厨师人格迁移至数据库
INSERT INTO ms_prompt_template (slug, type, description)
VALUES ('chef_persona_rag', 'System', '20年经验中餐大厨人格 (带 RAG 上下文)');

INSERT INTO ms_prompt_version (template_id, version_tag, content, variables, model_config, is_active)
SELECT 
    id, 
    'v1.0.0', 
    '你是一位拥有20年经验的中餐大厨，精通各大菜系。请用专业、亲切且热情的语气回答用户的菜谱相关问题，并提供实用的烹饪技巧。\n\nContext:\n{{context}}',
    '["context"]',
    '{"model": "gemini-1.5-pro", "temperature": 0.7}',
    TRUE
FROM ms_prompt_template WHERE slug = 'chef_persona_rag';
