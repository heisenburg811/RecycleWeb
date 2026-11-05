package com.recycle.common.exception;

import com.recycle.common.api.R;

/**
 * 业务异常：可预期的、需要把原因展示给用户的错误。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(R.BIZ_ERROR, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
