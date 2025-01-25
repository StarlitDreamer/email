package com.java.email.controller.receiver;

import com.java.email.common.Response.Result;
import com.java.email.model.entity.receiver.SupplierDocument;
import com.java.email.model.dto.request.SupplierFilterRequest;
import com.java.email.service.receiver.SupplierService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/supplierManage")
public class SupplierController {
    
    @Autowired
    private SupplierService supplierService;

    @PostMapping("/createSupplier")
    public Result createSupplier(@RequestBody SupplierDocument supplierDocument) {
        return supplierService.createSupplier(supplierDocument);
    }

    @PostMapping("/updateSupplier")
    public Result updateSupplier(@RequestBody SupplierDocument supplierDocument) {
        return supplierService.updateSupplier(supplierDocument);
    }

    @PostMapping("/deleteSupplier")
    public Result deleteSupplier(@RequestBody SupplierDocument supplierDocument) {
        return supplierService.deleteSupplier(supplierDocument);
    }

    @PostMapping("/filterSupplier")
    public Result filterSupplier(@RequestBody SupplierFilterRequest request) {
        return supplierService.filterSupplier(request);
    }

    @PostMapping("/assignSupplier")
    public Result assignSupplier(@RequestBody SupplierDocument supplierDocument) {
        return supplierService.assignSupplier(supplierDocument);
    }

    @PostMapping("/assignSupplierDetails")
    public Result assignSupplierDetails(@RequestBody Map<String, Object> params) {
        return supplierService.assignSupplierDetails(params);
    }

    @PostMapping("/allAssignSupplier")
    public Result allAssignSupplier(@RequestBody Map<String, Object> params) {
        return supplierService.allAssignSupplier(params);
    }
}
