package com.java.email.repository;

import com.java.email.esdao.file.ImgDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImgRepository extends ElasticsearchRepository<ImgDocument, String> {
    // 根据创建者ID查找图片
    List<ImgDocument> findByCreatorId(String creatorId);
    
    // 根据图片名称模糊查询
    List<ImgDocument> findByImgNameLike(String imgName);
    
    // 根据状态查询
    List<ImgDocument> findByStatus(Integer status);
} 