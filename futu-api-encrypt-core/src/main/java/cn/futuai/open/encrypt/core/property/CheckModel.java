package cn.futuai.open.encrypt.core.property;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * CheckModel
 * @author Jason Kung
 * @date 2024/08/12 14:08
 */
@Data
public class CheckModel {

    /**
     * 检查模式
     */
    private CheckModelEnum model = CheckModelEnum.WHITE_LIST;

    /**
     * 白名单
     */
    private List<String> whiteList = new ArrayList<>();

    /**
     * 黑名单
     */
    private List<String> blackList = new ArrayList<>();

    public enum CheckModelEnum {
        /**
         * 白名单
         */
        WHITE_LIST,
        /**
         * 黑名单
         */
        BLACK_LIST
    }
}
