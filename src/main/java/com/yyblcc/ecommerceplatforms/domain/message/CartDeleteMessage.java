package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartDeleteMessage {
    private Long userId;
    private List<Long> productIds;
}
