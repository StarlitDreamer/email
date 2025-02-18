package com.java.email.esdao.repository.dictionary;

import com.java.email.model.entity.dictionary.CountryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends ElasticsearchRepository<CountryDocument, String> {

} 