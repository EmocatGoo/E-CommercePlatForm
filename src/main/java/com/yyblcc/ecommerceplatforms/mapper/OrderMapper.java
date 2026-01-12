package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.DTO.UserPageGroupDTO;
import com.yyblcc.ecommerceplatforms.domain.po.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    List<UserPageGroupDTO> selectOrderGroupPage(
            @Param("userId") Long userId,
            @Param("orderStatus") Integer orderStatus,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    Long countOrderGroup(
            @Param("userId") Long userId,
            @Param("orderStatus") Integer orderStatus,
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Order> selectOrderByOrderGroupSnAndPayStatus(
            @Param("orderGroupSn") String orderGroupSn,
            @Param("payStatus") Integer payStatus
    );

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM tb_orders WHERE craftsman_id = #{craftsmanId}")
    BigDecimal calculateOrderSaleAmount(@Param("craftsmanId") Long craftsmanId);


}
