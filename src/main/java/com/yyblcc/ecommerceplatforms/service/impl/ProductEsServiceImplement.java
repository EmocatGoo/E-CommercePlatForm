package com.yyblcc.ecommerceplatforms.service.impl;

import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.domain.Enum.ProductStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.document.ProductDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Craftsman;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.repository.ProductRepository;
import com.yyblcc.ecommerceplatforms.service.ProductEsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductEsServiceImplement implements ProductEsService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository;

    @Override
    public void saveOrUpdate(Product product) {
        if (!isSearchable(product)){
            productRepository.deleteById(product.getId());
            return;
        }
        ProductDocument doc = new ProductDocument();
        BeanUtils.copyProperties(product, doc);
        productRepository.save(doc);
    }

    private boolean isSearchable(Product product) {
        return product.getStatus().equals(ProductStatusEnum.LISTED.getCode())
                && product.getReviewStatus().equals(ProductStatusEnum.APPROVE.getCode())
                && product.getIsDeleted() == 0;
    }

    @Override
    public void deleteById(Long productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public List<ProductDocument> search(String keyword, int page, int size) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            if (keyword.length() == 1) {
                                b.should(s -> s.match(m ->
                                        m.field("productName").query(keyword)));
                            }

                            b.should(s -> s.multiMatch(mm ->
                                mm.query(keyword)
                                   .fields(
                                           "productName^3",
                                           "description",
                                           "culturalBackground")));
                                   b.minimumShouldMatch(String.valueOf(1));
                                   b.filter(f -> f.term(t -> t.field("status").value(ProductStatusEnum.LISTED.getCode())));
                                   b.filter(f -> f.term(t -> t.field("reviewStatus").value(ProductStatusEnum.APPROVE.getCode())));
                                   return b;
                        }))
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        return hits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
