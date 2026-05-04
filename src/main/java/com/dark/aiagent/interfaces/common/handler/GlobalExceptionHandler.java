package com.dark.aiagent.interfaces.common.handler;

import com.dark.aiagent.interfaces.common.dto.ErrorResponse;
import com.dark.aiagent.domain.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        String path = request.getRequestURI();
        String errorCode = e.getErrorCode();
        
        // 业务异常消息通常已经过处理，直接使用，或尝试从 messageSource 进一步增强
        String message = messageSource.getMessage(errorCode, null, e.getMessage(), LocaleContextHolder.getLocale());
        
        log.warn("【业务异常】Path: {}, traceId: {}, errorCode: {}, message: {}", path, traceId, errorCode, message);

        ErrorResponse error = ErrorResponse.of(
            traceId,
            HttpStatus.BAD_REQUEST.value(),
            errorCode,
            message,
            path
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        String path = request.getRequestURI();
        
        // 默认错误码使用 status
        String errorCode = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        // 尝试国际化处理消息
        String message = messageSource.getMessage(errorCode, null, e.getMessage(), LocaleContextHolder.getLocale());
        
        log.error("【系统异常】Path: {}, traceId: {}, errorCode: {}, Reason: {}", path, traceId, errorCode, message, e);

        ErrorResponse error = ErrorResponse.of(
            traceId,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            errorCode,
            message,
            path
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 可以根据需要添加更多细分异常的处理，例如 BadSqlGrammarException 等。
     */
}
