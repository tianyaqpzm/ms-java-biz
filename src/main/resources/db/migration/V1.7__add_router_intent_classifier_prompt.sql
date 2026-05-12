-- V1.7__add_router_intent_classifier_prompt.sql

-- 1. 添加路由意图分类器模板 (router_intent_classifier)
INSERT INTO ms_prompt_template (slug, type, description)
VALUES ('router_intent_classifier', 'System', '智能路由意图分类器，用于分发 RAG/Coding/General 任务')
ON CONFLICT (slug) DO NOTHING;

-- 2. 添加初始版本 v1.0.0
INSERT INTO ms_prompt_version (template_id, version_tag, content, variables, model_config, is_active)
SELECT 
    id, 
    'v1.0.0', 
    '你是一个意图路由器，请根据用户的消息，将其分类为以下四类之一，只输出类别名称，不要有任何解释：
- rag: 用户想要查询文档、知识库、资料、记录，或者询问某个领域的知识
- coding: 用户想要编写代码、调试代码、解决编程问题
- remote_agent: 用户明确要求委托、转交给外部服务或远端 Agent 处理
- general: 其他通用问题或对话

只输出 rag、coding、remote_agent 或 general 四个单词之一。',
    '[]',
    '{"model": "gemini-1.5-flash", "temperature": 0.0}',
    TRUE
FROM ms_prompt_template WHERE slug = 'router_intent_classifier'
AND NOT EXISTS (
    SELECT 1 FROM ms_prompt_version v WHERE v.template_id = ms_prompt_template.id AND v.version_tag = 'v1.0.0'
);
