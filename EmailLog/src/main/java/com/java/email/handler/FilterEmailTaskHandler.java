package com.java.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.result.Result;
import com.java.email.service.EmailManageService;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.User;
import com.java.email.service.EmailLogService;
import com.java.email.service.UserService;
import com.java.email.vo.FilterTaskVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

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
    private final EmailManageService emailManageService;
    public FilterEmailTaskHandler(EmailLogService emailLogService, UserService userService, EmailManageService emailManageService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
        this.emailManageService = emailManageService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        // 处理 GET 请求，查询邮件任务
        if (request.method() == HttpMethod.GET && "/emailManage/filterTask".equals(decoder.path())) {
            try {
                List<String> managedUserEmails=null;
                // 获取用户信息
                Map<String, Object> userInfo = ThreadLocalUtil.get();
                if (userInfo == null) {
                    sendResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "未登录");
                    return;
                }

                Integer userRole = (Integer) userInfo.get("role");
                String userEmail = userService.findById(ThreadLocalUtil.getUserId()).getUserEmail() ;


                if (userRole == null || userEmail == null) {
                    sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "无法获取用户信息");
                    return;
                }

                // 解析查询参数
                Map<String, String> params = decoder.parameters().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

                // 根据用户角色添加权限限制
                switch (userRole) {
                    case 2: // 大管理员，不需要额外限制
                        break;
                    case 3: // 小管理员，只能查看自己管理的用户的邮件
                        managedUserEmails = userService.findManagedUserEmails((String) userInfo.get("id"));
                        if (params.containsKey("sender_id")) {
                            String requestedSender = params.get("sender_id");
                            if (!managedUserEmails.contains(requestedSender)) {
                                sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "无权查看该用户的邮件");
                                return;
                            }
                        } else {
                            params.put("senderIds", String.join(",", managedUserEmails));
                        }
                        break;
                    case 4: // 普通用户，只能查看自己的邮件
                        params.put("sender_id", userEmail);
                        break;
                    default:
                        sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "未知的用户角色");
                        return;
                }

                // 获取分页参数
                int page = Integer.parseInt(params.getOrDefault("page_num", "0"));
                int size = Integer.parseInt(params.getOrDefault("page_size", "5"));

//                // 处理发件人名称查询
//                if (params.containsKey("senderName")) {
//                    String senderEmail = userService.findUserEmailByUserName(params.get("senderName"));
//                    if (senderEmail != null) {
//                        params.put("senderId", senderEmail);
//                    }
//                    params.remove("senderName");
//                }

                // 移除分页参数
                params.remove("page_num");
                params.remove("page_size");

                try {
                    // 使用新的查询方法
                    List<EmailTask> emailTaskList = emailLogService.findByDynamicQueryEmailTask(
                            params, page, size, userRole, userEmail, managedUserEmails);

                    // 转换为VO对象
                    List<FilterTaskVo> emailFilterTaskVoList = emailTaskList.stream()
                            .map(emailTask -> {
                                FilterTaskVo filterTaskVo = new FilterTaskVo();
                                BeanUtils.copyProperties(emailTask, filterTaskVo);
                                filterTaskVo.setStartDate(dateTimeFormatter(emailTask.getStartDate()));
                                filterTaskVo.setEndDate(dateTimeFormatter(emailTask.getEndDate()));
                                filterTaskVo.setTaskStatus(emailManageService.findLatestStatusByTaskId(emailTask.getEmailTaskId()));
                                filterTaskVo.setPage(page);
                                filterTaskVo.setSize(size);
                                if (emailTask.getSenderId() != null ) {

                                        filterTaskVo.setSenderName(emailTask.getSenderName());
                                        filterTaskVo.setSenderEmail(emailTask.getSenderId());

                                }
                                return filterTaskVo;
                            })
                            .collect(Collectors.toList());

                    // 返回结果
                    String responseContent = objectMapper.writeValueAsString(Result.ok(emailFilterTaskVoList));
                    sendResponse(ctx, HttpResponseStatus.OK, responseContent);

                } catch (IllegalArgumentException e) {
                    // 处理权限验证失败的情况
                    sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "权限不足: " + e.getMessage());
                }

            } catch (Exception e) {
                log.error("Error processing request", e);
                sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
            }
        } else {
            ctx.fireChannelRead(request.retain());
        }
    }

    private String dateTimeFormatter(long secondsTimestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(secondsTimestamp), 
                ZoneId.systemDefault()
        );
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
