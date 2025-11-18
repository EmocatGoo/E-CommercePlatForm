package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.UserCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCollectMapper extends BaseMapper<UserCollect> {
    @Update("""
        INSERT INTO tb_user_collection (user_id, workshop_id, status,update_time)
        VALUES (#{userId}, #{workShopId}, 1, NOW())
        ON DUPLICATE KEY UPDATE
            status = 1 - status,
            update_time = NOW()
        """)
    int Collect(@Param("userId") Long userId, @Param("workShopId") Long workShopId);

    int getFinalStatus(@Param("userId") Long userId, @Param("workShopId") Long workShopId);
}
