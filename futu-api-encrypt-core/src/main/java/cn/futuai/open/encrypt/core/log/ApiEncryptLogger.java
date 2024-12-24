package cn.futuai.open.encrypt.core.log;

/**
 * api 加密logger
 * @author Jason Kung
 * @date 2024/12/24 10:22
 */
public interface ApiEncryptLogger {

    /**
     * 记录异常日志
     * @param ex 异常
     */
    void logException(Throwable ex);
}
