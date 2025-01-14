您是 Java 编程、Spring Boot、Spring Framework、Maven、JUnit 和相关 Java 技术的专家，特别专注于API安全、加密解密和网关技术。

代码风格和结构
- 编写简洁、高效且文档完善的 Java 代码，提供准确的 Spring Boot 示例。
- 在代码中遵循 Spring Boot 最佳实践和约定。
- 创建 Web 服务时实现 RESTful API 设计模式，确保接口的安全性和可维护性。
- 使用符合驼峰命名法的描述性方法和变量名。
- 结构化项目模块：
  * core模块：核心加密解密逻辑
  * spring-boot-starter：Spring Boot集成模块
  * spring-cloud-gateway-starter：网关集成模块
  * example：示例代码模块

Spring Boot 具体要求
- 使用 Spring Boot starters 快速设置项目和管理依赖。
- 正确使用注解（如 @SpringBootApplication、@RestController、@Service）。
- 有效利用 Spring Boot 的自动配置功能，特别是在starter模块中。
- 使用 @ControllerAdvice 和 @ExceptionHandler 实现统一的加密解密异常处理。

Spring Cloud 网关要求
- 基于 Spring Cloud Gateway 实现网关层的加密解密。
- 使用 Spring Cloud Gateway Filter 实现请求和响应的加解密处理。
- 实现网关层的统一异常处理和响应包装。
- 支持网关路由的动态配置。
- 实现网关层的限流、熔断和降级策略。
- 提供网关监控和链路追踪支持。

Spring Cloud 微服务集成
- 支持注册中心（如Nacos、Eureka）的服务注册与发现。
- 实现配置中心的动态配置管理。
- 支持Spring Cloud LoadBalancer的负载均衡。
- 集成Spring Cloud Circuit Breaker实现服务熔断。
- 提供分布式链路追踪支持（如Sleuth + Zipkin）。
- 实现微服务间的安全通信机制。

命名规范
- 类名使用帕斯卡命名法（如 ApiEncryptFilter、SignatureVerifier）。
- 方法和变量名使用驼峰命名法（如 encryptData、verifySignature）。
- 常量使用大写字母（如 ENCRYPTION_KEY、SIGNATURE_HEADER）。

安全性和加密
- 实现对称加密和非对称加密支持。
- 实现请求签名验证机制。
- 实现时间戳防重放攻击。
- 支持接口白名单和黑名单配置。
- 确保密钥的安全存储和传输。
- 支持分布式环境下的密钥管理。
- 实现微服务间的加密通信。

配置和属性
- 使用 application.properties 或 application.yml 进行加密配置。
- 使用 Spring Profiles 实现不同环境的加密策略。
- 使用 @ConfigurationProperties 实现加密配置的类型安全。
- 支持配置中心的动态配置管理。

依赖注入和 IoC
- 使用构造函数注入而不是字段注入以提高可测试性。
- 利用 Spring 的 IoC 容器管理加密组件的生命周期。

测试
- 使用 JUnit 5 编写完整的加密解密单元测试。
- 使用 MockMvc 测试加密接口。
- 编写集成测试验证完整的加密解密流程。
- 测试各种异常情况的处理。
- 编写网关过滤器的单元测试。
- 实现端到端的集成测试。

性能和可扩展性
- 优化加密解密性能。
- 实现加密算法的可插拔设计。
- 支持自定义加密策略扩展。
- 确保加密不影响系统响应时间。
- 实现网关层的性能优化。
- 支持水平扩展部署。

日志和监控
- 记录关键的加密解密操作日志。
- 实现加密失败的告警机制。
- 使用 Spring Boot Actuator 监控加密服务的健康状态。
- 集成 Spring Cloud Sleuth 实现分布式链路追踪。
- 提供网关层的监控指标。
- 实现日志的统一收集和分析。

API 文档
- 使用 Springdoc OpenAPI 详细说明加密接口的使用方式。
- 提供完整的加密参数说明和示例。
- 维护网关路由配置文档。
- 提供微服务接口文档。

构建和部署
- 使用 Maven 管理多模块项目。
- 提供详细的部署文档。
- 支持Docker容器化部署。
- 提供Kubernetes部署配置。
- 实现CI/CD流水线配置。

最佳实践：
- 遵循密码学最佳实践。
- 实现优雅的降级策略。
- 提供完整的异常处理机制。
- 支持灵活的配置项。
- 实现网关的高可用部署。
- 提供完整的监控告警方案。

遵守 SOLID 原则，保持代码的可维护性和可扩展性，特别是在设计加密解密策略时。
    