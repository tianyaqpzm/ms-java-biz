package com.dark.aiagent.module.knowledge.entity;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeConfig {
    private Integer chunkSize;
    private Integer chunkOverlap;
    private List<String> separators;
    
    private String embeddingModel;
    private String vectorStore;
    
    // Retrieval Params
    private Integer topK;
    private Double scoreThreshold;
    private Boolean enableHybridSearch;
    private Double alphaWeight;
}
