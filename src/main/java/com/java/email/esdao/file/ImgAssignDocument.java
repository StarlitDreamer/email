package com.java.email.esdao;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "img_assign")
public class ImgAssignDocument {
    
    @Id
    private String imgId;

    @Field(type = FieldType.Nested)
    private List<AssignProcess> assignProcess;

    @Data
    public static class AssignProcess {
        @Field(type = FieldType.Keyword)
        private String assignorId;

        @Field(type = FieldType.Text, analyzer = "ik_max_word")
        private String assignorName;

        @Field(type = FieldType.Nested)
        private List<Assignee> assignee;

        @Field(type = FieldType.Date, index = false)
        private String assignDate;
    }

    @Data
    public static class Assignee {
        @Field(type = FieldType.Keyword)
        private String assigneeId;

        @Field(type = FieldType.Text, analyzer = "ik_max_word")
        private String assigneeName;
    }
} 