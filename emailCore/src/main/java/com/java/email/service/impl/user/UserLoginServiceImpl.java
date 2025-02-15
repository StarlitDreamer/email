package com.java.email.service.impl.user;

import com.java.email.common.Redis.RedisService;
import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.constant.MagicMathConstData;
import com.java.email.constant.RedisConstData;
import com.java.email.esdao.repository.AuthRepository;
import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.model.domain.User;
import com.java.email.model.entity.AuthDocument;
import com.java.email.model.entity.UserDocument;
import com.java.email.service.user.UserLoginService;
import com.java.email.utils.JwtUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private RedisService redisService;
    
    @Override
    public Result login(User user) {
        // 检查参数
        if (user == null || Strings.isEmpty(user.getUserAccount()) || Strings.isEmpty(user.getUserPassword()) || user.getUserRole()==1){
            return new Result(ResultCode.R_ParamError);
        }
        Integer userRole = user.getUserRole();
        if (userRole != 2 && userRole != 3 && userRole != 4) {
            return new Result(ResultCode.R_ParamError);
        }
        // 验证账号密码
        String account = user.getUserAccount();
        String password = user.getUserPassword();
        UserDocument userDoc = userRepository.findByUserAccount(account);
        if (userDoc == null) {
            return new Result(ResultCode.R_UserNotFound);
        }
        if (!userDoc.getUserPassword().equals(password)) {
            return new Result(ResultCode.R_PasswordError);
        }
        if (!userDoc.getUserRole().equals(userRole)) {
            return new Result(ResultCode.R_ParamError);
        }
        //存入信息生成token
        String userId = userDoc.getUserId();
        if (userId == null) {
            return new Result(ResultCode.R_Fail,"用户信息有误");
        }
        String userName = userDoc.getUserName();
        if (userName == null) {
            return new Result(ResultCode.R_Fail,"用户信息有误");
        }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("name", userName);
        userMap.put("role", userRole); // Use the userRole variable declared earlier
        String token = JwtUtil.genToken(userMap);
        boolean redisSet = redisService.set(RedisConstData.USER_LOGIN_TOKEN + userId, token, MagicMathConstData.REDIS_VERIFY_TOKEN_TIMEOUT, TimeUnit.HOURS);
        if (!redisSet) {
            return new Result(ResultCode.R_Fail);
        }
        // 响应参数
        Map<String, Object> userRep = new HashMap<>();
        userRep.put("user_id",userId);
        userRep.put("user_name",userName); // Using userName already declared above
        userRep.put("user_token",token);
        List<Map<String, String>> userAuths = new ArrayList<>();
        if (userDoc.getUserAuthId() != null) {
            for (String authId : userDoc.getUserAuthId()) {
                Map<String, String> authMap = new HashMap<>();
                authMap.put("user_auth_id", authId);
                // TODO: 这里需要通过authId查询权限名称
                AuthDocument userAuth = authRepository.findByAuthId(authId);
                if (userAuth != null) {
                    authMap.put("user_auth_name", userAuth.getAuthName()); 
                }
                userAuths.add(authMap);
            }
        }
        userRep.put("user_auth", userAuths);
        return new Result(ResultCode.R_Ok,userRep);
    }
}
