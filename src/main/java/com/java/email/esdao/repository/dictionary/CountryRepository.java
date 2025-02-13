package com.java.email.esdao.repository.dictionary;

import com.java.email.model.entity.dictionary.CommodityDocument;
import com.java.email.model.entity.dictionary.CountryDocument;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends ElasticsearchRepository<CountryDocument, String> {
    List<CountryDocument> findByCountryNameLike(String countryName);

    CountryDocument findByCountryName(String countryName);

} 