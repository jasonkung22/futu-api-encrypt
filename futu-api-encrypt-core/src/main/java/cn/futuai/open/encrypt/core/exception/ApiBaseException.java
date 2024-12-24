package cn.futuai.open.encrypt.core.exception;

/**
 * api加密异常
 * @author Jason Kung
 * @date 2024/06/07 14:55
 */
public class ApiBaseException extends RuntimeException {

    public ApiBaseException() {
    }

    public ApiBaseException(String message) {
        super(message);
    }

    public ApiBaseException(String message, Throwable e) {
        super(message, e);
    }
}
