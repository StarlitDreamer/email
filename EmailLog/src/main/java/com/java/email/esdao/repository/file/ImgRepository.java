package com.java.email.esdao.repository.file;

import com.java.email.model.entity.file.ImgDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ImgRepository extends ElasticsearchRepository<ImgDocument, String> {

}