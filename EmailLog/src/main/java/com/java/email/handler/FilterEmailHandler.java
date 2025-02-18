package com.java.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.common.userCommon.ThreadLocalUtil;
import com.java.email.pojo.Customer;
import com.java.email.result.Result;
import com.java.email.service.CustomerService;
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
public class FilterEmailHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final EmailLogService emailLogService;
    private final UserService userService;
    private final CustomerService customerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_PAGE_SIZE = 10000;  // ES默认最大返回10000条

    public FilterEmailHandler(EmailLogService emailLogService, UserService userService, CustomerService customerService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
        this.customerService = customerService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        if (request.method() == HttpMethod.GET && "/emailManage/filterEmail".equals(decoder.path())) {
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
                        if (params.containsKey("senderId")) {
                            String requestedSender = params.get("senderId");
                            if (!managedUserEmails.contains(requestedSender)) {
                                sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "无权查看该用户的邮件");
                                return;
                            }
                        } else {
                            params.put("senderIds", String.join(",", managedUserEmails));
                        }
                        break;
                    case 4: // 普通用户，只能查看自己的邮件
                        params.put("senderId", userEmail);
                        break;
                    default:
                        sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "未知的用户角色");
                        return;
                }

                // 获取分页参数
                int page = Integer.parseInt(params.getOrDefault("page_num", "0"));
                int size = Integer.parseInt(params.getOrDefault("page_size", "10"));

//                // 处理发件人和收件人名称查询
//                if(params.containsKey("senderName")){
//                    String senderEmail = userService.findUserEmailByUserName(params.get("senderName"));
//                    // 验证权限
//                    if (userRole == 4 && !senderEmail.equals(userEmail)) {
//                        sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "只能查看自己的邮件");
//                        return;
//                    } else if (userRole == 3) {
//                        managedUserEmails = userService.findManagedUserEmails(userEmail);
//                        if (!managedUserEmails.contains(senderEmail)) {
//                            sendResponse(ctx, HttpResponseStatus.FORBIDDEN, "无权查看该用户的邮件");
//                            return;
//                        }
//                    }
//                    params.put("senderId", senderEmail);
//                }

//                if(params.containsKey("receiverName")){
//                    params.put("receiverId", userService.findUserEmailByUserName(params.get("receiverName")));
//                }

                // 移除分页参数
                params.remove("page_num");
                params.remove("page_size");

                // 获取所有符合条件的邮件任务
                List<EmailTask> emailTasks = emailLogService.findByEmailTasks(params, userRole, userEmail, managedUserEmails);

                // 收集所有邮件记录
                List<String> finalManagedUserEmails = managedUserEmails;
                List<FilterEmailVo> allEmailVos = emailTasks.stream()
                    .flatMap(emailTask -> {
                        try {
                            params.put("emailTaskId", emailTask.getEmailTaskId());
                            List<UndeliveredEmail> logList = emailLogService.findByDynamicQueryEmail(
                                params, 
                                0,  // from
                                MAX_PAGE_SIZE,  // 使用请求的大小和最大限制中的较小值
                                userRole, 
                                userEmail, 
                                finalManagedUserEmails
                            );
                            params.remove("emailTaskId");
                            
                            return logList.stream().map(email -> {
                                FilterEmailVo emailVo = new FilterEmailVo();
                                emailVo.setSubject(emailTask.getSubject());
                                emailVo.setTaskType(emailTask.getSubject());
                                emailVo.setEmailTaskId(emailTask.getEmailTaskId());
                                BeanUtils.copyProperties(email, emailVo);
                                Customer customer=null;
                                try {
                                    customer=customerService.findCustomerByEmail(email.getReceiverId()[0]);
                                    emailVo.setReceiverLevel(customer.getCustomerLevel());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                emailVo.setEmailStatus(email.getErrorCode());
                                emailVo.setStartDate(dateTimeFormatter(email.getStartDate()));
                                emailVo.setEndDate(dateTimeFormatter(email.getEndDate()));
                                
                                try {
//                                 User sender = userService.findByUserEmail(email.getSenderId()[0]);
                                    User receiver = userService.findByUserEmail(email.getReceiverId()[0]);
                                    emailVo.setSenderEmail(email.getSenderId()[0]);
                                    emailVo.setSenderName(email.getSenderName());
                                    emailVo.setReceiverEmail(receiver.getUserEmail());
                                    emailVo.setReceiverName(receiver.getUserName());
                                    emailVo.setReceiverBirth(customer.getBirth());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return emailVo;
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

                // 对总结果进行分页
                int fromIndex = page * size;
                int toIndex = Math.min(fromIndex + size, allEmailVos.size());
                
                List<FilterEmailVo> pagedResults = fromIndex < allEmailVos.size() 
                    ? allEmailVos.subList(fromIndex, toIndex)
                    : List.of();

                // 设置分页信息
                pagedResults.forEach(emailVo -> {
                    emailVo.setPage(page);
                    emailVo.setSize(size);
                });

                // 返回结果
                String responseContent = objectMapper.writeValueAsString(Result.ok(pagedResults));
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
