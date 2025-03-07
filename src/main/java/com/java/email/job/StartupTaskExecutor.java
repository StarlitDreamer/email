package com.java.email.job;

import com.java.email.model.entity.Email;
import com.java.email.model.entity.EmailTask;
import com.java.email.repository.EmailRepository;
import com.java.email.repository.EmailTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupTaskExecutor {

    @Autowired
    private EmailTaskRepository emailTaskRepository;

    @Autowired
    private EmailRepository emailRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        long currentTimeMillis = System.currentTimeMillis();
        long currentTime=currentTimeMillis/1000;

        String defaultTaskId = "birth"; // 你可以换成数据库中的 ID
        String defaultSubject = "默认生日邮件";

        EmailTask emailTask = new EmailTask();
        emailTask.setEmailTaskId(defaultTaskId);
        emailTask.setSubject(defaultSubject);

        emailTaskRepository.save(emailTask);

        Email email = new Email();
        email.setEmailTaskId(defaultTaskId); // Set email_task_id
        email.setCreatedAt(currentTime);  // Set created_at
        email.setUpdateAt(currentTime);   // Set update_at
        email.setEmailStatus(2);          // Set email_status to 1 (开始状态)

        emailRepository.save(email);
    }
}