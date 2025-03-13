package com.java.email.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.result.Result;
import com.java.email.service.EmailManageService;
import com.java.email.vo.EmailTaskVo;
import com.java.email.vo.ResultEmailTaskVo;
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
import com.java.email.common.Redis.RedisService;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * @author EvoltoStar
 */
@Slf4j
public class FilterEmailTaskHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final EmailLogService emailLogService;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmailManageService emailManageService;
    private final RedisService redisService;

    public FilterEmailTaskHandler(EmailLogService emailLogService, UserService userService, EmailManageService emailManageService, RedisService redisService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
        this.emailManageService = emailManageService;
        this.redisService = redisService;
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
                String userId = ThreadLocalUtil.getUserId();


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
                        String cacheKey = "managed_users:" + userId;
                        String cacheValue = redisService.get(cacheKey);

                        if (cacheValue != null) {
                            try {
                                managedUserEmails = objectMapper.readValue(cacheValue, new TypeReference<List<String>>() {});
                            } catch (Exception e) {
                                log.error("Redis 缓存解析失败", e);
                            }
                        }

                        if (managedUserEmails == null) {
                            managedUserEmails = userService.findManagedUserEmails(userId);
                            if (managedUserEmails != null) {
                                redisService.set(cacheKey, managedUserEmails, 1, TimeUnit.HOURS);
                            }
                        }

                        if (params.containsKey("sender_id")) {
                            String requestedSender = params.get("sender_id");
                            assert managedUserEmails != null;
                            if (!managedUserEmails.contains(requestedSender)) {
                                sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "无权查看该用户的邮件");
                                return;
                            }
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
                int page = Integer.parseInt(params.getOrDefault("page_num", "1"));
                int size = Integer.parseInt(params.getOrDefault("page_size", "5"));

                // 移除分页参数
                params.remove("page_num");
                params.remove("page_size");

                try {
                    // 使用新的查询方法
                    EmailTaskVo emailTaskVo = emailLogService.findByDynamicQueryEmailTask(
                            params, page, size, userRole, userEmail, managedUserEmails);
                    List<EmailTask> emailTaskList =  emailTaskVo.getEmailTask();


                    // 转换为VO对象
                    List<FilterTaskVo> emailFilterTaskVoList = emailTaskList.stream()
                            .map(emailTask -> {
                                FilterTaskVo filterTaskVo = new FilterTaskVo();
                                BeanUtils.copyProperties(emailTask, filterTaskVo);
                                filterTaskVo.setTask_id(emailTask.getEmailTaskId());
                                filterTaskVo.setStart_date(dateTimeFormatter(emailTask.getStartDate()));
                                filterTaskVo.setEnd_date(dateTimeFormatter(emailTask.getEndDate()));


                                try {
                                    // 邮件类型名称缓存
                                    String typeCacheKey = "email_type:" + emailTask.getEmailTypeId();
                                    String emailTypeName = redisService.get(typeCacheKey);
                                    if (emailTypeName == null) {
                                        emailTypeName = emailLogService.findByEmailTypeName(emailTask.getEmailTypeId());
                                        if(emailTypeName != null){
                                            redisService.set(typeCacheKey, emailTypeName, 1, TimeUnit.HOURS);
                                        }
                                    }
                                    filterTaskVo.setEmail_type_name(emailTypeName);

                                    // 任务状态缓存（带自动过期）
                                    String statusKey = "task_status:" + emailTask.getEmailTaskId();
                                    String statusValue = redisService.get(statusKey);
                                    Long taskStatus=null;

                                    if(statusValue!=null){
                                        try {
                                            taskStatus = objectMapper.readValue(statusValue, Long.class);
                                        }catch (Exception e){
                                            log.error("Redis 缓存解析失败", e);
                                        }
                                    }
                                    if (taskStatus == null) {
                                        // 如果缓存中没有值，从数据库获取
                                        taskStatus = emailManageService.findLatestStatusByTaskId(emailTask.getEmailTaskId());
                                        // 将新的状态存入缓存
                                        if (taskStatus != null) {
                                            redisService.set(statusKey, objectMapper.writeValueAsString(taskStatus), 1, TimeUnit.HOURS);
                                        }
                                    }


                                    filterTaskVo.setTask_status(taskStatus);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                if (emailTask.getSenderId() != null ) {

                                        filterTaskVo.setSender_name(emailTask.getSenderName());
                                        filterTaskVo.setSender_email(emailTask.getSenderId());

                                }
                                return filterTaskVo;
                            })
                            .collect(Collectors.toList());

                    ResultEmailTaskVo resultEmailTaskVo = new ResultEmailTaskVo();
                    resultEmailTaskVo.setTask_info(emailFilterTaskVoList);
                    resultEmailTaskVo.setPage_num(page);
                    resultEmailTaskVo.setPage_size(size);
                    resultEmailTaskVo.setTotal_items(emailTaskVo.getTotal());

                    // 返回结果
                    String responseContent = objectMapper.writeValueAsString(Result.ok(resultEmailTaskVo));
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
