package com.yyblcc.ecommerceplatforms.domain.message;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CraftsmanMessage {
    private Long id;
    private Integer actionCode;
}
