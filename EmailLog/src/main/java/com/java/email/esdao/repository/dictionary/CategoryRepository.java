package com.java.email.esdao.repository.dictionary;

import com.java.email.model.entity.dictionary.CategoryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CategoryRepository extends ElasticsearchRepository<CategoryDocument, String> {
    
}
