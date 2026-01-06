package com.yyblcc.ecommerceplatforms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyblcc.ecommerceplatforms.domain.DTO.ProductCommentAddDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ProductCommentLikeDTO;
import com.yyblcc.ecommerceplatforms.domain.po.ProductComment;
import com.yyblcc.ecommerceplatforms.domain.po.ProductCommentLike;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CommentQuery;

public interface ProductCommentService extends IService<ProductComment> {
    Result createComment(ProductCommentAddDTO dto);

    Result listComments(Long productId);

    Result likeComment(ProductCommentLikeDTO productCommentLikeDTO);

    Result deleteComment(Long commentId);

    Result pageComment(CommentQuery commentQuery);
}
