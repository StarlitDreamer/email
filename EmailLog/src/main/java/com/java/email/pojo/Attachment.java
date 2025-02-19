package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {
    @JsonProperty("attachment_id")
    private String attachmentId;

    @JsonProperty("attachment_name")
    private String attachmentName;

    @JsonProperty("attachment_size")
    private long attachmentSize;

    @JsonProperty("attachment_url")
    private String attachmentUrl;

    @JsonProperty("belong_user_id")
    private String[] belongUserId; // ES 不是数组

    @JsonProperty("created_at")
    private long createdAt;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("status")
    private int status; // ES 是 integer

    @JsonProperty("updated_at")
    private long updatedAt;
}

