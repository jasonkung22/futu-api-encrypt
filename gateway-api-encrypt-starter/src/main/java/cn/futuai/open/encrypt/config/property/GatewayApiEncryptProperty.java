package cn.futuai.open.encrypt.config.property;

import cn.futuai.open.encrypt.util.ApiEncryptUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * api加密参数
 * @author gyf
 * @date 2024/06/07 16:19
 */
@Data
@ConfigurationProperties(prefix = "spring.cloud.gateway.api-encrypt")
public class GatewayApiEncryptProperty {

    /**
     * 是否开启
     */
    private Boolean enabled = true;
    /**
     * RSA私钥
     */
    private String rsaPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI6VKvsus/Z0R3zvqre3gzClHJdsCVeKq89hZFFWbg5l3FNbiGZEEiuD1LL+USi5GCxeRK+xEOFTj7I/waRVb3x7V3J1N0q4nNWoZvRey0MTVaBkoHeB5tzn2ZOCBQRJnijXcO58ChcLXOTQId+zDiCBCom/A62gtH8isH4PYoZXAgMBAAECgYBpetrwNa223nDgcWFHRkCMZSmQr8D9fT37Th5rudfzWNG07RssJKGYhY9913xs9vl2IUsI+qH1P98nS9lSXE37mfOKFhfGZIUjAhMb7/w8hhuHpBXopVpUJZW0B46gfPOsrmvq+xiwlI02UYJ1ZOrfdfbvss/Gwtgrk4pMigL1OQJBANRm4mOUMwF+xUxeOLa2Aafke/iwdcxoV1k1gXmTH0B8wf08zDR7heW737YBEvsjyfEpjo7Y0kGSE5zmTWNnuKUCQQCr2XuZeJLqq6etq7IhboPAx8E2xgfOY/hgKPr9IvM8gYee628YhyOynIhOVFGxaf7dMH9eZ1P6jAbvsgm+mFZLAkEAyY0btJyTzg5q0G30aUTKy3OgRDvGfIJiqM+CHkiCdmIsfs5rhD3WsEqYHZBlX5T1cvgZQ+nxkrE4FUHhG7v31QJAYJZ9TNYjJTjTpt5A4V9/OAROCZ4mVw+DU3DVGR/ivJhFBMJpD80s+D/YsMXdoKzlraaLgCDtZ336jBByP6jZnwJBAIGUnbs7eRLcXzlbORdKC/EfkDYS2rrXLFvQhehT7Y8dKHLZfJElnrHB33Qd8R8WP0PsPU6D7EWNU2zVNK1EDxY=";

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

    @PostConstruct
    public void init() {
        ApiEncryptUtil.reloadRsaPrivateKey(rsaPrivateKey);
    }

    @Data
    public static class CheckModel {

        /**
         * 检查模式
         */
        private CheckModelEnum model = CheckModelEnum.WHITE_LIST;

        /**
         * 白名单
         */
        private List<String> whiteList = new ArrayList<>();

        /**
         * 黑名单
         */
        private List<String> blackList = new ArrayList<>();
    }

    public enum CheckModelEnum {
        /**
         * 白名单
         */
        WHITE_LIST,
        /**
         * 黑名单
         */
        BLACK_LIST
    }

    @Data
    public static class TimestampVerify {

        /**
         * 是否开启时间戳校验
         */
        private Boolean enable = true;
        /**
         * 时间戳有效秒数
         */
        private Long timestampValidSecond = 24 * 60 * 60L;
    }

    @Data
    public static class SignVerify {

        /**
         * 是否开启签名校验
         */
        private Boolean enable = true;
    }

    @Data
    public static class RequestDecrypt {

        /**
         * 是否开启响应解密
         */
        private Boolean enable = true;
        /**
         * 请求解密检查模式
         */
        private CheckModel checkModel = new CheckModel();
    }

    @Data
    public static class ResponseEncrypt {

        /**
         * 是否开启响应加密
         */
        private Boolean enable = true;
        /**
         * 请求解密检查模式
         */
        private CheckModel checkModel = new CheckModel();
    }
}
