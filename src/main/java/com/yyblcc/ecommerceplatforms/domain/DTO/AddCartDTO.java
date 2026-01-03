package com.yyblcc.ecommerceplatforms.domain.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Min(value = 1, message = "数量最小为1")
    private Integer quantity;

    // 是否默认选中，默认 true
    private Boolean checked;
}
