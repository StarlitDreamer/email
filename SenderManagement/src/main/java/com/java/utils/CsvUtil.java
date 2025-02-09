package com.java.utils;

import com.java.model.dto.CsvUserDto;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;

public class CsvUtil {

public static List<CsvUserDto> parseCsvFile(MultipartFile file) throws IOException {
    try (InputStreamReader reader = new InputStreamReader(file.getInputStream())) {
        // 使用 OpenCSV 解析 CSV 文件并将其转换为 Java 对象
        CsvToBean<CsvUserDto> csvToBean = new CsvToBeanBuilder<CsvUserDto>(reader)
                .withType(CsvUserDto.class)
                .build();

        // 返回解析后的对象列表
        return csvToBean.parse();
    }
}
}
