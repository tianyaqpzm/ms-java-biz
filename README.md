# ms-java-biz (核心业务服务)

## 简介
本微服务负责企业核心业务逻辑与数据持久化，通过 MCP (Model Context Protocol) 协议为 AI Agent 提供业务工具集。

## 工程目录结构 (DDD 4-Layer Architecture)

```text
src/main/java/com/dark/aiagent
├── interfaces           # 接口层 (Interfaces)：负责处理外部请求与响应格式化
│   ├── chat             # 聊天相关接口 (XiaozhiController, ChatHistoryController)
│   ├── event            # 活动相关接口 (TimeLimitedEventController)
│   ├── knowledge        # 知识库管理接口 (KnowledgeController)
│   └── user             # 用户信息接口 (UserController)
├── application          # 应用层 (Application)：负责业务流程编排与 DTO 转换
│   ├── chat             # 聊天业务流程 (含 DTO)
│   ├── event            # 活动业务流程 (含 DTO)
│   ├── knowledge        # 知识库处理流程 (含分词、上传策略)
│   └── user             # 用户相关应用逻辑
├── domain               # 领域层 (Domain)：核心业务逻辑与实体定义 (纯 POJO)
│   ├── chat             # 聊天领域模型 (ChatSession, ChatMessage)
│   ├── event            # 活动领域模型 (TimeLimitedEvent)
│   └── knowledge        # 知识库领域模型 (KnowledgeDocument, KnowledgeTopic)
├── infrastructure       # 基础设施层 (Infrastructure)：技术实现细节
│   ├── persistence      # 持久化实现 (MySQL/Postgres MyBatis-Plus, Mongo)
│   │   ├── chat         # 聊天数据 DO 与 Mapper
│   │   ├── event        # 活动数据 DO 与 Mapper
│   │   └── knowledge    # 知识库数据 DO 与 Mapper
│   └── client           # 外部服务客户端 (RPC, HTTP 外部调用)
├── assistant            # AI 助手核心能力封装 (LangChain4j 编排)
├── config               # 全局配置类
├── constant             # 全局常量
├── mcp                  # MCP 协议实现 (Tools 注册与消息推送)
├── security             # 安全鉴权逻辑
└── store                # 专用存储实现 (如 MongoChatMemoryStore)
```

## 开发规范
* 遵循 `CODING_STANDARDS.md` 中的 DDD 规范。
* 领域层严禁引入任何框架注解。
* 复杂查询需通过物理汇总表 + 数据库触发器实现。
