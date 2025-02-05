package com.java.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignProcess {
    @Field(type = FieldType.Keyword,name="assignor_id")
    private String assignorId;  // 分配者ID

    @Field(type = FieldType.Text,name="assignor_name")
    private String assignorName; // 分配者名称

    @Field(type = FieldType.Keyword,name="assignee_id")
    private String assigneeId;  // 被分配者ID

    @Field(type = FieldType.Text,name="assignee_name")
    private String assigneeName; // 被分配者名称

    @Field(type = FieldType.Keyword,name="assign_date")
    private String assignDate;  // 分配日期
}
