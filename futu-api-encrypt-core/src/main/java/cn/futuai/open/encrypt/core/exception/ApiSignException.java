package cn.futuai.open.encrypt.core.exception;

/**
 * api签名有效性异常
 * @author Jason Kung
 * @date 2024/06/07 14:55
 */
public class ApiSignException extends ApiBaseException {

    /**
     * 请求标识符
     */
    private String requestUri;
    /**
     * 时间戳
     */
    private String timestamp;
    /**
     * 对称加密密钥
     */
    private String aesKey;
    /**
     * 原始queryString
     */
    private String orgQueryString;
    /**
     * 原始body
     */
    private String orgBody;

    public ApiSignException(String requestUri, String timestamp, String aesKey, String orgQueryString, String orgBody) {
        super("api sign exception");
        this.requestUri = requestUri;
        this.timestamp = timestamp;
        this.aesKey = aesKey;
        this.orgQueryString = orgQueryString;
        this.orgBody = orgBody;
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

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getOrgQueryString() {
        return orgQueryString;
    }

    public void setOrgQueryString(String orgQueryString) {
        this.orgQueryString = orgQueryString;
    }

    public String getOrgBody() {
        return orgBody;
    }

    public void setOrgBody(String orgBody) {
        this.orgBody = orgBody;
    }
}
