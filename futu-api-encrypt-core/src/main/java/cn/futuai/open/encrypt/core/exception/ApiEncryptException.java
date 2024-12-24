package cn.futuai.open.encrypt.core.exception;

/**
 * api加密异常
 * @author Jason Kung
 * @date 2024/06/07 14:55
 */
public class ApiEncryptException extends ApiBaseException {

    /**
     * 请求标识符
     */
    private String requestUri;
    /**
     * 参数
     */
    private String param;
    /**
     * 对称密钥
     */
    private String aesKey;

    public ApiEncryptException(String requestUri, String param, String aesKey, Throwable e) {
        super("api encrypt exception", e);
        this.requestUri = requestUri;
        this.param = param;
        this.aesKey = aesKey;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}
