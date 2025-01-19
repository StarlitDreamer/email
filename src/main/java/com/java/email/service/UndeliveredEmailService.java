package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.entity.ResendStrategy;
import com.java.email.entity.UndeliveredEmail;
import com.java.email.repository.UndeliveredEmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UndeliveredEmailService {

    @Autowired
    private UndeliveredEmailRepository undeliveredEmailRepository;

    /**
     * 根据条件筛选未送达邮件
     *
     * @param emailTaskId  邮件任务 ID
     * @param receiverIds  收件人 ID 列表
     * @param senderIds    发件人 ID 列表
     * @param resendStatus 重发状态
     * @param errorCode    错误代码
     * @param page         页码
     * @param size         每页大小
     * @return 符合条件的未送达邮件分页结果
     */
    public Result<Page<UndeliveredEmail>> findUndeliveredEmailsByCriteria(
            String emailTaskId, List<String> receiverIds, List<String> senderIds,
            Integer resendStatus, Long errorCode, int page, int size) {
        try {
            // 创建分页对象
            Pageable pageable = PageRequest.of(page, size);

            // 动态构建查询条件
            if (emailTaskId != null) {
                return Result.success(undeliveredEmailRepository.findByEmailTaskId(emailTaskId, pageable));
            } else if (receiverIds != null && !receiverIds.isEmpty()) {
                return Result.success(undeliveredEmailRepository.findByReceiverIdsIn(receiverIds, pageable));
            } else if (senderIds != null && !senderIds.isEmpty()) {
                return Result.success(undeliveredEmailRepository.findBySenderIdsIn(senderIds, pageable));
            } else if (resendStatus != null) {
                return Result.success(undeliveredEmailRepository.findByResendStatus(resendStatus, pageable));
            } else if (errorCode != null) {
                return Result.success(undeliveredEmailRepository.findByErrorCode(errorCode, pageable));
            } else {
                // 如果没有条件，返回所有未送达邮件（分页）
                return Result.success(undeliveredEmailRepository.findAll(pageable));
            }
        } catch (Exception e) {
            // 返回错误结果
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询未送达邮件数据
     *
     * @param pageNum  当前页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 当前页的数据列表
     */
    public List<UndeliveredEmail> getUndeliveredEmails(int pageNum, int pageSize) {
        // 执行分页查询
        Page<UndeliveredEmail> page = undeliveredEmailRepository.findAll(PageRequest.of(pageNum, pageSize));
        // 获取当前页的数据列表
        return page.getContent();
    }

    /**
     * 根据邮件 ID 更新重发策略
     *
     * @param emailId     邮件 ID
     * @param resendGap   重发间隔（分钟）
     * @param resendTimes 重发次数
     * @return 更新后的未送达邮件
     * @throws IllegalArgumentException 如果邮件不存在或参数无效
     */
    public UndeliveredEmail updateResendStrategy(String emailId, long resendGap, long resendTimes) {
        // 查找未送达邮件
        UndeliveredEmail undeliveredEmail = undeliveredEmailRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("邮件不存在"));

        // 创建或更新重发策略
        ResendStrategy resendStrategy = undeliveredEmail.getResendStrategy();
        if (resendStrategy == null) {
            resendStrategy = new ResendStrategy();
        }
        resendStrategy.setResendGap(resendGap);
        resendStrategy.setResendTimes(resendTimes);

        // 更新邮件中的重发策略
        undeliveredEmail.setResendStrategy(resendStrategy);

        // 保存更新后的邮件
        return undeliveredEmailRepository.save(undeliveredEmail);
    }

    /**
     * 根据邮件 ID 更新重发状态
     *
     * @param emailId      邮件 ID
     * @param resendStatus 新的重发状态
     * @return 更新后的未送达邮件
     * @throws IllegalArgumentException 如果邮件不存在或状态无效
     */
    public UndeliveredEmail updateResendStatus(String emailId, int resendStatus) {
        // 查找未送达邮件
        UndeliveredEmail undeliveredEmail = undeliveredEmailRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("邮件不存在"));

        // 更新重发状态
        undeliveredEmail.setResendStatus(resendStatus);

        // 保存更新后的邮件
        return undeliveredEmailRepository.save(undeliveredEmail);
    }
}