package io.dtonic.dhubingestmodule.common.exception;

import io.dtonic.dhubingestmodule.common.code.DataCoreUiCode.ErrorCode;

/**
 * ResourceNotFoundException 에러 정의 클래스
 */
public class ResourceNotFoundException extends BaseException {

    private static final long serialVersionUID = 325647261102179280L;

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String msg) {
        super(errorCode, msg);
        this.errorCode = errorCode;
    }

    public ResourceNotFoundException(ErrorCode errorCode, Throwable throwable) {
        super(errorCode, throwable);
        this.errorCode = errorCode;
    }

    public ResourceNotFoundException(ErrorCode errorCode, String msg, Throwable throwable) {
        super(errorCode, msg, throwable);
        this.errorCode = errorCode;
    }
}
