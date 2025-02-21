package com.java.email.service;

import com.java.email.entity.EmailDetail;
import com.java.email.repository.EmailDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailDetailService {

    @Autowired
    private EmailDetailRepository emailDetailRepository;

    // 获取所有状态码为 500 的邮件的 emailTaskId
    public List<String> getEmailTaskIdsForErrorCode500() {
        List<EmailDetail> emailDetails = emailDetailRepository.findByErrorCode(500);
        return emailDetails.stream()
                .map(EmailDetail::getEmailTaskId)
                .collect(Collectors.toList());
    }
}
