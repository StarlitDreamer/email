package com.java.email.service;

import com.java.email.pojo.UndeliveredEmail;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UndeliveredEmailService {
    void saveEmail(UndeliveredEmail emailTask) throws IOException;

    List<UndeliveredEmail> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException;
}
