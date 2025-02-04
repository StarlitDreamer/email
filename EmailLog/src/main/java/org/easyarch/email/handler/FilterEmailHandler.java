package org.easyarch.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.easyarch.email.pojo.Email;
import org.easyarch.email.pojo.EmailTask;
import org.easyarch.email.pojo.UndeliveredEmail;
import org.easyarch.email.pojo.User;
import org.easyarch.email.result.EmailStatusEnum;
import org.easyarch.email.service.EmailLogService;
import org.easyarch.email.service.UserService;
import org.easyarch.email.vo.EmailVo;
import org.easyarch.email.vo.FilterEmailVo;
import org.easyarch.email.vo.FilterTaskVo;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FilterEmailHandler(EmailLogService emailLogService, UserService userService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());

        if (request.method() == HttpMethod.GET && "/emailManage/filterEmail".equals(decoder.path())) {
            try {
                // 解析查询参数
                Map<String, String> params = decoder.parameters().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));

                // 获取分页参数
                int page = Integer.parseInt(params.getOrDefault("page_num", "0"));
                int size = Integer.parseInt(params.getOrDefault("page_size", "10"));

                // 获取所有符合条件的邮件任务
                List<EmailTask> emailTasks = emailLogService.findByEmailTasks(params);

                // 处理发件人和收件人名称查询
                if(params.containsKey("senderName")){
                    params.put("senderId", userService.findUserEmailByUserName(params.get("senderName")));
                }
                if(params.containsKey("receiverName")){
                    params.put("receiverId", userService.findUserEmailByUserName(params.get("receiverName")));
                }

                // 移除分页参数
                params.remove("page_num");
                params.remove("page_size");

                // 收集所有邮件记录
                List<FilterEmailVo> allEmailVos = emailTasks.stream()
                    .flatMap(emailTask -> {
                        try {
                            params.put("emailTaskId", emailTask.getEmailTaskId());
                            List<UndeliveredEmail> logList = emailLogService.findByDynamicQueryEmail(params, page,size); // 获取所有记录
                            params.remove("emailTaskId");
                            
                            return logList.stream().map(email -> {
                                FilterEmailVo emailVo = new FilterEmailVo();
                                emailVo.setSubject(emailTask.getSubject());
                                emailVo.setTaskType(emailTask.getSubject());
                                emailVo.setEmailTaskId(emailTask.getEmailTaskId());
                                BeanUtils.copyProperties(email, emailVo);
                                emailVo.setReceiverLevel(1);
                                emailVo.setStartDate(dateTimeFormatter(email.getStartDate()));
                                emailVo.setEndDate(dateTimeFormatter(email.getEndDate()));
                                
                                try {
                                    User sender = userService.findByUserEmail(email.getSenderId()[0]);
                                    User receiver = userService.findByUserEmail(email.getReceiverId()[0]);
                                    emailVo.setSenderEmail(sender.getUserEmail());
                                    emailVo.setSenderName(sender.getUserName());
                                    emailVo.setReceiverEmail(receiver.getUserEmail());
                                    emailVo.setReceiverName(receiver.getUserName());
                                    emailVo.setReceiverBirth(receiver.getCreatedAt());
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

                // 为分页后的结果设置分页信息
                pagedResults.forEach(emailVo -> {
                    emailVo.setPage(page);
                    emailVo.setSize(size);
                });

                // 转换为JSON并返回
                String responseContent = objectMapper.writeValueAsString(pagedResults);
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
