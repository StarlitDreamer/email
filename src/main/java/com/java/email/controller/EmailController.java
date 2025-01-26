package com.java.email.controller;

import com.java.email.Dto.RequestData;
import com.java.email.Dto.ResponseData;
import com.java.email.common.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EmailController {

    @PostMapping("/send-conditions")
    public Response getSendConditions(@RequestBody RequestData requestData) {
        // 构建响应数据
        Response response = new Response();
        response.setCode(200);
        response.setMsg("成功");
        response.setData(new ResponseData(
                requestData.getEmailType(),
                requestData.getArea(),
                requestData.getCountry(),
                requestData.getReceiverInfo()
        ));

        return response;
    }
}