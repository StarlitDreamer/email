package com.java.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.pojo.User;
import com.java.email.result.EmailTaskTypeEnum;
import com.java.email.service.EmailLogService;
import com.java.email.service.UserService;
import com.java.email.vo.UndeliveredEmailVo;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FailLogHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final EmailLogService emailLogService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FailLogHandler(EmailLogService emailLogService, UserService userService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        // 处理 GET 请求，查询失败邮件
        if (request.method() == HttpMethod.GET && "/api/email/faillog".equals(decoder.path())) {
            // 解析查询参数
            Map<String, String> params = decoder.parameters().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));


            int page = 0, size = 10;
            try {
                page = Integer.parseInt(params.getOrDefault("page_num", "0"));
                size = Integer.parseInt(params.getOrDefault("page_size", "10"));
                if (page < 0 || size <= 0) {
                    sendResponse(ctx, HttpResponseStatus.BAD_REQUEST,
                            "Page must be >= 0 and size must be > 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                sendResponse(ctx, HttpResponseStatus.BAD_REQUEST,
                        "Invalid page or size parameter. Expected integers.");
                return;
            }

            params.remove("page_num");
            params.remove("page_size");

            List<UndeliveredEmail> logList = emailLogService.findByDynamicQueryFailEmail(params, page, size);

            List<UndeliveredEmailVo> emailVoList=logList.stream().map(email -> {

                EmailTask emailTask = null;
                try {
                    emailTask = emailLogService.findByEmailTaskId(email.getEmailTaskId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                UndeliveredEmailVo emailVo= new UndeliveredEmailVo();
                    emailVo.setSubject(emailTask.getSubject());
                    emailVo.setTaskType(EmailTaskTypeEnum.getMessageByCode(emailTask.getTaskType()));
                    emailVo.setEmailType(emailTask.getSubject());
                    BeanUtils.copyProperties(email, emailVo);
                    emailVo.setLevel("高级");
                    emailVo.setErrorMsg(email.getErrorMsg());
                    emailVo.setStartDate(dateTimeFormatter(email.getStartDate()));
                    emailVo.setEndDate(dateTimeFormatter(email.getEndDate()));

                User sender = null;
                try {
                    sender = userService.findByUserEmail(email.getSenderId()[0]);
                    User receiver = userService.findByUserEmail(email.getReceiverId()[0]);
                    emailVo.setSenderEmail(sender.getUserEmail());
                    emailVo.setSenderName(sender.getUserName());
                    emailVo.setReceiverEmail(receiver.getUserEmail());
                    emailVo.setReceiverName(receiver.getUserName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


                return emailVo;

            }).toList();

            String responseContent = objectMapper.writeValueAsString(emailVoList);
            sendResponse(ctx, HttpResponseStatus.OK, responseContent);
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
