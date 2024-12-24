package cn.futuai.open.encrypt.core.exception;

/**
 * api时间戳异常
 * @author Jason Kung
 * @date 2024/06/07 14:55
 */
public class ApiTimestampException extends ApiBaseException {

    /**
     * 请求标识符
     */
    private String requestUri;
    /**
     * 时间戳
     */
    private String timestamp;

    public ApiTimestampException(String requestUri, String timestamp) {
        super("api timestamp exception");
        this.requestUri = requestUri;
        this.timestamp = timestamp;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
