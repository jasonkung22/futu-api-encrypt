package cn.futuai.open.encrypt.core.property;


/**
 * SignVerify
 * @author Jason Kung
 * @date 2024/08/12 14:09
 */
public class SignVerify {

    /**
     * 是否开启签名校验
     */
    private Boolean enabled = true;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
