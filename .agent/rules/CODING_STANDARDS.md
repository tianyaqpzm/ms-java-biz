---
trigger: always_on
---

# Java 领域模型 (DDD) 编程规范

## 1. 领域对象纯洁性 (Purity)

* 严禁框架侵入： Domain 层的 Entity（实体）和 Value Object（值对象）必须是纯 POJO。严禁在领域层中引入 javax.persistence.*、jakarta.persistence.* (如 @Entity, @Table) 或 Spring 的核心注解。数据持久化映射必须在 Infrastructure 层的 ORM 实体中进行处理。
* 严禁反向依赖： 领域层不得依赖 Service、Controller 或 Repository 的实现逻辑。

## 2. 充血模型 (Rich Domain Model)

* 禁止贫血模型： 严禁生成只有 Getter/Setter 的实体类。
* 行为封装： 状态的改变必须通过具有业务语义的方法进行（如 user.changePassword(newPwd)），严禁直接暴露 user.setPassword() 供外部随意修改。
* 私有无参构造： 必须提供私有的无参构造函数（为了反射兼容），公开的实例化必须通过包含所有必填属性的构造函数或工厂方法（Factory / Builder）进行。

## 3. 实体 (Entity) 与 值对象 (Value Object)
* Entity： 必须包含唯一标识符（ID）。equals() 和 hashCode() 只能且必须基于 ID 字段进行重写。
* Value Object： 必须是不可变对象 (Immutable)。在 Java 14+ 环境中优先使用 record 关键字。如果使用类，则字段必须为 final，且不提供 Setter。equals() 和 hashCode() 必须基于所有属性字段重写。

## 4. 微服务间通信与防腐隔离 (Inter-Service Communication)

* 防腐层 (ACL)： 应用服务 (Application Service) 严禁直接依赖 HTTP 客户端（如 RestTemplate）和 HashMap 进行外部请求。必须在应用层定义接口 (Port)，并在基础设施层提供具体实现 (Adapter)。
* 明确 DTO： 对于外部调用的入参和出参，必须定义明确的 Request/Response DTO（如 Java Record），严禁使用泛化的 Map<String, Object> 进行无类型的数据传递。
* URL 集中管理： 严禁在代码逻辑中硬编码其他微服务的 URL 或 Service Name。必须提取到专门的常量类中（如 RemoteApiConstants.java 或 UrlConfig.java），并按业界通用的“内部静态类”结构按微服务进行分组管理。

## 5. 国际化与本地化 (i18n & Localization)

* 资源隔离： 所有面向用户或前端的提示语、异常信息、日志预警必须进行国际化处理，严禁在 Java 代码中直接硬编码中文字符串。
* 文件规范： 必须在 `src/main/resources/i18n` 目录下维护资源文件，且必须至少包含 `messages.properties`（默认/兜底）、`messages_zh_CN.properties`（中文）和 `messages_en_US.properties`（英文）。
* 配置要求： 在 `application.yaml` 中配置 `spring.messages.basename=i18n/messages` 且必须指定 `encoding: UTF-8` 以防止中文乱码。
* 状态机/常量： 业务状态机（如"已发布", "未入库"等）在持久化层和传输中应使用 Enum 英文枚举名或纯数字代码，仅在接口呈现（Controller层或前端视图）时映射为对应的 i18n 文本。

## 6. 数据访问与持久化规范 (Data Access & Persistence)

* **SQL 治理**： 严禁在 Java 注解（如 `@Select`, `@Update`）中编写超过 5 行或包含复杂逻辑（如多层子查询、复杂聚合）的原生 SQL。此类 SQL 必须移至 MyBatis XML 映射文件中，或通过数据库视图（View）进行封装。
* **读写分离与汇总表 (Summary Table)**： 针对大表（如千万级聊天记录）的聚合查询（如计算会话列表、统计总额），严禁在读取时进行实时 `GROUP BY` 或相关子查询。必须建立物理汇总表（如 `chat_sessions`），通过冗余关键字段换取查询性能。
* **自动化同步 (Trigger/CDC)**： 在多语言、多服务共享数据库的环境下，汇总表的数据同步应优先考虑数据库触发器 (Trigger) 或变更数据捕获 (CDC) 机制，以实现底层解耦，确保数据最终一致性。
* **索引优化**： 所有外键字段及高频查询条件字段必须建立索引。针对大表的查询必须确保通过执行计划（Explain）验证，严禁出现全表扫描。

## 7. 测试驱动开发与质量保证 (TDD & Quality Assurance)

* **TDD 原则**： 强烈建议在编写核心业务逻辑之前，先编写相应的测试用例（Test-Driven Development）。
* **100% 测试覆盖率**： 所有新增的功能模块和核心业务代码，必须满足关键代码（如 Use Case, Domain Entity）的 **100% 测试覆盖率**。未经充分测试的代码严禁合并入主分支。