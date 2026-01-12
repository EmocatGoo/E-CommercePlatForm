package com.yyblcc.ecommerceplatforms.domain.DTO;

import com.yyblcc.ecommerceplatforms.domain.po.Product;
import lombok.Data;

import java.util.List;
@Data
public class OrderDTO {
    //订单项（商品）
    private List<OrderItemDTO> orderItemList;

    //收货人
    private String consignee;

    //收货号码
    private String phone;

    //收货地址（省）
    private String province;

    //收货地址（市）
    private String city;

    //收货地址（区）
    private String district;

    //详细地址
    private String detailAddress;

    //备注
    private String remark;

    //是否从购物车结算
    private Integer fromCart;
}
