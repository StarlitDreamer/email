package com.java.email.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.common.Redis.RedisService;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.pojo.Customer;
import com.java.email.result.Result;
import com.java.email.service.CustomerService;
import com.java.email.service.EmailRecipientService;
import com.java.email.vo.EmailVo;
import com.java.email.vo.ResultEmailVo;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.pojo.User;
import com.java.email.service.EmailLogService;
import com.java.email.service.UserService;
import com.java.email.vo.FilterEmailVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
/**
 * @author EvoltoStar
 */

@Slf4j
public class FilterBirthEmailHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final EmailLogService emailLogService;
    private final UserService userService;
    private final EmailRecipientService emailRecipientService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisService redisService;
    private static final int MAX_PAGE_SIZE = 10000;  // ES默认最大返回10000条

    public FilterBirthEmailHandler(EmailLogService emailLogService, UserService userService, EmailRecipientService emailRecipientService, RedisService redisService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
        this.emailRecipientService = emailRecipientService;
        this.redisService = redisService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        if (request.method() == HttpMethod.GET && "/emailManage/filterBirthEmail".equals(decoder.path())) {
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

                if (userRole == null || userId == null) {
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
                                String managedUserEmailsJson=objectMapper.writeValueAsString(managedUserEmails);
                                redisService.set(cacheKey, managedUserEmailsJson, 30, TimeUnit.MINUTES);
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
                String subject = null;
                if(params.containsKey("subject")){
                    subject = params.get("subject");
                    params.remove("subject");
                }

                EmailTask cachedTask = null;
                // 生日任务缓存（固定任务类型）
                String taskCacheKey = "birth_task:" + userId;
                String cacheValue = redisService.get(taskCacheKey);
                if(cacheValue!=null){
                    try {
                        cachedTask = objectMapper.readValue(cacheValue, EmailTask.class) ;
                    }catch (Exception e){
                        log.error("redis缓存解析失败",e);
                        params.put("email_task_id", "birth");
                        params.put("task_type", "4");
                        cachedTask = emailLogService.findByEmailTasks(params, userRole, userEmail, managedUserEmails);
                        if(cachedTask!=null){
                            String cachedTaskJson = objectMapper.writeValueAsString(cachedTask);
                            redisService.set(taskCacheKey, cachedTaskJson, 30, TimeUnit.MINUTES);
                        }

                    }
                }else {
                    params.put("email_task_id", "birth");
                    params.put("task_type", "4");
                    cachedTask = emailLogService.findByEmailTasks(params, userRole, userEmail, managedUserEmails);
                    if(cachedTask!=null){
                        String cachedTaskJson = objectMapper.writeValueAsString(cachedTask);
                        redisService.set(taskCacheKey, cachedTaskJson, 30, TimeUnit.MINUTES);
                    }
                }

                // 收集所有邮件记录
                if(subject!=null){
                    params.put("subject",subject);
                }

                List<String> finalManagedUserEmails = managedUserEmails;
                assert cachedTask != null;
                EmailVo emailVo = emailLogService.findByDynamicQueryBirthEmail(
                        params,
                        page,  // from
                        size,  // 使用请求的大小和最大限制中的较小值
                        userRole,
                        userEmail,
                        finalManagedUserEmails,
                        cachedTask.getEmailTaskId()
                );

                List<UndeliveredEmail> logList = emailVo.getEmailList();

                EmailTask finalCachedTask = cachedTask;
                List<FilterEmailVo> Results =logList.stream().map(email -> {
                    FilterEmailVo filterEmailVo = new FilterEmailVo();
                    filterEmailVo.setSubject(email.getSubject());
                    filterEmailVo.setTask_type(finalCachedTask.getTaskType());
                    filterEmailVo.setEmailTaskId(finalCachedTask.getEmailTaskId());
                    BeanUtils.copyProperties(email, filterEmailVo);
                    Map<String, String> receiverInfo = null;
                    String receiverCacheKey;
                    int cacheDuration = 60; // 默认缓存时间 1 小时

                    if (params.containsKey("receiver_level") || params.containsKey("receiver_birth")) {
                        String paramHash = generateParamHash(params);
                        receiverCacheKey = "recipient:" + email.getReceiverId() + ":" + paramHash;
                        cacheDuration = 30; // 特殊参数时缓存 30 分钟
                    } else {
                        receiverCacheKey = "recipient:" + email.getReceiverId();
                    }

                    String receiverCacheValue = redisService.get(receiverCacheKey);

                    if (receiverCacheValue != null) {
                        try {
                            receiverInfo = objectMapper.readValue(receiverCacheValue, new TypeReference<Map<String, String>>() {});
                        } catch (Exception e) {
                            log.error("Error parsing receiverInfo from cache: {}", e.getMessage());
                        }
                    }

                    if (receiverInfo == null) {
                        receiverInfo = params.containsKey("receiver_level") || params.containsKey("receiver_birth")
                                ? emailRecipientService.getRecipientDetail(email.getReceiverId(), params)
                                : emailRecipientService.getRecipientDetail(email.getReceiverId());

                        if (receiverInfo != null) {
                            try {
                                String receiverInfoJson = objectMapper.writeValueAsString(receiverInfo);
                                redisService.set(receiverCacheKey, receiverInfoJson, cacheDuration, TimeUnit.MINUTES);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }

                    filterEmailVo.setEmail_status(email.getErrorCode());
                    filterEmailVo.setError_msg(email.getErrorMsg());
                    filterEmailVo.setStart_date(dateTimeFormatter(email.getStartDate()));
                    filterEmailVo.setEnd_date(dateTimeFormatter(email.getEndDate()));
                    filterEmailVo.setReceiver_level(Long.parseLong(receiverInfo.get("level")));

                    filterEmailVo.setSender_email(email.getSenderId());
                    filterEmailVo.setSender_name(email.getSenderName());
                    filterEmailVo.setReceiver_email(email.getReceiverId());
                    filterEmailVo.setReceiver_name(email.getReceiverName());
                    filterEmailVo.setReceiver_birth(receiverInfo.get("birth"));
                    return filterEmailVo;
                }).toList();


                ResultEmailVo resultEmailVo=new ResultEmailVo();
                resultEmailVo.setTotal_items(emailVo.getTotal());
                resultEmailVo.setPage_num(page);
                resultEmailVo.setPage_size(size);
                resultEmailVo.setEmail_info(Results);


                // 返回结果
                String responseContent = objectMapper.writeValueAsString(Result.ok(resultEmailVo));
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

    // 生成参数哈希方法
    private String generateParamHash(Map<String, String> params) {
        try {
            return params.entrySet().stream()
                    .filter(e -> e.getKey().startsWith("receiver_"))
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
        } catch (Exception e) {
            return "default_hash";
        }
    }
}
