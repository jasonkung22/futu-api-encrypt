package cn.futuai.open.encrypt.core.property;


/**
 * TimestampVerify
 * @author Jason Kung
 * @date 2024/08/12 14:09
 */
public class TimestampVerify {

    /**
     * 是否开启时间戳校验
     */
    private Boolean enabled = true;
    /**
     * 时间戳有效秒数
     */
    private Long timestampValidSecond = 24 * 60 * 60L;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getTimestampValidSecond() {
        return timestampValidSecond;
    }

    public void setTimestampValidSecond(Long timestampValidSecond) {
        this.timestampValidSecond = timestampValidSecond;
    }
}
