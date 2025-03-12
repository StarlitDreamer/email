package com.java.email.handler;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.common.Redis.RedisService;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.RsendDetails;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.result.Result;
import com.java.email.service.EmailLogService;
import com.java.email.service.EmailManageService;
import com.java.email.service.EmailRecipientService;
import com.java.email.service.UserService;
import com.java.email.vo.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
@Slf4j
public class FilterUndeliveredEmailHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmailLogService emailLogService;
    private final EmailRecipientService emailRecipientService;
    private final RedisService redisService;

    public FilterUndeliveredEmailHandler(UserService userService, EmailLogService emailLogService, EmailRecipientService emailRecipientService, EmailManageService emailManageService, RedisService redisService) {
        this.userService = userService;
        this.emailRecipientService = emailRecipientService;
        this.emailLogService = emailLogService;
        this.redisService = redisService;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        if (request.method() == HttpMethod.GET && "/emailDetails/undelivered-emails".equals(decoder.path())) {
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
                        managedUserEmails =  objectMapper.readValue(redisService.get(cacheKey),new TypeReference<List<String>>(){});
                        if (managedUserEmails == null) {
                            managedUserEmails = userService.findManagedUserEmails(userId);
                            redisService.set(cacheKey, managedUserEmails, 1, TimeUnit.HOURS);
                        }
                        if (params.containsKey("sender_id")) {
                            String requestedSender = params.get("sender_id");
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

//                // 获取所有符合条件的邮件任务
                List<String> emailTaskIds = emailLogService.findByEmailTasksIds(params, userRole, userEmail, managedUserEmails);

                // 收集所有邮件记录
                List<String> finalManagedUserEmails = managedUserEmails;
                EmailVo emailVo = emailLogService.findByDynamicQueryUndeliveredEmail(
                        params,
                        emailTaskIds,
                        page,  // from
                        size,  // 使用请求的大小和最大限制中的较小值
                        userRole,
                        userEmail,
                        finalManagedUserEmails
                );

                List<UndeliveredEmail> logList = emailVo.getEmailList();

                List<FilterRsendEmailVo> Results =logList.stream().map(email -> {
                    EmailTask emailTask = null;
                    FilterRsendEmailVo filterEmailVo;
                    try {
                        emailTask = emailLogService.findByEmailTaskId(email.getEmailTaskId());
                        filterEmailVo = new FilterRsendEmailVo();
                        BeanUtils.copyProperties(email, filterEmailVo);
                        filterEmailVo.setSubject(emailTask.getSubject());
                        filterEmailVo.setTask_type(emailTask.getTaskType());
                        filterEmailVo.setEmailTaskId(emailTask.getEmailTaskId());

                        filterEmailVo.setEmail_type_name(emailLogService.findByEmailTypeName(emailTask.getEmailTypeId()));


                        Map<String, String> receiverInfo;
                        try {
                            if (params.containsKey("receiver_level") || params.containsKey("receiver_birth")) {
                                String paramHash = generateParamHash(params); // 生成参数哈希
                                String receiverCacheKey = "recipient:" + email.getReceiverId() + ":" + paramHash;
                                receiverInfo = objectMapper.readValue(redisService.get(receiverCacheKey), new TypeReference<Map<String, String>>() {
                                });

                                if (receiverInfo == null) {
                                    receiverInfo = emailRecipientService.getRecipientDetail(email.getReceiverId(), params);
                                    redisService.set(receiverCacheKey, receiverInfo, 6, TimeUnit.HOURS);
                                }
                            } else {
                                String receiverCacheKey = "recipient:" + email.getReceiverId();
                                receiverInfo = objectMapper.readValue(redisService.get(receiverCacheKey), new TypeReference<Map<String, String>>() {
                                });
                                if (receiverInfo == null) {
                                    receiverInfo = emailRecipientService.getRecipientDetail(email.getReceiverId());
                                    redisService.set(receiverCacheKey, receiverInfo, 24, TimeUnit.HOURS);
                                }
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
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

                        RsendDetails resendInfo;
                        String  resendCacheKey = "resend:" + email.getEmailId();
                        resendInfo=objectMapper.readValue(redisService.get(resendCacheKey),RsendDetails.class);
                        if(resendInfo==null){
                            resendInfo=emailRecipientService.getResendDetails(email.getEmailId());
                            redisService.set(resendCacheKey,resendInfo,1,TimeUnit.HOURS);
                        }
                        if (resendInfo != null) {
                            filterEmailVo.setResend_start_date(resendInfo.getStartTime());
                            filterEmailVo.setResend_end_date(resendInfo.getEndTime());
                            filterEmailVo.setResend_status(resendInfo.getStatus());
                            filterEmailVo.setResend_msg(resendInfo.getErrorMsg());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    return filterEmailVo;
                }).toList();


                ResultRsenEmailVo resultEmailVo=new ResultRsenEmailVo();
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
