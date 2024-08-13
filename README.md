# 一、概述

futu-api-encrypt是一款基于spring-boot和spring-cloud-gateway的接口加解密starter，通spring-boot的简单配置就能够实现各种业务场景下的接口参数校验和加解密。其中包含**时间戳**
校验、**签名**校验、**对称加密**、**非对称加密**等多种功能，也能够根据白名单、黑名单配置过滤接口地址。

# 二、快速开始

> 基于spring-cloud-gateway（spring-boot类似）

### 1、引入依赖

```xml

<dependency>
    <groupId>cn.futuai.open</groupId>
    <artifactId>futu-api-encrypt-spring-cloud-gateway-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2、配置参数
```yaml
spring:
  cloud:
    gateway:
      api-encrypt:
        # 全局开关
        enabled: true
        # 加密对称密钥http header key
        encrypt-aes-key-header-key: "ek"
        # 时间戳http header key
        timestamp-header-key: "ts"
        # 签名值http header key
        sign-header-key: "sign"
        # 加密query参数key
        encrypt-param-key: "ciphertext"
        # rsa非对称加密私钥
        rsa-private-key: "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAI6VKvsus/Z0R3zvqre3gzClHJdsCVeKq89hZFFWbg5l3FNbiGZEEiuD1LL+USi5GCxeRK+xEOFTj7I/waRVb3x7V3J1N0q4nNWoZvRey0MTVaBkoHeB5tzn2ZOCBQRJnijXcO58ChcLXOTQId+zDiCBCom/A62gtH8isH4PYoZXAgMBAAECgYBpetrwNa223nDgcWFHRkCMZSmQr8D9fT37Th5rudfzWNG07RssJKGYhY9913xs9vl2IUsI+qH1P98nS9lSXE37mfOKFhfGZIUjAhMb7/w8hhuHpBXopVpUJZW0B46gfPOsrmvq+xiwlI02UYJ1ZOrfdfbvss/Gwtgrk4pMigL1OQJBANRm4mOUMwF+xUxeOLa2Aafke/iwdcxoV1k1gXmTH0B8wf08zDR7heW737YBEvsjyfEpjo7Y0kGSE5zmTWNnuKUCQQCr2XuZeJLqq6etq7IhboPAx8E2xgfOY/hgKPr9IvM8gYee628YhyOynIhOVFGxaf7dMH9eZ1P6jAbvsgm+mFZLAkEAyY0btJyTzg5q0G30aUTKy3OgRDvGfIJiqM+CHkiCdmIsfs5rhD3WsEqYHZBlX5T1cvgZQ+nxkrE4FUHhG7v31QJAYJZ9TNYjJTjTpt5A4V9/OAROCZ4mVw+DU3DVGR/ivJhFBMJpD80s+D/YsMXdoKzlraaLgCDtZ336jBByP6jZnwJBAIGUnbs7eRLcXzlbORdKC/EfkDYS2rrXLFvQhehT7Y8dKHLZfJElnrHB33Qd8R8WP0PsPU6D7EWNU2zVNK1EDxY="
        # 检测模式
        check-model:
          # 模式
          model: black_list
          # 黑名单URL列表
          black-list:
            - /api/user/code
            - /api/user/code2
            - /api/user/code3
            - /api/user/code4
            - /api/user/export
        timestamp:
          # 时间戳校验开关
          enabled: true
          # 时间戳有效秒数
          timestamp-valid-second: 31536000 # 1年
        sign:
          # 签名校验开关
          enabled: true
        # 请求解密配置
        request-decrypt:
          # 是否开启
          enabled: true
          # 检测模式
          check-model:
            # 白名单模式
            model: white_list
            # 白名单URL列表
            white-list:
              - /api/user/code2
              - /api/user/code4
        # 响应加密配置
        response-encrypt:
          # 是否开启
          enabled: true
          # 检测模式
          check-model:
            # 黑名单模式
            model: black_list
            # 黑名单URL列表
            black-list:
              - /api/user/code
              - /api/user/code4
              - /api/user/export
```
### 3、自定义API异常回调管理器
```java
@Configuration
public class ApiEncryptConfiguration {

    public ApiEncryptConfiguration() {
        // 网关回调管理器
        GatewayApiExceptionCallbackManager.setApiExceptionHandler(new GatewayApiExceptionRequestHandler() {
            /**
             * 网关API校验失败，就会调用此回调
             * @param serverWebExchange serverWebExchange
             * @param throwable 异常
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
# 三、案例
### 1、配置网关服务和用户服务
#### 网关服务：

- 引入futu-api-encrypt-spring-cloud-gateway-starter和nacos依赖
- 参考“快速开始”配置API加解密规则
#### 用户服务

- 引入nacos等依赖
- 定义API接口

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
        String name = "测试文件";

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

### 2、发起请求

#### 请求参数加密，响应结果加密，校验时间戳、签名

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1723463068571-469cbc39-1237-43d4-bdaa-e6495fd6efad.png#averageHue=%23fdfcfc&clientId=u0f345f74-d927-4&from=paste&height=618&id=ud0b94ff5&originHeight=618&originWidth=865&originalType=binary&ratio=1&rotation=0&showTitle=false&size=40216&status=done&style=none&taskId=u55edca16-0e37-4bf6-8a05-2c2a5006e39&title=&width=865)

#### 请求参数不加密，响应结果不加密，校验时间戳、签名

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550690737-7e0c7c38-3250-48c3-ae6d-9714183a278d.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=853&id=u793141e2&originHeight=1279&originWidth=1257&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=67864&status=done&style=none&taskId=u61834138-13f9-4577-be79-f9aa7976526&title=&width=838)

#### 请求参数加密，响应结果不加密，校验时间戳、签名

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1723463106346-988f417f-b577-4749-aafd-a2e33a07ed4f.png#averageHue=%23fdfdfd&clientId=u0f345f74-d927-4&from=paste&height=700&id=uca1ae20d&originHeight=700&originWidth=1004&originalType=binary&ratio=1&rotation=0&showTitle=false&size=46035&status=done&style=none&taskId=ubd0db2a7-836f-4800-afbc-f9ae711abe1&title=&width=1004)

#### 请求参数不加密，响应结果加密，校验时间戳、签名

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550974052-0dae9688-6744-43c2-ac41-f104c232db5f.png#averageHue=%23fdfdfc&clientId=uaf5e9763-0136-4&from=paste&height=767&id=u6be91f79&originHeight=1151&originWidth=1644&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=73149&status=done&style=none&taskId=u0293497d-bdcd-4dab-8c0b-06958221d5d&title=&width=1096)

#### 跳过所有检测

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718551122125-f8d16e65-c796-41ca-8c4e-625065c6dbf3.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=835&id=u3a8f089f&originHeight=1253&originWidth=2445&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=104682&status=done&style=none&taskId=u81683239-7942-490d-8fa8-fb9d8e4b443&title=&width=1630)

#### 错误请求

![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1723464493419-40a6cdd2-7479-47c1-9bce-f60229f41e67.png#averageHue=%23fdfdfd&clientId=u0f345f74-d927-4&from=paste&height=649&id=u3d301b35&originHeight=649&originWidth=931&originalType=binary&ratio=1&rotation=0&showTitle=false&size=38983&status=done&style=none&taskId=uc566f878-8717-4f02-bf1b-31a2cd7f9fd&title=&width=931)

# 四、原理解析

[API接口加解密技术方案（参考HTTPS原理和微信支付）](https://juejin.cn/post/7358368402795692082)

