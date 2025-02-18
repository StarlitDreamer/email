package com.java.email.controller.login;

import com.java.email.common.Response.Result;
import com.java.email.model.domain.User;
import com.java.email.service.user.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.java.email.utils.LogUtil;
@Validated
@RestController
@RequestMapping("/user")
public class LoginController {

    private static final LogUtil logUtil = LogUtil.getLogger(LoginController.class);

    @Autowired
    private UserLoginService userLoginService;
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        logUtil.info("LoginController.login(),进入登录接口");
        return userLoginService.login(user);
    }

    @GetMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token){
        logUtil.info("LoginController.logout(),退出登录接口");
        return userLoginService.logout(token);
    }
}
