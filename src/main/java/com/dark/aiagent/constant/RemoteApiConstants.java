package com.dark.aiagent.constant;

/**
 * 远程微服务 API 接口路径常量配置
 * <p>
 * 统一管理所有跨微服务调用的接口路径，避免硬编码，方便后续统一查找和修改接口依赖。
 * 业界通用格式：按照目标服务名称进行分类 (内部静态类)。
 * </p>
 */
public final class RemoteApiConstants {

    private RemoteApiConstants() {
        // 私有化构造器，防止实例化
    }

    /**
     * Python Agent 服务 (ms-py-agent) 相关接口
     */
    public static final class PythonAgent {
        
        /**
         * 服务名，用于 Nacos 服务发现
         */
        public static final String SERVICE_NAME = "ms-py-agent";

        /**
         * 知识库文档分块入库接口
         */
        public static final String KNOWLEDGE_DOCUMENT_INGEST = "/rest/kb/v1/documents/ingest";
        
    }
    
    // 后续可以继续追加其他微服务...
    // public static final class Gateway { ... }
}
