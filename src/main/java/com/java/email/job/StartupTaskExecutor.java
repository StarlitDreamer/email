package com.java.email.job;

import com.java.email.model.request.UpdateBirthEmailTaskRequest;
import com.java.email.service.EmailTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupTaskExecutor {

    @Autowired
    private EmailTaskService emailTaskService;  // 假设你的 `updateBirthEmailTask` 方法在 EmailTaskService

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        System.out.println("Spring Boot 启动完成，自动执行 updateBirthEmailTask()...");

        // 这里填入默认的 taskId 和 request
        String defaultTaskId = "birth"; // 你可以换成数据库中的 ID
        UpdateBirthEmailTaskRequest request = new UpdateBirthEmailTaskRequest();
//        request.setTemplateId("default_template_id");
        request.setSubject("默认生日邮件");
        request.setEmailStatus("2"); // 1 表示启动状态
//        request.setAttachment(null); // 附件可为空

        // 执行方法
        String result = emailTaskService.updateBirthEmailTask(defaultTaskId, request);
//        System.out.println("updateBirthEmailTask 执行结果：" + result);
    }
}

