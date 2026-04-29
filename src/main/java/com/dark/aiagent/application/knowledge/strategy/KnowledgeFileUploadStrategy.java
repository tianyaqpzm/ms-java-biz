package com.dark.aiagent.application.knowledge.strategy;

import com.dark.aiagent.module.common.upload.FileUploadStrategy;
import com.dark.aiagent.application.knowledge.service.KnowledgeDocumentApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
public class KnowledgeFileUploadStrategy implements FileUploadStrategy {

    @Autowired
    private KnowledgeDocumentApplicationService documentService;

    @Override
    public String getUploadType() {
        return "knowledge";
    }

    @Override
    public Object handleUpload(MultipartFile file, Map<String, Object> extraParams) {
        String topicId = (String) extraParams.get("topicId");
        if (topicId == null || topicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Uploading knowledge document requires 'topicId' parameter.");
        }
        
        // Delegate to existing physical storage and DB logic
        return documentService.uploadDocument(topicId, file);
    }
}
