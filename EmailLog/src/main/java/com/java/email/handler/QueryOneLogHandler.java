//package com.java.email.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.handler.codec.http.*;
//import io.netty.util.CharsetUtil;
//import lombok.extern.slf4j.Slf4j;
//import com.java.email.pojo.EmailTask;
//import com.java.email.pojo.UndeliveredEmail;
//import com.java.email.pojo.User;
//import com.java.email.service.EmailLogService;
//import com.java.email.service.UserService;
//import com.java.email.vo.EmailVo;
//import org.springframework.beans.BeanUtils;
//
//import java.io.IOException;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Slf4j
//public class QueryOneLogHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
//
//    private final EmailLogService emailLogService;
//    private final UserService userService;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public QueryOneLogHandler(EmailLogService emailLogService, UserService userService) {
//        this.emailLogService = emailLogService;
//        this.userService = userService;
//    }
/////
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
//        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
//
//        //查询邮件详情
//        if (request.method() == HttpMethod.GET && "/api/email/queryonelog".equals(decoder.path())) {
//            try {
//                // 解析查询参数
//                Map<String, String> params = decoder.parameters().entrySet().stream()
//                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
//
//                // 获取分页参数
//                int page = Integer.parseInt(params.getOrDefault("page_num", "0"));
//                int size = Integer.parseInt(params.getOrDefault("page_size", "10"));
//
//                String emailTaskId =params.get("emailTaskId");
//
//                // 移除分页参数
//                params.remove("page_num");
//                params.remove("page_size");
//
//                // 使用动态参数查询
//
//                List<UndeliveredEmail> logList = emailLogService.findByDynamicQueryEmail(params, page, size);
//                EmailTask emailTask=emailLogService.findByEmailTaskId(emailTaskId);
//                List<EmailVo> emailVoList=logList.stream().map(email -> {
//                    EmailVo emailVo=new EmailVo();
//                    emailVo.setSubject(emailTask.getSubject());
//                    emailVo.setTaskType(emailTask.getSubject());
//                    BeanUtils.copyProperties(email, emailVo);
//                    emailVo.setLevel("高级");
//                    emailVo.setErrorCode(email.getErrorCode());
//                    emailVo.setErrorMsg(email.getErrorMsg());
//                    emailVo.setStartDate(dateTimeFormatter(email.getStartDate()));
//                    emailVo.setEndDate(dateTimeFormatter(email.getEndDate()));
//                    try {
//                        User sender=userService.findByUserEmail(email.getSenderId()[0]);
//                        User receiver=userService.findByUserEmail(email.getReceiverId()[0]);
//                        emailVo.setSenderEmail(sender.getUserEmail());
//                        emailVo.setSenderName(sender.getUserName());
//                        emailVo.setReceiverEmail(receiver.getUserEmail());
//                        emailVo.setReceiverName(receiver.getUserName());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                    return emailVo;
//                }).toList();
//
//
//                // 转换为JSON并返回
//                String responseContent = objectMapper.writeValueAsString(emailVoList);
//                sendResponse(ctx, HttpResponseStatus.OK, responseContent);
//            } catch (Exception e) {
//                log.error("Error processing request", e);
//                sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
//            }
//        } else {
//            ctx.fireChannelRead(request.retain());
//        }
//    }
//
//    private String dateTimeFormatter(long secondsTimestamp) {
//        // 将秒级时间戳转换为 LocalDateTime
//        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(secondsTimestamp), ZoneId.systemDefault());
//
//        // 格式化日期为字符串
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        return dateTime.format(formatter);
//    }
//
//    private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
//        FullHttpResponse response = new DefaultFullHttpResponse(
//                HttpVersion.HTTP_1_1,
//                status,
//                Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
//        );
//
//        response.headers()
//                .set(HttpHeaderNames.CONTENT_TYPE, "application/json")
//                .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
//
//        ctx.writeAndFlush(response);
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        log.error("Channel exception caught", cause);
//        sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
//        ctx.close();
//    }
//}
