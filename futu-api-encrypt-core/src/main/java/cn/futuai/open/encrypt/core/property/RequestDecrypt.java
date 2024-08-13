package cn.futuai.open.encrypt.core.property;

import lombok.Data;

/**
 * RequestDecrypt
 * @author Jason Kung
 * @date 2024/08/12 14:10
 */
@Data
public class RequestDecrypt {

    /**
     * 是否开启响应解密
     */
    private Boolean enabled = true;
    /**
     * 请求解密检查模式
     */
    private CheckModel checkModel = new CheckModel();
}
