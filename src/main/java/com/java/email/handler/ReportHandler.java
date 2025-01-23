package com.java.email.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.email.pojo.Email;
import com.java.email.pojo.EmailTask;
import com.java.email.result.Result;
import com.java.email.service.EmailLogService;
import com.java.email.vo.ReportVo;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;



@Slf4j
public class ReportHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final EmailLogService emailLogService;
    private final ObjectMapper objectMapper;
    
    public ReportHandler(EmailLogService emailLogService) {
        this.emailLogService = emailLogService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        if (request.method() != HttpMethod.GET || !"/api/email/report".equals(decoder.path())) {
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
        // 参数验证
        String emailTaskId = getRequiredParameter(decoder, "emailTaskId");
        if (emailTaskId == null) {
            sendResponse(ctx, HttpResponseStatus.BAD_REQUEST, 
                objectMapper.writeValueAsString(Result.fail("缺少必要的参数: emailTaskId")));
            return;
        }

        // 获取数据
        EmailTask emailTask = emailLogService.findByEmailTaskId(emailTaskId);
        List<Email> emailList = emailLogService.findAllEmail(emailTaskId);
        
        // 计算统计数据
        ReportVo reportVo = calculateReport(emailTask, emailList);


        // 返回响应
        sendResponse(ctx, HttpResponseStatus.OK, objectMapper.writeValueAsString(Result.ok(reportVo)));
    }

    private double calculateRate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.00;
        }
        // 使用 BigDecimal 处理精度
        BigDecimal rate = BigDecimal.valueOf((double) numerator / denominator * 100)
                .setScale(2, RoundingMode.HALF_UP);
        return rate.doubleValue();
    }

    private double formatAmount(double amount) {
        // 格式化数量，保留两位小数
        return BigDecimal.valueOf(amount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private ReportVo calculateReport(EmailTask emailTask, List<Email> emailList) {
        ReportVo reportVo = new ReportVo();
        long emailTotal = emailList.size();
        long sendNum = emailList.stream().filter(email -> email.getEmailStatus() == 1).count();
        long openNum = emailList.stream().filter(email -> email.getEmailStatus() == 2).count();
        
        // 设置投递率
        ReportVo.Delivery delivery = new ReportVo.Delivery();
        delivery.setRate(calculateRate(sendNum, emailTotal));
        delivery.setDeliveryAmount(formatAmount(sendNum));
        delivery.setTotal(emailTotal);
        reportVo.setDelivery(delivery);
        
        // 设置打开率
        ReportVo.Open open = new ReportVo.Open();
        open.setRate(calculateRate(openNum, sendNum));
        open.setOpenAmount(formatAmount(openNum));
        open.setTotal(sendNum);
        reportVo.setOpen(open);
        
        // 设置退信率
        ReportVo.Bounce bounce = new ReportVo.Bounce();
        bounce.setRate(calculateRate(emailTask.getBounceAmount(), emailTotal));
        bounce.setBounceAmount(formatAmount(emailTask.getBounceAmount()));
        bounce.setTotal(emailTotal);
        reportVo.setBounce(bounce);
        
        // 设置退订率
        ReportVo.Unsubscribe unsubscribe = new ReportVo.Unsubscribe();
        unsubscribe.setRate(calculateRate(emailTask.getUnsubscribeAmount(), emailTotal));
        unsubscribe.setUnsubscribeAmount(formatAmount(emailTask.getUnsubscribeAmount()));
        unsubscribe.setTotal(emailTotal);
        reportVo.setUnsubscribe(unsubscribe);
        
        return reportVo;
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

