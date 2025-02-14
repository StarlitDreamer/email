package com.java.email.esdao.repository.dictionary;

import com.java.email.model.entity.dictionary.CommodityDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CommodityRepository extends ElasticsearchRepository<CommodityDocument, String> {

    List<CommodityDocument> findByCommodityNameLike(String commodityName);

    CommodityDocument findByCommodityName(String trim);
}