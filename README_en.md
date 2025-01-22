[中文](README.md) | English

# I. Overview

futu-api-encrypt is an API encryption/decryption starter based on spring-boot and spring-cloud-gateway. Through simple
spring-boot configuration, it can implement interface parameter validation and encryption/decryption in various business
scenarios. It includes features such as **timestamp** validation, **signature** verification, **symmetric encryption**
, **asymmetric encryption**, and can filter interface addresses based on **whitelist** and **blacklist**. It also
supports **Tolerant Mode** for flexible handling of both encrypted and non-encrypted requests.

# II. Quick Start

> Based on spring-cloud-gateway (similar for spring-boot)

### 1. Add Dependency

```xml

<dependency>
    <groupId>cn.futuai.open</groupId>
    <artifactId>futu-api-encrypt-spring-cloud-gateway-starter</artifactId>
    <version>1.3.1</version>
</dependency>
```

### 2. Configuration Parameters

```yaml
spring:
  cloud:
    gateway:
      api-encrypt:
        # Global switch
        enabled: true
        # Encryption symmetric key http header key
        encrypt-aes-key-header-key: "ek"
        # Timestamp http header key
        timestamp-header-key: "ts"
        # Signature value http header key
        sign-header-key: "sign"
        # Encrypted query parameter key
        encrypt-param-key: "ciphertext"
        # RSA asymmetric encryption private key
        rsa-private-key: "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI6VKvsus/Z0R3zvqre3gzClHJdsCVeKq89hZFFWbg5l3FNbiGZEEiuD1LL+USi5GCxeRK+xEOFTj7I/waRVb3x7V3J1N0q4nNWoZvRey0MTVaBkoHeB5tzn2ZOCBQRJnijXcO58ChcLXOTQId+zDiCBCom/A62gtH8isH4PYoZXAgMBAAECgYBpetrwNa223nDgcWFHRkCMZSmQr8D9fT37Th5rudfzWNG07RssJKGYhY9913xs9vl2IUsI+qH1P98nS9lSXE37mfOKFhfGZIUjAhMb7/w8hhuHpBXopVpUJZW0B46gfPOsrmvq+xiwlI02UYJ1ZOrfdfbvss/Gwtgrk4pMigL1OQJBANRm4mOUMwF+xUxeOLa2Aafke/iwdcxoV1k1gXmTH0B8wf08zDR7heW737YBEvsjyfEpjo7Y0kGSE5zmTWNnuKUCQQCr2XuZeJLqq6etq7IhboPAx8E2xgfOY/hgKPr9IvM8gYee628YhyOynIhOVFGxaf7dMH9eZ1P6jAbvsgm+mFZLAkEAyY0btJyTzg5q0G30aUTKy3OgRDvGfIJiqM+CHkiCdmIsfs5rhD3WsEqYHZBlX5T1cvgZQ+nxkrE4FUHhG7v31QJAYJZ9TNYjJTjTpt5A4V9/OAROCZ4mVw+DU3DVGR/ivJhFBMJpD80s+D/YsMXdoKzlraaLgCDtZ336jBByP6jZnwJBAIGUnbs7eRLcXzlbORdKC/EfkDYS2rrXLFvQhehT7Y8dKHLZfJElnrHB33Qd8R8WP0PsPU6D7EWNU2zVNK1EDxY="
        # Tolerant mode configuration
        tolerant-urls:
          - /api/public/**  # Allow public APIs to support both encrypted and non-encrypted access
          - /api/legacy/**  # Compatible with legacy versions
        log-level:
          "default": warn
          "[/api/user/code]": info
          "[/api/user/code2]": debug
          "[/api/user/code3]": warn
          "[/api/user/code4]": error
          "[/api/user/export]": trace
        # Detection mode
        check-model:
          # Mode
          model: black_list
          # Blacklist URLs
          black-list:
            - /api/user/code
            - /api/user/code2
            - /api/user/code3
            - /api/user/code4
            - /api/user/export
        timestamp:
          # Timestamp validation switch
          enabled: true
          # Timestamp valid seconds
          timestamp-valid-second: 31536000 # 1 year
        sign:
          # Signature validation switch
          enabled: true
        # Request decryption configuration
        request-decrypt:
          # Enable switch
          enabled: true
          # Detection mode
          check-model:
            # Whitelist mode
            model: white_list
            # Whitelist URLs
            white-list:
              - /api/user/code2
              - /api/user/code4
        # Response encryption configuration
        response-encrypt:
          # Enable switch
          enabled: true
          # Detection mode
          check-model:
            # Blacklist mode
            model: black_list
            # Blacklist URLs
            black-list:
              - /api/user/code
              - /api/user/code4
              - /api/user/export
```

### 3. Custom API Exception Callback Manager

```java

@Configuration
public class ApiEncryptConfiguration {

    public ApiEncryptConfiguration() {
        // Gateway callback manager
        GatewayApiExceptionCallbackManager.setApiExceptionHandler(new GatewayApiExceptionRequestHandler() {
            /**
             * Called when gateway API validation fails
             * @param serverWebExchange serverWebExchange
             * @param throwable exception
             * @return mono
             */
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange,
                    Throwable throwable) {
                Response<Void> error = Response.create(ResponseTypeEnum.PARAM_VALID_ERROR);
                String errJson = JSONUtil.toJsonStr(error);
                return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(errJson), String.class);
            }
        });
    }
}
```

### 4. Tolerant Mode Description

Tolerant Mode is a special feature that allows specific API endpoints to support both encrypted and non-encrypted
requests simultaneously. This is particularly useful in the following scenarios:

1. Smooth API Upgrade: When upgrading existing non-encrypted APIs to encrypted ones, tolerant mode enables a smooth
   transition
2. Public APIs: Some public APIs may need to support both encrypted access (e.g., mobile clients) and non-encrypted
   access (e.g., web clients)
3. Backward Compatibility: To maintain compatibility with legacy clients, APIs can support both old and new access
   methods

Usage:

1. Configure URLs that need tolerant mode through `tolerant-urls` in the configuration file
2. Supports wildcard matching, e.g., `/api/public/**`
3. When a request matches a tolerant mode URL:
    - If the request includes encryption header (encrypt-aes-key-header-key), it's processed as an encrypted request
    - If the request doesn't include encryption header, it's processed as a non-encrypted request
    - Timestamp and signature validation (if enabled) are performed regardless of encryption

# III. Examples

### 1. Configure Gateway Service and User Service

#### Gateway Service:

- Import futu-api-encrypt-spring-cloud-gateway-starter and nacos dependencies
- Configure API encryption rules as per "Quick Start"

#### User Service

- Import nacos and other dependencies
- Define API interface

```java

@Slf4j
@RestController
public class UserController {

    @PostMapping("{path}")
    public Map<String, Object> code(@PathVariable("path") String path, @RequestParam String test1,
            @RequestParam String test2,
            @RequestParam String test3,
            @RequestBody String body) {
        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        map.put("test1", test1);
        map.put("test2", test2);
        map.put("test3", test3);
        map.put("body", body);
        log.info(String.valueOf(map));
        return map;
    }

    @SneakyThrows
    @GetMapping("export")
    public void export(HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        String name = "Test File";

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", UriUtils.encode(name, StandardCharsets.UTF_8) + ".xlsx");

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            response.setHeader(entry.getKey(), String.join(";", entry.getValue()));
        }

        IOUtils.copy(new FileInputStream("D:\\project\\personal\\futu-api-encrypt\\example\\doc\\测试.xlsx"),
                response.getOutputStream());
    }
}
```

### 2. Make Requests

#### Request Parameters Encrypted, Response Results Encrypted, Validate Timestamp and Signature

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1723463068571-469cbc39-1237-43d4-bdaa-e6495fd6efad.png#averageHue=%23fdfcfc&clientId=u0f345f74-d927-4&from=paste&height=618&id=ud0b94ff5&originHeight=618&originWidth=865&originalType=binary&ratio=1&rotation=0&showTitle=false&size=40216&status=done&style=none&taskId=u55edca16-0e37-4bf6-8a05-2c2a5006e39&title=&width=865)

#### Request Parameters Not Encrypted, Response Results Not Encrypted, Validate Timestamp and Signature

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550690737-7e0c7c38-3250-48c3-ae6d-9714183a278d.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=853&id=u793141e2&originHeight=1279&originWidth=1257&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=67864&status=done&style=none&taskId=u61834138-13f9-4577-be79-f9aa7976526&title=&width=838)

#### Request Parameters Encrypted, Response Results Not Encrypted, Validate Timestamp and Signature

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1723463106346-988f417f-b577-4749-aafd-a2e33a07ed4f.png#averageHue=%23fdfdfd&clientId=u0f345f74-d927-4&from=paste&height=700&id=uca1ae20d&originHeight=700&originWidth=1004&originalType=binary&ratio=1&rotation=0&showTitle=false&size=46035&status=done&style=none&taskId=ubd0db2a7-836f-4800-afbc-f9ae711abe1&title=&width=1004)

#### Request Parameters Not Encrypted, Response Results Encrypted, Validate Timestamp and Signature

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550974052-0dae9688-6744-43c2-ac41-f104c232db5f.png#averageHue=%23fdfdfc&clientId=uaf5e9763-0136-4&from=paste&height=767&id=u6be91f79&originHeight=1151&originWidth=1644&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=73149&status=done&style=none&taskId=u0293497d-bdcd-4dab-8c0b-06958221d5d&title=&width=1096)

#### Skip All Validations

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718551122125-f8d16e65-c796-41ca-8c4e-625065c6dbf3.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=835&id=u3a8f089f&originHeight=1253&originWidth=2445&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=104682&status=done&style=none&taskId=u81683239-7942-490d-8fa8-fb9d8e4b443&title=&width=1630)

#### Error Request

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1723464493419-40a6cdd2-7479-47c1-9bce-f60229f41e67.png#averageHue=%23fdfdfd&clientId=u0f345f74-d927-4&from=paste&height=649&id=u3d301b35&originHeight=649&originWidth=931&originalType=binary&ratio=1&rotation=0&showTitle=false&size=38983&status=done&style=none&taskId=uc566f878-8717-4f02-bf1b-31a2cd7f9fd&title=&width=931)

# IV. Technical Analysis

[API Interface Encryption and Decryption Technical Solution (Referencing HTTPS Principles and WeChat Pay)](https://juejin.cn/post/7358368402795692082) 