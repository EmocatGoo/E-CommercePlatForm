package com.yyblcc.ecommerceplatforms.domain.VO;

import com.yyblcc.ecommerceplatforms.domain.document.CraftsmanDocument;
import com.yyblcc.ecommerceplatforms.domain.document.ProductDocument;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GlobalSearchResultVO {

    private List<CraftsmanDocument> craftsmen;
    private List<ProductDocument> products;
}
