package cn.futuai.open.encrypt.core.property;

import cn.futuai.open.encrypt.core.constants.enums.ApiEncryptLogLevelEnum;
import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.hutool.core.util.StrUtil;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * api加密参数
 * @author Jason Kung
 * @date 2024/06/07 16:19
 */
public class BaseApiEncryptProperties {

    private static final Logger log = LoggerFactory.getLogger(BaseApiEncryptProperties.class);
    /**
     * 是否开启
     */
    private Boolean enabled = true;
    /**
     * RSA私钥
     */
    private String rsaPrivateKey = "";

    /**
     * 检查模式
     */
    private CheckModel checkModel = new CheckModel();

    /**
     * 加密aes密钥header key
     */
    private String encryptAesKeyHeaderKey = "ek";

    /**
     * 时间戳header key
     */
    private String timestampHeaderKey = "ts";

    /**
     * 签名header key
     */
    private String signHeaderKey = "sign";

    /**
     * 加密参数key
     */
    private String encryptParamKey = "ciphertext";

    /**
     * 时间戳
     */
    private TimestampVerify timestamp = new TimestampVerify();

    /**
     * 签名
     */
    private SignVerify sign = new SignVerify();

    /**
     * 请求解密
     */
    private RequestDecrypt requestDecrypt = new RequestDecrypt();
    /**
     * 响应加密
     */
    private ResponseEncrypt responseEncrypt = new ResponseEncrypt();
    /**
     * 日志级别
     */
    private Map<String, ApiEncryptLogLevelEnum> logLevel = new HashMap<>();

    @PostConstruct
    public void init() {
        if (StrUtil.isBlank(rsaPrivateKey)) {
            log.error("futu-api-encrypt rsa private key is blank");
            System.exit(0);
            return;
        }
        ApiEncryptUtil.reloadRsaPrivateKey(rsaPrivateKey);
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    public CheckModel getCheckModel() {
        return checkModel;
    }

    public void setCheckModel(CheckModel checkModel) {
        this.checkModel = checkModel;
    }

    public String getEncryptAesKeyHeaderKey() {
        return encryptAesKeyHeaderKey;
    }

    public void setEncryptAesKeyHeaderKey(String encryptAesKeyHeaderKey) {
        this.encryptAesKeyHeaderKey = encryptAesKeyHeaderKey;
    }

    public String getTimestampHeaderKey() {
        return timestampHeaderKey;
    }

    public void setTimestampHeaderKey(String timestampHeaderKey) {
        this.timestampHeaderKey = timestampHeaderKey;
    }

    public String getSignHeaderKey() {
        return signHeaderKey;
    }

    public void setSignHeaderKey(String signHeaderKey) {
        this.signHeaderKey = signHeaderKey;
    }

    public String getEncryptParamKey() {
        return encryptParamKey;
    }

    public void setEncryptParamKey(String encryptParamKey) {
        this.encryptParamKey = encryptParamKey;
    }

    public TimestampVerify getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(TimestampVerify timestamp) {
        this.timestamp = timestamp;
    }

    public SignVerify getSign() {
        return sign;
    }

    public void setSign(SignVerify sign) {
        this.sign = sign;
    }

    public RequestDecrypt getRequestDecrypt() {
        return requestDecrypt;
    }

    public void setRequestDecrypt(RequestDecrypt requestDecrypt) {
        this.requestDecrypt = requestDecrypt;
    }

    public ResponseEncrypt getResponseEncrypt() {
        return responseEncrypt;
    }

    public void setResponseEncrypt(ResponseEncrypt responseEncrypt) {
        this.responseEncrypt = responseEncrypt;
    }

    public Map<String, ApiEncryptLogLevelEnum> getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Map<String, ApiEncryptLogLevelEnum> logLevel) {
        this.logLevel = logLevel;
    }
}
