package com.java.model.domain;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "user_assign")
@AllArgsConstructor
@NoArgsConstructor
public class UserAssign {
    @Id
    @Field(type = FieldType.Keyword,name="user_id")
    private String userId;
    @Field(type = FieldType.Nested,name="assign_process")
    private List<AssignProcess> assignProcess;
}
