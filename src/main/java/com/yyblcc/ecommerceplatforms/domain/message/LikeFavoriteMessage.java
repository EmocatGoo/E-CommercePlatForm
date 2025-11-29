package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LikeFavoriteMessage implements Serializable {
    private Long productId;
    private Long userId;
    private Integer action;
    private String type;
    private String requestId;
}
