package cn.futuai.example.controller;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

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

    @SneakyThrows
    @GetMapping("export")
    public void export(HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        String name = "测试文件";

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", UriUtils.encode(name, StandardCharsets.UTF_8) + ".xlsx");

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            response.setHeader(entry.getKey(), String.join(";", entry.getValue()));
        }

        IOUtils.copy(new FileInputStream(
                        "D:\\project\\personal\\gateway-api-encrypt\\example\\gateway-api-encrypt-example\\doc\\测试.xlsx"),
                response.getOutputStream());
    }
}
