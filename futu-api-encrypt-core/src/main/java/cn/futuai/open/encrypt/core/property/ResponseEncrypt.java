package cn.futuai.open.encrypt.core.property;


/**
 * ResponseEncrypt
 * @author Jason Kung
 * @date 2024/08/12 14:10
 */
public class ResponseEncrypt {

    /**
     * 是否开启响应加密
     */
    private Boolean enabled = true;
    /**
     * 请求解密检查模式
     */
    private CheckModel checkModel = new CheckModel();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public CheckModel getCheckModel() {
        return checkModel;
    }

    public void setCheckModel(CheckModel checkModel) {
        this.checkModel = checkModel;
    }
}
