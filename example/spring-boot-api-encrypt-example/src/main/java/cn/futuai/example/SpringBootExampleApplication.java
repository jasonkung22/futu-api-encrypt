package cn.futuai.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 第三方程序
 * @author Duke 附加参数 extra
 * @date 2021/6/2 16:11
 */
@Slf4j
@SpringBootApplication
public class SpringBootExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExampleApplication.class, args);
        log.info("--------------------用户服务启动完成--------------------");
    }
}
