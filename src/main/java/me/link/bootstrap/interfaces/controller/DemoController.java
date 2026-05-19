package me.link.bootstrap.interfaces.controller;

import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.interfaces.dto.response.ResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
@Slf4j
public class DemoController {

    @GetMapping
    public ResultResponse<Void> demo() {
        log.info("DemoController.demo()");
        return ResultResponse.success();
    }
}
