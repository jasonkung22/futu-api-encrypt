package cn.futuai.open.encrypt.core.util;

import cn.futuai.open.encrypt.core.property.CheckModel;
import cn.futuai.open.encrypt.core.property.CheckModel.CheckModelEnum;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

/**
 * api检查器
 * @author Jason Kung
 * @date 2024/08/08 15:43
 */
public class ApiChecker {

    private final static AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    private static final Logger log = LoggerFactory.getLogger(ApiChecker.class);

    /**
     * 是否通过
     * @param requestUri 请求标识符
     * @param enabled    是否可用
     * @param checkModel 检查模式
     * @return 是否通过
     */
    public static boolean isPass(String requestUri, Boolean enabled, CheckModel checkModel) {
        if (!Boolean.TRUE.equals(enabled)) {
            return true;
        }

        return isPass(requestUri, checkModel);
    }

    /**
     * 是否匹配URL
     * @param url     URL
     * @param urlList URL列表
     * @return 是否匹配
     */
    public static boolean isMatchUrl(String url, List<String> urlList) {
        if (StrUtil.isEmpty(url)) {
            return false;
        }

        if (CollectionUtil.isEmpty(urlList)) {
            return false;
        }

        for (String item : urlList) {
            if (ANT_PATH_MATCHER.match(item, url)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否是容忍接口
     * @param requestUri    请求标识符
     * @param tolerantUrls  容忍接口列表
     * @param encryptAesKey 加密key的值
     * @return 是否是容忍接口且没有加密key
     */
    public static boolean isTolerantRequest(String requestUri, List<String> tolerantUrls, String encryptAesKey) {
        boolean isMatchTolerantUrl = isMatchUrl(requestUri, tolerantUrls);
        boolean isBlankAesKey = StrUtil.isBlank(encryptAesKey);
        boolean isTolerant = isMatchTolerantUrl && isBlankAesKey;

        if (isTolerant) {
            log.info("Tolerant mode check passed - Request URI: {}, Matched tolerant URLs: {}, No encryption key",
                    requestUri, tolerantUrls);
        } else if (isMatchTolerantUrl) {
            log.debug("Tolerant URL matched but contains encryption key - Request URI: {}, Encryption key: {}",
                    requestUri, encryptAesKey);
        }

        return isTolerant;
    }

    private static boolean isPass(String url, CheckModel checkModel) {

        boolean isPass = CheckModelEnum.WHITE_LIST.equals(checkModel.getModel())
                && isMatchUrl(url, checkModel.getWhiteList());

        if (CheckModelEnum.BLACK_LIST.equals(checkModel.getModel())
                && !isMatchUrl(url, checkModel.getBlackList())) {
            isPass = true;
        }

        return isPass;
    }
}
