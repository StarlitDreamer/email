package com.java.email.controller.receiver;

import com.java.email.common.Response.Result;
import com.java.email.model.entity.receiver.SupplierDocument;
import com.java.email.model.dto.request.SupplierFilterRequest;
import com.java.email.service.receiver.SupplierService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/supplierManage")
public class SupplierController {
    
    @Autowired
    private SupplierService supplierService;

    @PostMapping("/importSupplier")
    public Result importSupplier(@RequestParam("file") MultipartFile file) {
        return supplierService.importSupplier(file);
    }
        
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
