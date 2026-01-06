package com.yyblcc.ecommerceplatforms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyblcc.ecommerceplatforms.domain.po.ChatSession;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    
    /**
     * 根据用户ID获取会话列表
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getSessionsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据匠人ID获取会话列表
     * @param craftsmanId 匠人ID
     * @return 会话列表
     */
    List<ChatSession> getSessionsByCraftsmanId(@Param("craftsmanId") Long craftsmanId);
    
    /**
     * 根据用户ID和匠人ID获取会话
     * @param userId 用户ID
     * @param craftsmanId 匠人ID
     * @return 会话信息
     */
    ChatSession getSessionByUserIdAndCraftsmanId(@Param("userId") Long userId, @Param("craftsmanId") Long craftsmanId);

    long countUnreadByUserId(Long userId);

    long countUnreadByCraftsmanId(Long craftsmanId);
}