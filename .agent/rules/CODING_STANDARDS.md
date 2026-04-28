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