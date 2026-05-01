package com.dark.aiagent.interfaces.common.handler;

import com.dark.aiagent.interfaces.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：捕获所有异常并返回标准 JSON。
 * 解决异常被转发到 /error 导致 Spring Security 拦截返回 403 的问题。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        String path = request.getRequestURI();
        
        log.error("【系统异常】Path: {}, traceId: {}, Reason: {}", path, traceId, e.getMessage(), e);

        ErrorResponse error = ErrorResponse.of(
            traceId,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            e.getMessage(),
            path
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 可以根据需要添加更多细分异常的处理，例如 BadSqlGrammarException 等。
     */
}
