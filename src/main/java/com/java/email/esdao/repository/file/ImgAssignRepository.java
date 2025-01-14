package com.java.email.esdao.repository.file;

import com.java.email.model.entity.file.ImgAssignDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImgAssignRepository extends ElasticsearchRepository<ImgAssignDocument, String> {
    Optional<ImgAssignDocument> findById(String imgId);

    void save(ImgAssignDocument existingAssignDoc);
}