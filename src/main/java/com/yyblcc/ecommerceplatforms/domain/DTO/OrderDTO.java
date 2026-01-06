package com.yyblcc.ecommerceplatforms.domain.DTO;

import com.yyblcc.ecommerceplatforms.domain.po.Product;
import lombok.Data;

import java.util.List;
@Data
public class OrderDTO {
    private List<OrderItemDTO> orderItemList;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String remark;
    private Integer fromCart;
}
