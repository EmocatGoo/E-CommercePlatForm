package com.yyblcc.ecommerceplatforms.domain.DTO;

import lombok.Data;

@Data
public class ProductCommentLikeDTO {
    private Long productId;
    private Long commentId;
    private Long userId;
}
