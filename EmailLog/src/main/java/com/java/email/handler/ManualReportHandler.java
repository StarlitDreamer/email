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
import com.java.email.result.Result;
import com.java.email.service.EmailLogService;
import com.java.email.vo.ReportVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ManualReportHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final EmailLogService emailLogService;
    private final ObjectMapper objectMapper;

    public ManualReportHandler(EmailLogService emailLogService) {
        this.emailLogService = emailLogService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        if (request.method() != HttpMethod.GET || !"/api/email/manual-report".equals(decoder.path())) {
            ctx.fireChannelRead(request.retain());
            return;
        }

        try {
            handleManualReportRequest(ctx, decoder);
        } catch (Exception e) {
            log.error("处理请求时发生错误", e);
            sendResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    private void handleManualReportRequest(ChannelHandlerContext ctx, QueryStringDecoder decoder) throws Exception {
        // 参数验证
        String startTime = getRequiredParameter(decoder, "startTime");
        String endTime = getRequiredParameter(decoder, "endTime");
        
        if (startTime == null || endTime == null) {
            sendResponse(ctx, HttpResponseStatus.BAD_REQUEST, 
                objectMapper.writeValueAsString(Result.fail("缺少必要的参数: startTime 或 endTime")));
            return;
        }

        // 构建查询参数
        Map<String, String> params = new HashMap<>();
        params.put("startDate", startTime);
        params.put("endDate", endTime);

        // 获取时间范围内的所有手动发送任务
        List<EmailTask> emailTasks = emailLogService.findByEmailTasks(params);
        
        // 汇总统计数据
        long totalEmailCount = 0;
        long totalSendNum = 0;
        long totalBounceAmount = 0;
        long totalUnsubscribeAmount = 0;

        // 遍历所有任务并汇总数据
        for (EmailTask emailTask : emailTasks) {
            List<UndeliveredEmail> emailList = emailLogService.findAllEmail(emailTask.getEmailTaskId());
            
            totalEmailCount += emailList.size();
            totalSendNum += emailList.stream().filter(email -> email.getErrorCode() == 5).count();
            totalBounceAmount += emailTask.getBounceAmount();
            totalUnsubscribeAmount += emailTask.getUnsubscribeAmount();
        }

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