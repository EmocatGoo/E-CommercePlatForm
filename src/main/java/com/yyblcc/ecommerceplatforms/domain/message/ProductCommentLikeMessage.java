package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.Data;

@Data
public class ProductCommentLikeMessage {
    private Long productId;
    private Long commentId;
    private Long userId;
    private boolean liked;
}
