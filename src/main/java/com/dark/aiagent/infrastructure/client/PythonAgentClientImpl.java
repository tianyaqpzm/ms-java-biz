package com.dark.aiagent.infrastructure.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.dark.aiagent.application.knowledge.client.PythonAgentClient;
import com.dark.aiagent.application.knowledge.dto.IngestDocumentRequest;
import com.dark.aiagent.application.knowledge.dto.IngestDocumentResponse;
import com.dark.aiagent.constant.RemoteApiConstants;
import com.dark.aiagent.domain.common.exception.BusinessException;
import com.dark.aiagent.infrastructure.common.dto.RemoteErrorResponse;
import com.dark.aiagent.sdk.client.pyagent.model.IngestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PythonAgentClientImpl implements PythonAgentClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public PythonAgentClientImpl(WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public IngestDocumentResponse ingestDocument(IngestDocumentRequest request) {
        String pythonUrl = "http://" + RemoteApiConstants.PythonAgent.SERVICE_NAME
                + RemoteApiConstants.PythonAgent.KNOWLEDGE_DOCUMENT_INGEST;

        // 将领域层 DTO 转换为生成的 OpenAPI 实体
        IngestRequest remoteRequest =
                new IngestRequest().filePath(request.filePath()).category(request.category())
                        .tenantId(request.tenantId()).chunkSize(request.chunkSize())
                        .chunkOverlap(request.chunkOverlap()).separators(request.separators())
                        .embeddingModel(request.embeddingModel()).vectorStore(request.vectorStore())
                        .topK(request.topK())
                        .scoreThreshold(request.scoreThreshold() != null
                                ? request.scoreThreshold().floatValue()
                                : null)
                        .enableHybridSearch(request.enableHybridSearch())
                        .alphaWeight(
                                request.alphaWeight() != null ? request.alphaWeight().floatValue()
                                        : null)
                        .generationModel(request.generationModel())
                        .temperature(
                                request.temperature() != null ? request.temperature().floatValue()
                                        : null)
                        .maxTokens(request.maxTokens()).systemPrompt(request.systemPrompt())
                        .enableEvaluation(request.enableEvaluation())
                        .evaluationMetrics(request.evaluationMetrics());

        try {
            return webClient.post().uri(pythonUrl).bodyValue(remoteRequest).retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class).flatMap(body -> {
                                try {
                                    RemoteErrorResponse errorBody =
                                            objectMapper.readValue(body, RemoteErrorResponse.class);
                                    log.error("ms-py-agent returned error: status={}, body={}",
                                            response.statusCode(), errorBody);
                                    String errorCode =
                                            errorBody.error_code() != null ? errorBody.error_code()
                                                    : String.valueOf(response.statusCode().value());
                                    String errorMsg =
                                            errorBody.error_msg() != null ? errorBody.error_msg()
                                                    : "Remote call failed";
                                    return Mono.error(new BusinessException(errorCode, errorMsg));
                                } catch (Exception e) {
                                    log.error(
                                            "ms-py-agent returned non-JSON error: status={}, body={}",
                                            response.statusCode(), body);
                                    String msg = body != null && body.length() > 200
                                            ? body.substring(0, 200) + "..."
                                            : body;
                                    return Mono.error(new BusinessException(
                                            String.valueOf(response.statusCode().value()), msg));
                                }
                            }).cast(Throwable.class)
                                    .switchIfEmpty(Mono.error(new BusinessException(
                                            String.valueOf(response.statusCode().value()),
                                            "Remote call failed with no body"))))
                    .bodyToMono(IngestDocumentResponse.class).block();
        } catch (Exception e) {
            log.error("Failed to call ms-py-agent via WebClient: {}", e.getMessage());
            throw new BusinessException("DEP_0500", "调用 Python Agent 失败: " + e.getMessage());
        }
    }
}
