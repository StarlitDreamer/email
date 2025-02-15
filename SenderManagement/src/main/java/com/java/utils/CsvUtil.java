package com.java.utils;

import com.java.model.dto.CsvUserDto;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvUtil {

public static List<CsvUserDto> parseCsvFile(MultipartFile file) throws IOException {
    if (file.isEmpty()) {
        throw new IOException("文件为空");
    }

    if (!file.getOriginalFilename().endsWith(".csv")) {
        throw new IOException("文件格式不正确，请上传CSV文件");
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
        // 跳过CSV头行
        String headerLine = reader.readLine();
        String line;
        List<CsvUserDto> csvUserDtoList = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String[] data = line.split(",");
            CsvUserDto csvUserDto = new CsvUserDto();

            // 设置基本信息
            csvUserDto.setUserRole(data[0]);
            csvUserDto.setUserName(data[1]);
            csvUserDto.setUserAccount(data[2]);
            csvUserDto.setUserPassword(data[3]);
            csvUserDto.setUserEmail(data[4]);
            csvUserDto.setUserEmailCode(data[5]);
            csvUserDtoList.add(csvUserDto);
        }
        return csvUserDtoList;
    }
}
}
