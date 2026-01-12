package com.yyblcc.ecommerceplatforms.repository;

import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface CraftsmanRepository extends ElasticsearchRepository<CraftsmanDocument, Long> {

}
