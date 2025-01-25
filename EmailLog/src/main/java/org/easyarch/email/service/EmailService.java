package org.easyarch.email.service;

import org.easyarch.email.pojo.Email;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EmailService {

     void saveEmail(Email emailTask) throws IOException;

     List<Email> findByDynamicQueryEmail(Map<String, String> params, int page, int size) throws IOException;

     Email findById(String id) throws IOException;

     List<Email> findAllEmail(String emaIlTaskId) throws IOException;

     List<Email> findAll() throws IOException;

     void deleteById(String id) throws IOException;
}
