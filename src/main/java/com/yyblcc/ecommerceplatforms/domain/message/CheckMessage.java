package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckMessage implements Serializable {
    private Long userId;
    private Long productId;
    private Boolean checked;
    private Integer quantity;
}
