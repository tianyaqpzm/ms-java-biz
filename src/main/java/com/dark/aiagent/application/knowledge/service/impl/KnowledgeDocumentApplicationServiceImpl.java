package com.dark.aiagent.application.knowledge.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.dark.aiagent.application.knowledge.service.KnowledgeDocumentApplicationService;
import com.dark.aiagent.application.knowledge.client.PythonAgentClient;
import com.dark.aiagent.application.knowledge.dto.IngestDocumentRequest;
import com.dark.aiagent.application.knowledge.dto.IngestDocumentResponse;
import com.dark.aiagent.domain.knowledge.entity.KnowledgeDocument;
import com.dark.aiagent.domain.knowledge.repository.KnowledgeDocumentRepository;
import com.dark.aiagent.domain.knowledge.valueobject.KnowledgeConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;

@Service
public class KnowledgeDocumentApplicationServiceImpl implements KnowledgeDocumentApplicationService {

    @Value("${app.knowledge.upload-dir:/tmp/ai_knowledge_uploads}")
    private String uploadDir;

    private final KnowledgeDocumentRepository documentRepository;
    private final PythonAgentClient pythonAgentClient;
    private final ObjectMapper objectMapper;

    public KnowledgeDocumentApplicationServiceImpl(
        KnowledgeDocumentRepository documentRepository,
        PythonAgentClient pythonAgentClient,
        ObjectMapper objectMapper
    ) {
        this.documentRepository = documentRepository;
        this.pythonAgentClient = pythonAgentClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public KnowledgeDocument uploadDocument(String topicId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Upload file is empty");
        }

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        File destFile = new File(dir, uniqueFileName);

        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        String id = UUID.randomUUID().toString().replace("-", "");
        String title = originalFilename != null ? originalFilename : "Unnamed Document";
        
        // 使用充血模型实体构造方法
        KnowledgeDocument doc = new KnowledgeDocument(
            id, topicId, title, "Admin", destFile.getAbsolutePath(), null
        );

        documentRepository.save(doc);
        return doc;
    }

    @Override
    public Resource getDocumentResource(String documentId) {
        KnowledgeDocument doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));
            
        File file = new File(doc.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("Physical file not found on server");
        }
        return new FileSystemResource(file);
    }

    @Override
    public void deleteDocument(String documentId) {
        KnowledgeDocument doc = documentRepository.findById(documentId).orElse(null);
        if (doc != null) {
            if (doc.getFilePath() != null && !doc.getFilePath().trim().isEmpty()) {
                File file = new File(doc.getFilePath());
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }
            documentRepository.deleteById(documentId);
        }
    }

    @Override
    public java.util.List<KnowledgeDocument> getDocumentsByTopic(String topicId) {
        return documentRepository.findByTopicId(topicId);
    }

    @Override
    public Map<String, Object> initiateIngest(String documentId, Map<String, Object> ingestPayload) {
        KnowledgeDocument doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // 业界最佳实践：使用 ObjectMapper 自动映射扁平 Map 到嵌套领域对象
        // 这里我们先映射到一个平铺的 DTO，再构建领域模型，或者直接使用 objectMapper 的能力
        KnowledgeConfig config = convertToConfig(ingestPayload);

        // 充血模型：触发业务状态变化
        doc.assignConfig(config);
        doc.process(); // This method exists

        documentRepository.update(doc);

        IngestDocumentRequest request = new IngestDocumentRequest(
            doc.getFilePath() != null ? doc.getFilePath() : "mock/path.txt",
            doc.getTopicId(),
            "default",
            config.chunking().chunkSize(),
            config.chunking().chunkOverlap(),
            config.chunking().separators(),
            config.indexing().embeddingModel(),
            config.indexing().vectorStore(),
            config.retrieval().topK(),
            config.retrieval().scoreThreshold(),
            config.retrieval().enableHybridSearch(),
            config.retrieval().alphaWeight(),
            config.generation().generationModel(),
            config.generation().temperature(),
            config.generation().maxTokens(),
            config.generation().systemPrompt(),
            config.evaluation().enableEvaluation(),
            config.evaluation().evaluationMetrics()
        );
        
        try {
            IngestDocumentResponse response = pythonAgentClient.ingestDocument(request);
            doc.publish(); // 充血模型：文档发布完成
            documentRepository.update(doc);
            
            // 为了兼容旧的方法签名返回值
            Map<String, Object> result = new HashMap<>();
            result.put("status", response.status());
            result.put("message", response.message());
            result.put("data", response.data());
            return result;
        } catch (Exception e) {
            doc.markAsFailed(); // 充血模型：标记失败
            documentRepository.update(doc);
            throw new RuntimeException("Failed to call ms-py-agent: " + e.getMessage());
        }
    }
    /**
     * 将前端平铺的 Map 转换为结构化的领域对象
     */
    private KnowledgeConfig convertToConfig(Map<String, Object> payload) {
        try {
            // 步骤 1: 使用 Jackson 将 Map 转换为平铺的 DTO (复用 IngestDocumentRequest 的解析能力)
            // 注意：这里为了简化，我们直接从 Map 手动构建嵌套结构，但使用包装方法
            return new KnowledgeConfig(
                new KnowledgeConfig.ChunkingConfig(
                    (Integer) payload.get("chunkSize"),
                    (Integer) payload.get("chunkOverlap"),
                    (List<String>) payload.get("separators")
                ),
                new KnowledgeConfig.IndexingConfig(
                    (String) payload.get("embeddingModel"),
                    (String) payload.get("vectorStore")
                ),
                new KnowledgeConfig.RetrievalConfig(
                    (Integer) payload.get("topK"),
                    payload.get("scoreThreshold") != null ? Double.valueOf(payload.get("scoreThreshold").toString()) : null,
                    (Boolean) payload.get("enableHybridSearch"),
                    payload.get("alphaWeight") != null ? Double.valueOf(payload.get("alphaWeight").toString()) : null
                ),
                new KnowledgeConfig.GenerationConfig(
                    (String) payload.get("generationModel"),
                    payload.get("temperature") != null ? Double.valueOf(payload.get("temperature").toString()) : null,
                    (Integer) payload.get("maxTokens"),
                    (String) payload.get("systemPrompt")
                ),
                new KnowledgeConfig.EvaluationConfig(
                    (Boolean) payload.get("enableEvaluation"),
                    (List<String>) payload.get("evaluationMetrics")
                )
            );
        } catch (Exception e) {
            throw new RuntimeException("解析配置参数失败: " + e.getMessage());
        }
    }
}
