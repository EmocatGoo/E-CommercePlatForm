package com.yyblcc.ecommerceplatforms.service.impl;

import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Craftsman;
import com.yyblcc.ecommerceplatforms.repository.CraftsmanRepository;
import com.yyblcc.ecommerceplatforms.service.CraftsmanEsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CraftsmanEsServiceImplement implements CraftsmanEsService {

    private final CraftsmanRepository craftsmanRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void saveOrUpdate(Craftsman craftsman) {
        // 1. 非可搜索状态，直接删除索引
        if (!isSearchable(craftsman)) {
            craftsmanRepository.deleteById(craftsman.getId());
            return;
        }

        // 2. Entity → Document
        CraftsmanDocument doc = new CraftsmanDocument();
        BeanUtils.copyProperties(craftsman, doc);

        // 3. 写入 ES
        craftsmanRepository.save(doc);

    }
    private boolean isSearchable(Craftsman craftsman) {
        return craftsman.getStatus().equals(StatusConstant.ENABLE)
                && craftsman.getReviewStatus().equals(StatusConstant.ENABLE)
                && craftsman.getIsDeleted() == 0;
    }

    @Override
    public void deleteById(Long craftsmanId) {
        craftsmanRepository.deleteById(craftsmanId);
    }

    @Override
    public List<CraftsmanDocument> search(String keyword, int page, int size) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> {
                            if (keyword.length() == 1){
                                log.info("keyword: {}", keyword);
                                b.should(s -> s.match(m -> m.field("name").query(keyword)));
                            }
                            b.should(s -> s.multiMatch(mm ->
                               mm.query(keyword)
                                       .fields(
                                               "name^3",
                                               "technique^2",
                                               "introduction",
                                               "workshopName"
                                       )
                            ));
                            b.minimumShouldMatch("1");
                            b.filter(f -> f.term(t -> t.field("status").value(StatusConstant.ENABLE)));
                            b.filter(f -> f.term(t -> t.field("reviewStatus").value(StatusConstant.ENABLE)));
                            return b;
                        }))
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<CraftsmanDocument> hits =
                elasticsearchOperations.search(query, CraftsmanDocument.class);

        return hits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
