package com.java.email.esdao.repository.dictionary;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.java.email.model.entity.dictionary.CategoryDocument;

public interface CategoryRepository extends ElasticsearchRepository<CategoryDocument, String> {
    
}
