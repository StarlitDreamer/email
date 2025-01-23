package com.java.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.User;
import com.java.email.service.EmailLogService;
import com.java.email.service.UserService;
import com.java.email.vo.FilterTaskVo;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author EvoltoStar
 */
@Slf4j
public class FilterEmailTaskHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final EmailLogService emailLogService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FilterEmailTaskHandler(EmailLogService emailLogService, UserService userService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        // 处理 GET 请求，查询邮件任务
        if (request.method() == HttpMethod.GET && "/emailManage/filterTask".equals(decoder.path())) {
            try {
                // 解析查询参数
                Map<String, String> params = decoder.parameters().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

                // 获取分页参数
                int page = Integer.parseInt(params.getOrDefault("page", "0"));
                int size = Integer.parseInt(params.getOrDefault("size", "5"));

                // 移除分页参数
                params.remove("page");
                params.remove("size");

                // 使用动态参数查询
                FilterTaskVo filterTaskVo =new FilterTaskVo();
                List<EmailTask> emailTaskList = emailLogService.findByDynamicQueryEmailTask(params, page, size);
                List<FilterTaskVo> emailFilterTaskVoList = emailTaskList.stream().map(emailTask -> {
                    BeanUtils.copyProperties(emailTask, filterTaskVo);
                    filterTaskVo.setStartDate(dateTimeFormatter(emailTask.getStartDate()));
                    filterTaskVo.setEndDate(dateTimeFormatter(emailTask.getEndDate()));
                    filterTaskVo.setPage(page);
                    filterTaskVo.setSize(size);
                    try {
                        User user=userService.findByUserId(emailTask.getSenderId()[0]);
                        filterTaskVo.setSenderName(user.getUserName());
                        filterTaskVo.setSenderEmail(user.getUserEmail());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }return filterTaskVo;
                }).toList();


                // 转换为JSON并返回
                String responseContent = objectMapper.writeValueAsString(emailFilterTaskVoList);
                sendResponse(ctx, HttpResponseStatus.OK, responseContent);
            } catch (Exception e) {
                log.error("Error processing request", e);
                sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
            }
        } else {
            ctx.fireChannelRead(request.retain());
        }

    }

    private String dateTimeFormatter(long secondsTimestamp) {
        // 将秒级时间戳转换为 LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(secondsTimestamp), ZoneId.systemDefault());

        // 格式化日期为字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
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
