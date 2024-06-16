package cn.futuai.example.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户
 * @author Jason Kung
 * @date 2024/06/16 15:57
 */
@Slf4j
@RestController
public class UserController {

    @PostMapping("{path}")
    public Map<String, Object> code(@PathVariable("path") String path, @RequestParam String test1,
            @RequestParam String test2,
            @RequestParam String test3,
            @RequestBody String body) {
        Map<String, Object> map = new HashMap<>();
        map.put("path", path);
        map.put("test1", test1);
        map.put("test2", test2);
        map.put("test3", test3);
        map.put("body", body);
        log.info(String.valueOf(map));
        return map;
    }
}
