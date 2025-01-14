package cn.futuai.example.test;

import cn.futuai.example.BaseTest;
import cn.futuai.open.encrypt.spring.boot.config.property.ApiEncryptProperties;
import javax.annotation.Resource;
import org.junit.Test;

/**
 * 测试
 * @author Jason Kung
 * @date 2024/12/23 15:00
 */
public class ApiTest extends BaseTest {

    @Resource
    private ApiEncryptProperties apiEncryptProperties;

    @Test
    public void test() {
        System.out.println(apiEncryptProperties);
    }
}
