package com.java.email.esdao.repository.dictionary;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.java.email.model.entity.dictionary.CommodityDocument;

import java.util.List;

public interface CommodityRepository extends ElasticsearchRepository<CommodityDocument, String> {

    List<CommodityDocument> findByCommodityNameLike(String commodityName);
}