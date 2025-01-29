package org.example.controller;

import org.example.entity.RestBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/api/")
public class AccountController {

    @ResponseBody
    @GetMapping("/text")
    public RestBean<String> test(){
        return RestBean.success("hello world");
    }

}
