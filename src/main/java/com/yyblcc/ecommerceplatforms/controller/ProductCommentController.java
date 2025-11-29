package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.DTO.ProductCommentAddDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ProductCommentLikeDTO;
import com.yyblcc.ecommerceplatforms.domain.po.ProductComment;
import com.yyblcc.ecommerceplatforms.domain.po.ProductCommentLike;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CommentQuery;
import com.yyblcc.ecommerceplatforms.service.ProductCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
public class ProductCommentController {
    private final ProductCommentService productCommentService;

    @PostMapping
    public Result createComment(@RequestBody ProductCommentAddDTO dto){
        log.info("用户评论,{}",dto);
        return productCommentService.createComment(dto);
    }

    @GetMapping("/list")
    public Result listComments(@RequestParam Long productId){
        log.info("搜寻id为:{}下用户的子评论",productId);
        return productCommentService.listComments(productId);
    }

    @PostMapping("/like")
    public Result likeComment(@RequestBody ProductCommentLikeDTO productCommentLikeDTO){
        log.info("用户发起点赞/取消点赞请求:{}",productCommentLikeDTO);
        return productCommentService.likeComment(productCommentLikeDTO);
    }

    @DeleteMapping("/{commentId}")
    public Result deleteComment(@PathVariable Long commentId){
        log.info("删除评论:{}",commentId);
        return productCommentService.deleteComment(commentId);
    }

    @GetMapping("/page")
    public Result pageComments(CommentQuery commentQuery){
        log.info("管理员查询评论信息");
        return productCommentService.pageComment(commentQuery);
    }

}
