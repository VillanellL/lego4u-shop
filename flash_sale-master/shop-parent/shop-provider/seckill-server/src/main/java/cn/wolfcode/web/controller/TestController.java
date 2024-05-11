package cn.wolfcode.web.controller;

import cn.wolfcode.web.anno.RequireLogin;
import cn.wolfcode.web.config.UserPhone;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by wolfcode-lanxw
 */
@RestController
public class TestController {
    @RequestMapping("/test")
    @RequireLogin
    public String test(@UserPhone String phone){
        System.out.println("当前用户:"+phone);
        return "test";
    }
    @RequestMapping("/test2")
    public String test(){
        System.out.println("1");
        System.out.println("2");
        return "test";
    }
}
