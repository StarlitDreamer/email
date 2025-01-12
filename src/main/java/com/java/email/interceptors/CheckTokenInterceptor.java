package com.example.rv.interceptors;

import com.example.rv.constData.RedisConstData;
import com.example.rv.service.common.RedisService;
import com.example.rv.utils.JwtUtil;
import com.example.rv.utils.LogUtil;
import com.example.rv.utils.Md5Util;
import com.example.rv.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class CheckTokenInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisService redisService;
    private static final LogUtil logUtil=LogUtil.getLogger(CheckTokenInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        int runStep=0;
        do {
            //从token获取userId，拿到redis的key
            String token=request.getHeader("Authorization");
            if (token == null){
                runStep = 1;
                break;
            }
            Map<String,Object> userMap = JwtUtil.parseToken(token);
            if (userMap == null){
                runStep = 2;
                break;
            }
            String redisKey = RedisConstData.USER_LOGIN_TOKEN + userMap.get("id");
            if (Strings.isEmpty(redisKey)){
                runStep = 3;
                break;
            }
            String redisToken = redisService.get(redisKey);
            if (Strings.isEmpty(redisToken)){
                runStep = 4;
                break;
            }
            //万一刷新了之后前端没更新，token不相等也算过期。
            if (!redisToken.equals(token)){
                runStep = 5;
                break;
            }
            //没过期就把数据存进线程
            userMap.put("token",redisToken);
            ThreadLocalUtil.set(userMap);
            return true;
        }while (false);
        logUtil.error("CheckTokenInterceptor error in step : " + runStep + ", url: " + request.getRequestURL());
        response.setStatus(401);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
