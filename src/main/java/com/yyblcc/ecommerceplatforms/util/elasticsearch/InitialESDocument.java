package com.yyblcc.ecommerceplatforms.util.elasticsearch;

import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import com.yyblcc.ecommerceplatforms.domain.document.ProductDocument;
import com.yyblcc.ecommerceplatforms.domain.po.Craftsman;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import com.yyblcc.ecommerceplatforms.mapper.CraftsmanMapper;
import com.yyblcc.ecommerceplatforms.mapper.ProductMapper;
import com.yyblcc.ecommerceplatforms.repository.CraftsmanRepository;
import com.yyblcc.ecommerceplatforms.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
public class InitialESDocument implements CommandLineRunner {

    private final CraftsmanMapper craftsmanMapper;
    private final CraftsmanRepository craftsmanRepository;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    @Override
    public void run(String... args) throws Exception {
        List<Craftsman> craftsmen = craftsmanMapper.selectList(null);
        List<CraftsmanDocument> documents = craftsmen.stream()
                .map(c-> new CraftsmanDocument()
                        .setId(c.getId())
                        .setName(c.getName())
                        .setTechnique(c.getTechnique())
                        .setIntroduction(c.getIntroduction())
                        .setWorkshopName(c.getWorkshopName())
                        .setStatus(c.getStatus())
                        .setReviewStatus(c.getReviewStatus())
                        .setCreateTime(c.getCreateTime() == null ? null:c.getCreateTime().toLocalDate()))
                .toList();
        craftsmanRepository.saveAll(documents);

        List<Product> products = productMapper.selectList(null);
        List<ProductDocument> productDocuments = products.stream()
                .map(p-> new ProductDocument()
                        .setId(p.getId())
                        .setProductName(p.getProductName())
                        .setPrice(p.getPrice())
                        .setDescription(p.getDescription())
                        .setCraftsmanId(p.getCraftsmanId())
                        .setCulturalBackground(p.getCulturalBackground())
                        .setStatus(p.getStatus())
                        .setReviewStatus(p.getReviewStatus())
                        .setCreateTime(p.getCreateTime() == null ? null:p.getCreateTime().toLocalDate())
                ).toList();
        productRepository.saveAll(productDocuments);
    }
}
