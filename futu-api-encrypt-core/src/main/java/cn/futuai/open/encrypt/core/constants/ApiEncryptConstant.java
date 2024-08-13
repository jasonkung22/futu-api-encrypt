package cn.futuai.open.encrypt.core.constants;

/**
 * API加密常量
 * @author Jason Kung
 * @date 2024/08/08 14:41
 */
public interface ApiEncryptConstant {

    /**
     * Aes密钥
     */
    String AES_KEY = "aesKey";
    /**
     * 签名
     */
    String SIGN = "sign";
    /**
     * 时间戳
     */
    String TIMES_TAMP = "timestamp";
    /**
     * 原始请求参数
     */
    String ORG_QUERY_STRING = "orgQueryString";
    /**
     * 原始请求体
     */
    String ORG_BODY = "orgBody";
}
