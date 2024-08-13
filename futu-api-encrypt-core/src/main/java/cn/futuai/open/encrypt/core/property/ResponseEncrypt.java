package cn.futuai.open.encrypt.core.property;

import lombok.Data;

/**
 * ResponseEncrypt
 * @author Jason Kung
 * @date 2024/08/12 14:10
 */
@Data
public class ResponseEncrypt {

    /**
     * 是否开启响应加密
     */
    private Boolean enabled = true;
    /**
     * 请求解密检查模式
     */
    private CheckModel checkModel = new CheckModel();
}
