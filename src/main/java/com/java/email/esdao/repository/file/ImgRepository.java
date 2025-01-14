package com.java.email.esdao.repository.file;

import com.java.email.model.entity.file.ImgDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImgRepository extends ElasticsearchRepository<ImgDocument, String> {
    // 根据创建者ID查找图片
    List<ImgDocument> findByCreatorId(String creatorId);
    
    // 根据图片名称模糊查询
    List<ImgDocument> findByImgNameLike(String imgName);
    
    // 根据状态查询
    List<ImgDocument> findByStatus(Integer status);

    <S extends ImgDocument> Iterable<S> saveAll(Iterable<S> entities);

    Optional<ImgDocument> findById(String imgId);

    ImgDocument save(ImgDocument imgDoc);

    void deleteById(String imgId);
}