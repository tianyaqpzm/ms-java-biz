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

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;
import java.util.List;

@Service
public class KnowledgeDocumentApplicationServiceImpl implements KnowledgeDocumentApplicationService {

    @Value("${app.knowledge.upload-dir:/tmp/ai_knowledge_uploads}")
    private String uploadDir;

    private final KnowledgeDocumentRepository documentRepository;
    private final PythonAgentClient pythonAgentClient;

    public KnowledgeDocumentApplicationServiceImpl(KnowledgeDocumentRepository documentRepository, PythonAgentClient pythonAgentClient) {
        this.documentRepository = documentRepository;
        this.pythonAgentClient = pythonAgentClient;
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

        // 构建不可变 Value Object
        KnowledgeConfig config = new KnowledgeConfig(
            ingestPayload.containsKey("chunkSize") ? (Integer) ingestPayload.get("chunkSize") : null,
            ingestPayload.containsKey("chunkOverlap") ? (Integer) ingestPayload.get("chunkOverlap") : null,
            null, null, null, null, null, null, null
        );

        // 充血模型：触发业务状态变化
        doc.assignConfig(config);
        doc.process(); // This method exists

        documentRepository.update(doc);

        IngestDocumentRequest request = new IngestDocumentRequest(
            doc.getFilePath() != null ? doc.getFilePath() : "mock/path.txt",
            doc.getTopicId(),
            "default",
            config.chunkSize(),
            config.chunkOverlap()
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
}
