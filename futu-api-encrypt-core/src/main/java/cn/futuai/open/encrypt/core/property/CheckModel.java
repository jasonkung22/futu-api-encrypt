package cn.futuai.open.encrypt.core.property;

import java.util.ArrayList;
import java.util.List;

/**
 * CheckModel
 * @author Jason Kung
 * @date 2024/08/12 14:08
 */
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

    public CheckModelEnum getModel() {
        return model;
    }

    public void setModel(CheckModelEnum model) {
        this.model = model;
    }

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<String> blackList) {
        this.blackList = blackList;
    }

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
