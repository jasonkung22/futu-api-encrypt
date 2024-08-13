package cn.futuai.open.encrypt.core.property;

import cn.futuai.open.encrypt.core.util.ApiEncryptUtil;
import cn.hutool.core.util.StrUtil;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * api加密参数
 * @author Jason Kung
 * @date 2024/06/07 16:19
 */
@Data
@Slf4j
public class BaseApiEncryptProperties {

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

    @PostConstruct
    public void init() {
        if (StrUtil.isBlank(rsaPrivateKey)) {
            log.error("futu-api-encrypt rsa private key is blank");
            System.exit(0);
            return;
        }
        ApiEncryptUtil.reloadRsaPrivateKey(rsaPrivateKey);
    }
}
