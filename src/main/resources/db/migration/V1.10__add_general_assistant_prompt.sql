-- V1.10__add_general_assistant_prompt.sql

-- 1. 添加通用助手模板 (general_assistant)
INSERT INTO ms_prompt_template (slug, type, description)
VALUES ('general_assistant', 'System', '通用对话助手，处理常规问答与工具调用')
ON CONFLICT (slug) DO NOTHING;

-- 2. 添加初始版本 v1.0.0
INSERT INTO ms_prompt_version (template_id, version_tag, content, variables, model_config, is_active)
SELECT 
    id, 
    'v1.0.0', 
    '你是一个全能的 AI 助手。你可以：
1. 友好、专业地回答用户的各类通用问题。
2. 在需要时，通过调用工具来获取实时信息或执行操作（如查询数据库、处理文件等）。
3. 保持多轮对话的上下文连贯性。

请始终使用 Markdown 格式回复。',
    '[]',
    '{"model": "gemini-1.5-flash", "temperature": 0.7}',
    TRUE
FROM ms_prompt_template WHERE slug = 'general_assistant'
AND NOT EXISTS (
    SELECT 1 FROM ms_prompt_version v WHERE v.template_id = ms_prompt_template.id AND v.version_tag = 'v1.0.0'
);
