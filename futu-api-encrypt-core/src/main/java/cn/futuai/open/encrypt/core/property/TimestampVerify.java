package cn.futuai.open.encrypt.core.property;

import lombok.Data;

/**
 * TimestampVerify
 * @author Jason Kung
 * @date 2024/08/12 14:09
 */
@Data
public class TimestampVerify {

    /**
     * 是否开启时间戳校验
     */
    private Boolean enabled = true;
    /**
     * 时间戳有效秒数
     */
    private Long timestampValidSecond = 24 * 60 * 60L;
}
