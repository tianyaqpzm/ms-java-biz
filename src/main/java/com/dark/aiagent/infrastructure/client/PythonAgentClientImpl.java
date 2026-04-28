package com.dark.aiagent.infrastructure.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.dark.aiagent.application.knowledge.client.PythonAgentClient;
import com.dark.aiagent.application.knowledge.dto.IngestDocumentRequest;
import com.dark.aiagent.application.knowledge.dto.IngestDocumentResponse;
import com.dark.aiagent.constant.RemoteApiConstants;

import java.util.List;

@Component
public class PythonAgentClientImpl implements PythonAgentClient {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public PythonAgentClientImpl(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public IngestDocumentResponse ingestDocument(IngestDocumentRequest request) {
        List<ServiceInstance> instances = discoveryClient.getInstances(RemoteApiConstants.PythonAgent.SERVICE_NAME);
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("Service " + RemoteApiConstants.PythonAgent.SERVICE_NAME + " not found in Nacos");
        }
        
        String pythonUrl = instances.get(0).getUri().toString() + RemoteApiConstants.PythonAgent.KNOWLEDGE_DOCUMENT_INGEST;
        
        try {
            return restTemplate.postForObject(pythonUrl, request, IngestDocumentResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call ms-py-agent: " + e.getMessage(), e);
        }
    }
}
