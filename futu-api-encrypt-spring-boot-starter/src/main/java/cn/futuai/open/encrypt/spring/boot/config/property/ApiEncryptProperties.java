package cn.futuai.open.encrypt.spring.boot.config.property;

import cn.futuai.open.encrypt.core.property.BaseApiEncryptProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * api加密参数
 * @author Jason Kung
 * @date 2024/06/07 16:19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "spring.api-encrypt")
public class ApiEncryptProperties extends BaseApiEncryptProperties {

}
