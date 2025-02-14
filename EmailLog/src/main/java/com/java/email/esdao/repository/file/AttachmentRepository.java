package com.java.email.esdao.repository.file;

import com.java.email.model.entity.file.AttachmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AttachmentRepository extends ElasticsearchRepository<AttachmentDocument, String> {


}