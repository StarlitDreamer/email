package com.java.email.service.impl.dictionary;

import com.java.email.esdao.repository.dictionary.EmailTypeRepository;
import com.java.email.model.entity.dictionary.EmailTypeDocument;
import com.java.email.service.dictionary.EmailTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailTypeServiceImpl implements EmailTypeService {
    @Autowired
    private EmailTypeRepository emailTypeRepository;

    // 测试用
    @Override
    public EmailTypeDocument saveEmailType(EmailTypeDocument emailType) {
        EmailTypeDocument savedEmailType = emailTypeRepository.save(emailType);
        return savedEmailType;
    }
}
