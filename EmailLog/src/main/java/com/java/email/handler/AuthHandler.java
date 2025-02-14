package com.java.email.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.logging.log4j.util.Strings;
import com.java.email.common.Redis.RedisService;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.constant.RedisConstData;
import com.java.email.utils.JwtUtil;
import com.java.email.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import java.util.Map;

@Component
public class AuthHandler extends ChannelInboundHandlerAdapter {
    
    @Autowired
    private RedisService redisService;
    private static final LogUtil logUtil = LogUtil.getLogger(AuthHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            ctx.fireChannelRead(msg);
            return;
        }

        FullHttpRequest request = (FullHttpRequest) msg;
        int runStep = 0;
        boolean isAuthenticated = false;

        do {
            // 从token获取userId，拿到redis的key
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

            // 万一刷新了之后前端没更新，token不相等也算过期
            if (!redisToken.equals(token)) {
                runStep = 5;
                break;
            }

            // 验证通过
            userMap.put("token", redisToken);
            ThreadLocalUtil.set(userMap);
            isAuthenticated = true;
        } while (false);

        if (!isAuthenticated) {
            logUtil.error("AuthHandler error in step : " + runStep + ", url: " + request.uri());
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UNAUTHORIZED,
                Unpooled.EMPTY_BUFFER
            );
            ctx.writeAndFlush(response);
            return;
        }

        try {
            ctx.fireChannelRead(request);
        } finally {
            ThreadLocalUtil.remove();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logUtil.error("AuthHandler error: ", cause);
        ctx.close();
    }
} 