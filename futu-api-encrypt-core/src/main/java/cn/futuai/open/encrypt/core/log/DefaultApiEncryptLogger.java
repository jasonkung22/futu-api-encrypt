package cn.futuai.open.encrypt.core.log;

import cn.futuai.open.encrypt.core.constants.enums.ApiEncryptLogLevelEnum;
import cn.futuai.open.encrypt.core.exception.ApiDecryptException;
import cn.futuai.open.encrypt.core.exception.ApiEncryptException;
import cn.futuai.open.encrypt.core.exception.ApiSignException;
import cn.futuai.open.encrypt.core.exception.ApiTimestampException;
import cn.futuai.open.encrypt.core.property.BaseApiEncryptProperties;
import cn.hutool.core.util.ArrayUtil;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认api encrypt logger
 * @author Jason Kung
 * @date 2024/12/24 10:55
 */
public class DefaultApiEncryptLogger implements ApiEncryptLogger {

    private static final Logger log = LoggerFactory.getLogger(DefaultApiEncryptLogger.class);


    private static final String DEFAULT_LEVEL_KEY = "default";

    private final BaseApiEncryptProperties apiEncryptProperties;

    public DefaultApiEncryptLogger(BaseApiEncryptProperties apiEncryptProperties) {
        this.apiEncryptProperties = apiEncryptProperties;
    }

    /**
     * 记录异常日志
     * @param ex 异常
     */
    @Override
    public void logException(Throwable ex) {
        if (ex instanceof ApiTimestampException) {
            ApiTimestampException apiTimestampException = (ApiTimestampException) ex;
            log(apiTimestampException.getRequestUri(), ex, "api timestamp exception, requestUri:{}, timestamp:{}",
                    apiTimestampException.getRequestUri(),
                    apiTimestampException.getTimestamp());
        } else if (ex instanceof ApiSignException) {
            ApiSignException apiSignException = (ApiSignException) ex;
            log(apiSignException.getRequestUri(), ex,
                    "api sign exception, requestUri:{}, timestamp:{}, aesKey:{}, orgQueryString:{}, orgBody:{}",
                    apiSignException.getRequestUri(),
                    apiSignException.getTimestamp(), apiSignException.getAesKey(), apiSignException.getOrgQueryString(),
                    apiSignException.getOrgBody());
        } else if (ex instanceof ApiDecryptException) {
            ApiDecryptException apiDecryptException = (ApiDecryptException) ex;
            log(apiDecryptException.getRequestUri(), ex, "api decrypt exception, requestUri:{}, param:{}, aesKey:{}",
                    apiDecryptException.getRequestUri(),
                    apiDecryptException.getParam(), apiDecryptException.getAesKey());
        } else if (ex instanceof ApiEncryptException) {
            ApiEncryptException apiEncryptException = (ApiEncryptException) ex;
            log(apiEncryptException.getRequestUri(), ex, "api encrypt exception, requestUri:{}, param:{}, aesKey:{}",
                    apiEncryptException.getRequestUri(),
                    apiEncryptException.getParam(), apiEncryptException.getAesKey());
        }
    }

    private void log(String requestUri, Throwable ex, String format, Object... arguments) {
        Map<String, ApiEncryptLogLevelEnum> logLevelMap = apiEncryptProperties.getLogLevel();
        ApiEncryptLogLevelEnum defaultLogLevelEnum = logLevelMap.get(DEFAULT_LEVEL_KEY);

        if (!logLevelMap.containsKey(requestUri)) {
            if (defaultLogLevelEnum != null) {
                log(defaultLogLevelEnum, ex, format, arguments);
                return;
            }

            log(ApiEncryptLogLevelEnum.WARN, ex, format, arguments);
            return;
        }

        log(logLevelMap.get(requestUri), ex, format, arguments);
    }

    private void log(ApiEncryptLogLevelEnum levelEnum, Throwable ex, String format,
            Object... arguments) {
        switch (levelEnum) {
            case TRACE:
                log.trace(format, arguments);
                break;
            case DEBUG:
                log.debug(format, arguments);
                break;
            case INFO:
                log.info(format, arguments);
                break;
            case ERROR:
            case FATAL:
                log.error(format, ArrayUtil.append(arguments, ex));
                break;
            case OFF:
                break;
            case WARN:
            default:
                log.warn(format, arguments, ArrayUtil.append(arguments, ex));
                break;
        }
    }
}
