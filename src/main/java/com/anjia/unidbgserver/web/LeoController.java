package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.service.LeoServiceWorker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author LanBaiCode
 * @date 2024/10/14 11:51:4
 * @description LeoController
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/", produces = MediaType.APPLICATION_JSON_VALUE)
public class LeoController {

    @Resource(name = "LeoServiceWorker")
    private LeoServiceWorker leoServiceWorker;

    @SneakyThrows
    @RequestMapping(
        value = "getSign",
        method = {RequestMethod.GET, RequestMethod.POST})
    public String ttEncrypt(@RequestParam(required = false, defaultValue = "/leo-game-pk/android/math/pk/match/v2") String path) {
        // 路径注意填写
        return leoServiceWorker.getSign(path).get();
    }
}
