package com.dark.aiagent.module.knowledge.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dark.aiagent.module.knowledge.entity.KnowledgeConfig;
import com.dark.aiagent.module.knowledge.entity.KnowledgeDocument;
import com.dark.aiagent.module.knowledge.mapper.KnowledgeDocumentMapper;
import com.dark.aiagent.module.knowledge.service.KnowledgeDocumentService;

@Service
public class KnowledgeDocumentServiceImpl
        extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument>
        implements KnowledgeDocumentService {

    @Value("${app.knowledge.upload-dir:/tmp/ai_knowledge_uploads}")
    private String uploadDir;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public KnowledgeDocument uploadDocument(String topicId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Upload file is empty");
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

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(UUID.randomUUID().toString().replace("-", ""));
        doc.setTopicId(topicId);
        doc.setTitle(originalFilename != null ? originalFilename : "Unnamed Document");
        doc.setStatus("未处理");
        doc.setAuthor("Admin"); // default author, could be fetched from JWT
        doc.setFilePath(destFile.getAbsolutePath());

        this.save(doc);
        return doc;
    }

    @Override
    public Resource getDocumentResource(String documentId) {
        KnowledgeDocument doc = this.getById(documentId);
        if (doc == null || doc.getFilePath() == null) {
            throw new RuntimeException("Document or file path not found");
        }
        File file = new File(doc.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("Physical file not found on server");
        }
        return new FileSystemResource(file);
    }

    @Override
    public void deleteDocument(String documentId) {
        KnowledgeDocument doc = this.getById(documentId);
        if (doc != null) {
            if (doc.getFilePath() != null && !doc.getFilePath().trim().isEmpty()) {
                File file = new File(doc.getFilePath());
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }
            this.removeById(documentId);
        }
    }

    @Override
    public Map<String, Object> initiateIngest(String documentId,
            Map<String, Object> ingestPayload) {
        KnowledgeDocument doc = this.getById(documentId);
        if (doc == null) {
            throw new RuntimeException("Document not found");
        }

        // Save config
        KnowledgeConfig config = new KnowledgeConfig();
        if (ingestPayload.containsKey("chunkSize"))
            config.setChunkSize((Integer) ingestPayload.get("chunkSize"));
        if (ingestPayload.containsKey("chunkOverlap"))
            config.setChunkOverlap((Integer) ingestPayload.get("chunkOverlap"));
        // TODO: Map other properties if needed
        doc.setConfigJson(config);
        doc.setStatus("进行中");
        this.updateById(doc);

        // Prepare Request for Python Agent
        Map<String, Object> pythonReq = new HashMap<>();
        pythonReq.put("file_path", doc.getFilePath() != null ? doc.getFilePath() : "mock/path.txt"); // Provide
                                                                                                     // a
                                                                                                     // mock
                                                                                                     // path
                                                                                                     // if
                                                                                                     // null
        pythonReq.put("category", doc.getTopicId());
        pythonReq.put("tenant_id", "default");
        if (config.getChunkSize() != null)
            pythonReq.put("chunk_size", config.getChunkSize());
        if (config.getChunkOverlap() != null)
            pythonReq.put("chunk_overlap", config.getChunkOverlap());

        // Call Python Agent (assuming it's at localhost:8181 or dynamically resolved)
        String pythonUrl = "http://localhost:8181/rest/kb/v1/documents/ingest";
        try {
            Map<String, Object> response =
                    restTemplate.postForObject(pythonUrl, pythonReq, Map.class);
            doc.setStatus("已完成");
            this.updateById(doc);
            return response;
        } catch (Exception e) {
            doc.setStatus("处理失败");
            this.updateById(doc);
            throw new RuntimeException("Failed to call Python Agent: " + e.getMessage());
        }
    }
}
