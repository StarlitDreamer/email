package com.java.email.service;

import java.io.IOException;

public interface EmailTypeService {
    String findByEmailTypeName(String emailTypeId) throws IOException;
}
