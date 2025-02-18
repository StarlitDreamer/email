package com.java.email.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.common.Redis.RedisService;
import com.java.email.constant.RedisConstData;
import com.java.email.utils.JwtUtil;
import com.java.email.utils.LogUtil;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class TokenCheckHandler extends ChannelInboundHandlerAdapter {
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private static final LogUtil logUtil = LogUtil.getLogger(TokenCheckHandler.class);
    
    private final List<String> excludePaths = Arrays.asList(
            "/userManage/createUser",
            "/user/login",
            "/error"
    );

    public TokenCheckHandler(RedisService redisService) {
        this.redisService = redisService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            ctx.fireChannelRead(msg);
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        String uri = request.uri();
        
        // 检查是否是排除的路径
        if (isExcludePath(uri)) {
            ctx.fireChannelRead(request.retain());
            return;
        }

        int runStep = 0;
        try {
            do {
                // 从token获取userId
                String token = request.headers().get("Authorization");
                if (token == null) {
                    runStep = 1;
                    break;
                }

                Map<String, Object> userMap = JwtUtil.parseToken(token);
                if (userMap == null) {
                    runStep = 2;
                    break;
                }

                String redisKey = RedisConstData.USER_LOGIN_TOKEN + userMap.get("id");
                if (Strings.isEmpty(redisKey)) {
                    runStep = 3;
                    break;
                }

                String redisToken = redisService.get(redisKey);
                if (Strings.isEmpty(redisToken)) {
                    runStep = 4;
                    break;
                }

                // 验证token是否匹配
                if (!redisToken.equals(token)) {
                    runStep = 5;
                    break;
                }

                // 验证通过，存储用户信息
                userMap.put("token", redisToken);
                ThreadLocalUtil.set(userMap);
                
                // 继续处理请求
                ctx.fireChannelRead(request.retain());
                return;

            } while (false);

            // 记录错误日志
            logUtil.error("TokenCheckHandler error in step : " + runStep + ", url: " + request.uri());
            
            // 发送未授权响应
            HttpUtil.sendResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "未授权访问");
            
        } finally {
            // 清理ThreadLocal
            ThreadLocalUtil.remove();
        }
    }

    private boolean isExcludePath(String uri) {
        String path = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
        return excludePaths.stream().anyMatch(path::startsWith);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logUtil.error("Token验证发生错误", cause);
        HttpUtil.sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
        ctx.close();
    }
} 