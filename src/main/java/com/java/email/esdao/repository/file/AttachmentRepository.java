package com.java.email.esdao.repository.file;

import com.java.email.model.entity.file.AttachmentDocument;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface AttachmentRepository extends ElasticsearchRepository<AttachmentDocument, String> {
    AttachmentDocument save(AttachmentDocument entity);

    <S extends AttachmentDocument> Iterable<S> saveAll(Iterable<S> entities);

    Optional<AttachmentDocument> findById(String attachmentId);

    void deleteById(String attachmentId);

    @Query("{\"bool\": ?0}")
    Page<AttachmentDocument> findByCustomQuery(String query, Pageable pageable);

    @Query("{\"bool\": ?0}")
    Page<AttachmentDocument> search(NativeSearchQuery query, Pageable pageable);
}