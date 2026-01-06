package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDeleteMessage {
    private Long userId;
    private List<Long> productIds;
}
