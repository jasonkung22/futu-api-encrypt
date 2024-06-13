package cn.futuai.open.encrypt.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import java.nio.charset.StandardCharsets;
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
        try {
            return SecureUtil.aes(key.getBytes()).decryptStr(encryptData);
        } catch (Exception e) {
            log.error("对称解密失败,encryptData:{}, key:{}", encryptData, key, e);
            return null;
        }
    }


    public static String aesEncrypt(String data, String key) {
        try {
            return SecureUtil.aes(key.getBytes()).encryptBase64(data);
        } catch (Exception e) {
            log.error("对称解密失败,encryptData:{}, key:{}", data, key, e);
            return null;
        }
    }


    public static String sign(String data) {
        return SHA256.digestHex(data);
    }


    public static boolean verifySign(String data, String sign) {
        return Objects.equals(SHA256.digestHex(data), sign);
    }

    public static String rsaEncrypt(String data) {
        return RSA.encryptBase64(data, KeyType.PrivateKey);
    }

    public static String rsaDecrypt(String data) {
        return RSA.decryptStr(data, KeyType.PrivateKey);
    }

    public static Boolean verifySign(String timestamp, String encryptKey, String queryString, String body,
            String sign) {
        StringBuilder param = new StringBuilder();
        if (StrUtil.isNotBlank(queryString)) {
            param.append(queryString);
        }
        param.append(timestamp);
        param.append(encryptKey);
        if (StrUtil.isNotBlank(body)) {
            param.append(body);
        }
        return verifySign(param.toString(), sign);
    }

    public static void main(String[] args) {
        System.out.println(new RSA(null,
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOlSr7LrP2dEd876q3t4MwpRyXbAlXiqvPYWRRVm4OZdxTW4hmRBIrg9Sy/lEouRgsXkSvsRDhU4+yP8GkVW98e1dydTdKuJzVqGb0XstDE1WgZKB3gebc59mTggUESZ4o13DufAoXC1zk0CHfsw4ggQqJvwOtoLR/IrB+D2KGVwIDAQAB").encryptBase64(
                "TRTJYixbclBmiRnbmVbrWS50bT3FoCIw".getBytes(StandardCharsets.UTF_8), KeyType.PublicKey));
        System.out.println(rsaDecrypt(
                "feIBBw7JVBFQRIYN8AtBIVvVC/yW2nwEnm3edhreXRrDtx2+8qYRPkNlXgPU0Ga58HkbGAyuaJDaM+cOKb2Owtje3dfSOlPLLbbGUJCkkcbtbwXUuPSjW6UYfRhBuadpIk539kv/wgpWlO5Xei/T40+hJ07sqCrfkAm9oNtKEcc="));
        System.out.println(aesEncrypt("test1=1&test2=2&test3=3", "TRTJYixbclBmiRnbmVbrWS50bT3FoCIw"));
        System.out.println(aesEncrypt("{\"test123\":123}", "TRTJYixbclBmiRnbmVbrWS50bT3FoCIw"));

        System.out.println(verifySign("1718071578000", "TRTJYixbclBmiRnbmVbrWS50bT3FoCIw",
                "test1=1&test3=3&test2=2", "{\"test123\":123}",
                "e71c90b34e063cb2d647056fd7c994346b19f2522d68d04002996efb868208a3"));
    }
}
