package com.java.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.pojo.Email;
import com.java.email.service.EmailLogService;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class EmailLogHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final EmailLogService emailLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailLogHandler(EmailLogService emailLogService) {
        this.emailLogService = emailLogService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {

        // 处理POST请求，添加邮件
        if (request.method() == HttpMethod.POST && "/api/email/log".equals(request.uri())) {
            String content = request.content().toString(CharsetUtil.UTF_8);
            
            try {
                // 将JSON转换为EmailLog对象
                Email emailLog = objectMapper.readValue(content, Email.class);
                
                // 保存到Elasticsearch
                Email savedLog = emailLogService.saveEmail(emailLog);
                
                // 返回成功响应
                String responseContent = objectMapper.writeValueAsString(savedLog);
                sendResponse(ctx, HttpResponseStatus.OK, responseContent);
                
            } catch (Exception e) {
                log.error("Error processing email log", e);
                sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            ctx.fireChannelRead(request.retain());
        }
    }

    private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            status,
            Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
        );
        
        response.headers()
            .set(HttpHeaderNames.CONTENT_TYPE, "application/json")
            .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Channel exception caught", cause);
        sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        ctx.close();
    }
} 