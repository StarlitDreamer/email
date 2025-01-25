package com.java.email.controller.receiver;

import com.java.email.common.Response.Result;
import com.java.email.model.entity.receiver.CustomerDocument;
import com.java.email.service.receiver.CustomerService;
import com.java.email.model.dto.request.CustomerFilterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/customerManage")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/createCustomer")
    public Result createCustomer(@RequestBody CustomerDocument customerDocument) {
        return customerService.createCustomer(customerDocument);
    }

    @PostMapping("/updateCustomer")
    public Result updateCustomer(@RequestBody CustomerDocument customerDocument) {
        return customerService.updateCustomer(customerDocument);
    }

    @PostMapping("/deleteCustomer")
    public Result deleteCustomer(@RequestBody CustomerDocument customerDocument) {
        return customerService.deleteCustomer(customerDocument);
    }

    @PostMapping("/filterCustomer")
    public Result filterCustomer(@RequestBody CustomerFilterRequest request) {
        return customerService.filterCustomer(request);
    }

    @PostMapping("/assignCustomer")
    public Result assignCustomer(@RequestBody CustomerDocument customerDocument) {
        return customerService.assignCustomer(customerDocument);
    }

    @PostMapping("/assignCustomerDetails")
    public Result assignCustomerDetails(@RequestBody Map<String, Object> params) {
        return customerService.assignCustomerDetails(params);
    }

    @PostMapping("/allAssignCustomer")
    public Result allAssignCustomer(@RequestBody Map<String, Object> params) {
        return customerService.allAssignCustomer(params);
    }
}
