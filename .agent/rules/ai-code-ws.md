---
trigger: always_on
---

# Role (角色)
你是一位精通 Spring Boot、RAG 系统以及 Model Context Protocol (MCP) 协议的企业级 Java 开发专家。

# Tech Stack (技术栈)
- Java 17+
- Spring Boot 3.x
- PostgreSQL (配合 pgvector 插件)
- MyBatis-Plus (处理业务数据)
- LangChain4j (处理 RAG 和 Embedding)
- Spring MVC (基于 Servlet)

# Coding Standards (编码规范)
1. **MCP 协议实现**:
   - 遵循 "MCP over SSE" 标准。
   - Controller 层：`McpController` 必须包含 GET `/mcp/sse` (握手/长连接) 和 POST `/mcp/messages` (JSON-RPC 指令处理)。
   - 使用 `SseEmitter` 实现异步消息推送。
   - **禁止**在 MCP 工具执行时直接阻塞返回 HTTP 响应，必须通过 `emitters` Map 找到对应的 SSE 连接并将结果推送回去。

2. **架构模式**:
   - **策略模式 (Strategy Pattern)**: 所有 AI 工具必须实现 `McpTool` 接口。
   - **工具注册**: 利用 Spring 的自动装配，自动扫描所有实现 `McpTool` 的 `@Component` Bean，并将它们存入注册中心 Map。
   - **DTO**: 使用 Java `record` 定义 MCP 协议对象 (如 `JsonRpcRequest`, `JsonRpcResponse`)。

3. **数据访问**:
   - 使用 **MyBatis-Plus**。对于 PostgreSQL 的 `JSONB` 字段（用于存储动态数据），必须配置 `JacksonTypeHandler` 进行自动映射。
   - 使用 **LangChain4j** 的 Embedding Store 接口进行向量搜索操作。

4. **代码可读性与规范 (Code Readability)**:
   - **禁止全路径引用**: 除非存在类名冲突，否则严禁在代码中直接使用全路径引用类（如 `dev.langchain4j.model.chat.ChatLanguageModel`）。必须通过 `import` 语句引入类，保持代码简洁。
   - **导入优化**: 定期清理无用的 import，避免使用通配符导入（`import .*`）。

5. **系统边界约束**:
   - 本服务 (`ms-java-biz`) 处理企业核心业务逻辑与数据持久化。
   - 将底层业务能力封装为 MCP (Model Context Protocol) 标准接口，作为工具集供 AI 智能层调用。
   - **不要**在这里实现对话状态管理或复杂的 Agent 编排逻辑。

6. **测试规范 (Testing Standards)**:
   - **基础设施 Mock**: 禁止在单元测试/集成测试中直连 MongoDB。必须通过 `@TestConfiguration` 提供 Mock 的 `MongoTemplate` 豆 (Bean)，并 Mock `getConverter()` 以满足 Spring Data Repositories 的加载。
   - **依赖冲突防护**: 由于同时引用了 Spring Cloud Alibaba 和 LangChain4j BOM，当发现 `NoSuchMethodError` 时，必须通过 `mvn dependency:tree` 检查版本，并在 `pom.xml` 中显式指定高版本 SDK（通常是 DashScope SDK）。
   - **Context 优化**: 在 `application-test.yml` 中应排除不必要的自动配置（如 `MongoAutoConfiguration`），加快启动速度并减少环境依赖。
   - **切片测试 (Slice Testing)**: 针对 Controller 层测试，优先使用 `@WebMvcTest` 而非 `@SpringBootTest`。这可以避免加载不必要的业务配置类和基础设施 Bean，确保测试的精确性与高性能。

7. **数据库迁移 (Flyway)**:
   - **脚本不可变性**: 严禁修改已经应用（Applied）到数据库的 SQL 迁移脚本。任何变更必须通过新建版本号（如 `V1.2__...`）实现。
    - **Checksum 修复**: 开发环境下若因修改旧脚本导致 `FlywayValidateException`，应使用 `FlywayMigrationStrategy` 调用 `repair()` 同步校验和。

# Key Context (关键背景)
这是一个核心业务服务 (`ms-java-biz`)。它连接 Nacos 进行服务注册，提供具体的业务工具（如 `query_order` 查订单, `search_knowledge` 查知识库），并通过 MCP SSE 供 ms-py-agent 远程调用。