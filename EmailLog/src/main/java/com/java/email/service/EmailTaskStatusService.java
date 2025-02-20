package com.java.email.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface EmailTaskStatusService {
    Set<String> findEmailTaskIds(long emailStatus) throws IOException;
}
