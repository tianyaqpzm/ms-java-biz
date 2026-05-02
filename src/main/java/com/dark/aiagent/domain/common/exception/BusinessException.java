package com.dark.aiagent.domain.common.exception;

/**
 * 业务异常：携带错误码和国际化消息。
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String errorMsg;

    public BusinessException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BusinessException(String errorCode, String errorMsg, Throwable cause) {
        super(errorMsg, cause);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }


    public String getmessage() {
        return super.getMessage();
    }
}
