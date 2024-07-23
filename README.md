# 一、概述
gateway-api-encrypt是一款基于spring-cloud-gateway的接口加解密starter，通过SpringBoot的简单配置就能够实现各种业务场景下的接口参数校验和加解密。其中包含**时间戳**校验、**签名**校验、**对称加密**、**非对称加密**等多种功能，也能够根据白名单、黑名单配置过滤接口地址。
# 二、快速开始
### 1、引入依赖
```xml
<dependency>
    <groupId>cn.futuai.open</groupId>
    <artifactId>gateway-api-encrypt-starter</artifactId>
    <version>1.0.0</version>
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
        timestamp-header-key: "ek"
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
        timestamp:
          # 时间戳校验开关
          enable: true
          # 时间戳有效秒数
          timestamp-valid-second: 604800
        sign:
          # 签名校验开关
          enable: true
        # 请求解密配置
        request-decrypt:
          # 检测模式
          check-model:
            # 白名单模式
            model: white_list
            # 白名单URL列表
            white-list:
              - /api/user/code2
        # 响应加密配置
        response-encrypt:
          # 检测模式
          check-model:
            # 黑名单模式
            model: black_list
            # 黑名单URL列表
            black-list:
              - /api/user/code
```
### 3、自定义API异常回调管理器
```java
@Configuration
public class ApiEncryptConfiguration {

    public ApiEncryptConfiguration() {
        // 网关回调管理器
        GatewayApiInvalidCallbackManager.setApiInvalidHandler(new ApiValidExceptionRequestHandler() {
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
                String errJson = JacksonMapper.toJsonString(error);
                return ServerResponse.ok().body(Mono.just(errJson), String.class);
            }
        });
    }
}
```
# 三、案例
### 1、配置网关服务和用户服务
#### 网关服务：

- 引入gateway-api-encrypt-starter和nacos依赖
- 参考“快速开始”配置API加解密规则
#### 用户服务

- 引入nacos等依赖
- 定义API接口
```java
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
```
### 2、发起请求
[postman文件](https://github.com/jasonkung22/gateway-api-encrypt/blob/master/gateway-api-encrypt-example/doc/api-encrypt.postman_collection.json)
#### 请求参数加密，响应结果加密，校验时间戳、签名
![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550548941-7602284a-2398-49dd-8693-1b20c32e199c.png#averageHue=%23fdfdfc&clientId=uaf5e9763-0136-4&from=paste&height=753&id=u549165f4&originHeight=1130&originWidth=1568&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=76756&status=done&style=none&taskId=ubb236067-46d9-4a1a-b906-2198f8658d7&title=&width=1045.3333333333333)
#### 请求参数不加密，响应结果不加密，校验时间戳、签名
![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550690737-7e0c7c38-3250-48c3-ae6d-9714183a278d.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=853&id=u793141e2&originHeight=1279&originWidth=1257&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=67864&status=done&style=none&taskId=u61834138-13f9-4577-be79-f9aa7976526&title=&width=838)
#### 请求参数加密，响应结果不加密，校验时间戳、签名
![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550906540-d2a6663d-15ae-4e06-8cfb-a012bbe24241.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=867&id=u5ecbb6ed&originHeight=1300&originWidth=1549&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=76880&status=done&style=none&taskId=u7aec523a-1a77-4947-b414-92ae55da36c&title=&width=1032.6666666666667)
#### 请求参数不加密，响应结果加密，校验时间戳、签名
![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718550974052-0dae9688-6744-43c2-ac41-f104c232db5f.png#averageHue=%23fdfdfc&clientId=uaf5e9763-0136-4&from=paste&height=767&id=u6be91f79&originHeight=1151&originWidth=1644&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=73149&status=done&style=none&taskId=u0293497d-bdcd-4dab-8c0b-06958221d5d&title=&width=1096)
#### 跳过所有检测
![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718551122125-f8d16e65-c796-41ca-8c4e-625065c6dbf3.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=835&id=u3a8f089f&originHeight=1253&originWidth=2445&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=104682&status=done&style=none&taskId=u81683239-7942-490d-8fa8-fb9d8e4b443&title=&width=1630)
#### 错误请求
![image.png](https://cdn.nlark.com/yuque/0/2024/png/1221070/1718551549253-0ff61da8-f04f-4718-873b-4c4219201df9.png#averageHue=%23fdfdfd&clientId=uaf5e9763-0136-4&from=paste&height=761&id=ud5f5fadc&originHeight=1142&originWidth=2458&originalType=binary&ratio=1.5&rotation=0&showTitle=false&size=105907&status=done&style=none&taskId=uc902df94-a5f6-4abf-a68f-8c5c6f0f14e&title=&width=1638.6666666666667)
### 3、案例源码
[https://github.com/jasonkung22/gateway-api-encrypt/tree/master/gateway-api-encrypt-example](https://github.com/jasonkung22/gateway-api-encrypt/tree/master/gateway-api-encrypt-example)
# 四、原理解析
[API接口加解密技术方案（参考HTTPS原理和微信支付）](https://juejin.cn/post/7358368402795692082)
# 五、原理解析
