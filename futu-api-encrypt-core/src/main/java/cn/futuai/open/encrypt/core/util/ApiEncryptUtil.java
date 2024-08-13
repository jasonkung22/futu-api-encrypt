package cn.futuai.open.encrypt.core.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * 加解密接口实现
 * @author Jason Kung
 * @date 2023/11/07 11:27
 */
@Slf4j
public class ApiEncryptUtil {

    private final static Digester SHA256 = new Digester(DigestAlgorithm.SHA256);

    private static RSA RSA = null;

    public static void reloadRsaPrivateKey(String privateKey) {
        RSA = new RSA(privateKey, null);
    }

    public static String aesDecrypt(String encryptData, String key) {
        return SecureUtil.aes(key.getBytes()).decryptStr(encryptData);
    }

    public static String aesEncrypt(String data, String key) {
        return SecureUtil.aes(key.getBytes()).encryptBase64(data);
    }

    public static String sign(String data) {
        return SHA256.digestHex(data);
    }

    public static String sign(String timestamp, String encryptKey, String queryString, String body) {
        StringBuilder param = new StringBuilder();
        if (StrUtil.isNotBlank(queryString)) {
            param.append(queryString);
        }
        param.append(timestamp);
        param.append(encryptKey);
        if (StrUtil.isNotBlank(body)) {
            param.append(body);
        }
        return sign(param.toString());
    }

    @SuppressWarnings("unused")
    public static String rsaEncrypt(String data) {
        return RSA.encryptBase64(data, KeyType.PrivateKey);
    }

    public static String rsaDecrypt(String data) {
        return RSA.decryptStr(data, KeyType.PrivateKey);
    }

    public static Boolean verifySign(String timestamp, String encryptKey, String queryString, String body,
            String sign) {
        return Objects.equals(sign(timestamp, encryptKey, queryString, body), sign);
    }
}
