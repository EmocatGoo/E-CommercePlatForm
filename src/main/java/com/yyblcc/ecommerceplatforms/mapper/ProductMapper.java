package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductStatisticVO;
import com.yyblcc.ecommerceplatforms.domain.po.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {


    @Select("SELECT product_name as productName,sale_count as salesCount FROM tb_product ORDER BY sale_count DESC LIMIT 10")
    List<ProductStatisticVO> listTop10Products();
}
