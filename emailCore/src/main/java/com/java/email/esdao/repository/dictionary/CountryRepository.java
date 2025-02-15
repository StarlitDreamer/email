package com.java.email.esdao.repository.dictionary;

import com.java.email.model.entity.dictionary.CountryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends ElasticsearchRepository<CountryDocument, String> {
    List<CountryDocument> findByCountryNameLike(String countryName);

    CountryDocument findByCountryName(String countryName);

    Optional<CountryDocument> findByCountryId(String countryId);

} 