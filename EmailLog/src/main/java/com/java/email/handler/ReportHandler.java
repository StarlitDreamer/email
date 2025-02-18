package com.java.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.common.userCommon.ThreadLocalUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import com.java.email.pojo.EmailTask;
import com.java.email.pojo.UndeliveredEmail;
import com.java.email.result.Result;
import com.java.email.service.EmailLogService;
import com.java.email.vo.ReportVo;
import com.java.email.service.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;



@Slf4j
public class ReportHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final EmailLogService emailLogService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private static final int MAX_PAGE_SIZE = 10000;
    
    public ReportHandler(EmailLogService emailLogService, UserService userService) {
        this.emailLogService = emailLogService;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        if (request.method() != HttpMethod.GET || !"/reportManage/checkSingleReport".equals(decoder.path())) {
            ctx.fireChannelRead(request.retain());
            return;
        }

        try {
            handleReportRequest(ctx, decoder);
        } catch (Exception e) {
            log.error("处理请求时发生错误", e);
            sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    private void handleReportRequest(ChannelHandlerContext ctx, QueryStringDecoder decoder) throws Exception {
        // 获取用户信息
        Map<String, Object> userInfo = ThreadLocalUtil.get();
        if (userInfo == null) {
            sendResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "未登录");
            return;
        }

        Integer userRole = (Integer) userInfo.get("role");
        String userEmail = userService.findById(ThreadLocalUtil.getUserId()).getUserEmail() ;
        
        if (userRole == null || userEmail == null) {
            sendResponse(ctx, HttpResponseStatus.FORBIDDEN, 
                objectMapper.writeValueAsString(Result.fail("无法获取用户信息")));
            return;
        }

        // 获取管理的用户邮箱列表
        List<String> managedUserEmails = userRole == 3 ? 
            userService.findManagedUserEmails((String) userInfo.get("id")) : Collections.emptyList();

        // 参数验证
        String emailTaskId = getRequiredParameter(decoder, "emailTaskId");
        if (emailTaskId == null) {
            sendResponse(ctx, HttpResponseStatus.BAD_REQUEST, 
                objectMapper.writeValueAsString(Result.fail("缺少必要的参数: emailTaskId")));
            return;
        }

        // 构建查询参数
        Map<String, String> params = new HashMap<>();
        params.put("emailTaskId", emailTaskId);

        // 获取邮件任务
        List<EmailTask> emailTasks = emailLogService.findByDynamicQueryEmailTask(
            params, 0, 1, userRole, userEmail, managedUserEmails);
        
        if (emailTasks.isEmpty()) {
            sendResponse(ctx, HttpResponseStatus.NOT_FOUND, 
                objectMapper.writeValueAsString(Result.fail("未找到指定的邮件任务")));
            return;
        }

        EmailTask emailTask = emailTasks.get(0);

        // 获取任务相关的所有邮件
        List<UndeliveredEmail> emailList = emailLogService.findByDynamicQueryEmail(
            params, 0, MAX_PAGE_SIZE, userRole, userEmail, managedUserEmails);

        // 统计数据
        long totalEmailCount = emailList.size();
        long totalSendNum = emailList.stream()
            .filter(email -> email.getErrorCode() == 200L)
            .count();
        long totalBounceAmount = emailTask.getBounceAmount();
        long totalUnsubscribeAmount = emailTask.getUnsubscribeAmount();

        // 构建报表数据
        ReportVo reportVo = new ReportVo();
        
        // 设置投递率
        ReportVo.Delivery delivery = new ReportVo.Delivery();
        delivery.setRate(calculateRate(totalSendNum, totalEmailCount));
        delivery.setDeliveryAmount(formatAmount(totalSendNum));
        delivery.setTotal(totalEmailCount);
        reportVo.setDelivery(delivery);
        
        // 设置退信率
        ReportVo.Bounce bounce = new ReportVo.Bounce();
        bounce.setRate(calculateRate(totalBounceAmount, totalEmailCount));
        bounce.setBounceAmount(formatAmount(totalBounceAmount));
        bounce.setTotal(totalEmailCount);
        reportVo.setBounce(bounce);
        
        // 设置退订率
        ReportVo.Unsubscribe unsubscribe = new ReportVo.Unsubscribe();
        unsubscribe.setRate(calculateRate(totalUnsubscribeAmount, totalEmailCount));
        unsubscribe.setUnsubscribeAmount(formatAmount(totalUnsubscribeAmount));
        unsubscribe.setTotal(totalEmailCount);
        reportVo.setUnsubscribe(unsubscribe);

        // 返回响应
        sendResponse(ctx, HttpResponseStatus.OK, objectMapper.writeValueAsString(Result.ok(reportVo)));
    }

    private double calculateRate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.00;
        }
        return BigDecimal.valueOf((double) numerator / denominator * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double formatAmount(double amount) {
        return BigDecimal.valueOf(amount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String getRequiredParameter(QueryStringDecoder decoder, String name) {
        List<String> values = decoder.parameters().get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
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

