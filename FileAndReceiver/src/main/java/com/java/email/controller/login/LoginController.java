package com.java.email.controller.login;

import com.java.email.common.Response.Result;
import com.java.email.model.domain.User;
import com.java.email.service.user.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserLoginService userLoginService;
    @PostMapping("/login")
    public Result login(@RequestBody User user){
        return userLoginService.login(user);
    }
}
