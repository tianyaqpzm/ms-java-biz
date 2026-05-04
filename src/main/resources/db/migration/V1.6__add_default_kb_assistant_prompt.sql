-- V1.6__add_default_kb_assistant_prompt.sql

-- 1. 添加默认知识库助手模板 (default_kb_assistant)
INSERT INTO ms_prompt_template (slug, type, description)
VALUES ('default_kb_assistant', 'System', '通用知识库问答助手人格');

-- 2. 添加初始版本
INSERT INTO ms_prompt_version (template_id, version_tag, content, variables, model_config, is_active)
SELECT 
    id, 
    'v1.0.0', 
    '你是一位专业的知识库助手。请根据提供的上下文信息，准确、简洁且客观地回答用户的问题。如果上下文中没有相关信息，请诚实地告知用户。\n\nContext:\n{{context}}',
    '["context"]',
    '{"model": "gemini-1.5-flash", "temperature": 0.3}',
    TRUE
FROM ms_prompt_template WHERE slug = 'default_kb_assistant';
