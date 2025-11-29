package com.yyblcc.ecommerceplatforms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.annotation.UpdateBloomFilter;
import com.yyblcc.ecommerceplatforms.domain.DTO.ProductCommentAddDTO;
import com.yyblcc.ecommerceplatforms.domain.DTO.ProductCommentLikeDTO;
import com.yyblcc.ecommerceplatforms.domain.Enum.RoleEnum;
import com.yyblcc.ecommerceplatforms.domain.message.ProductCommentLikeMessage;
import com.yyblcc.ecommerceplatforms.domain.po.PageBean;
import com.yyblcc.ecommerceplatforms.domain.po.ProductComment;
import com.yyblcc.ecommerceplatforms.domain.po.ProductCommentLike;
import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.domain.query.CommentQuery;
import com.yyblcc.ecommerceplatforms.mapper.ProductCommentLikeMapper;
import com.yyblcc.ecommerceplatforms.mapper.ProductCommentMapper;
import com.yyblcc.ecommerceplatforms.service.ProductCommentService;
import com.yyblcc.ecommerceplatforms.util.commentPath.PathUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCommentServiceImplement extends ServiceImpl<ProductCommentMapper, ProductComment> implements ProductCommentService {
    private final ProductCommentMapper productCommentMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final HttpServletRequest request;
    private final ProductCommentLikeMapper productCommentLikeMapper;
    private final RocketMQTemplate rocketMQTemplate;

    private static final DefaultRedisScript<Long> LIKE_SCRIPT;

    static {
        LIKE_SCRIPT = new DefaultRedisScript<>();
        LIKE_SCRIPT.setLocation(new ClassPathResource("like.lua"));
        LIKE_SCRIPT.setResultType(Long.class);
    }

    @Override
    @UpdateBloomFilter
    public Result createComment(ProductCommentAddDTO dto) {
//        Long userId = (Long)request.getSession().getAttribute("USER_ID");
//        if(userId == null){
//           Long userId = AuthContext.getUserId();
//        }
        //TODO 测试环境
        Long userId = 4L;
        if (userId == null) {
            return Result.error("请先登录");
        }
        boolean isReply = dto.getReplyToUserId() != null;

        Long rootId = null;
        String parentPath = null;
        Long parentId = null;
        int depth = 1;

        if (isReply) {
            parentId = dto.getParentCommentId();
            ProductComment parent = getById(parentId);

            if (parent == null) {
                return Result.error("父评论不存在");
            }

            rootId = (parent.getRootId() == null || parent.getRootId() == 0)
                    ? parent.getId()
                    : parent.getRootId();

            parentPath = parent.getPath();
            depth = parent.getDepth() + 1;
        }

        String nextPathSegment = null;

        //A（根评论）
        //├── B（回复 A）
        //│   ├── C（回复 B）
        //│   └── D（回复 B）
        //└── E（回复 A）
        //A   → 0001
        //B   → 0001.0001
        //C   → 0001.0001.0001
        //D   → 0001.0001.0002
        //E   → 0001.0002
        //非回复评论,找当前路径上最大的path,如0001.0001.0002.0003  0003就是最大的路径,
        if (!isReply) {
            // 根评论找同一个 productId 下 path 最大的根
            String maxRootPath = productCommentMapper.selectMaxRootPath(dto.getProductId());
            nextPathSegment = PathUtils.genRootNextPath(maxRootPath);
            parentPath = nextPathSegment;
        } else {
            // 回复评论找同 parent 下的最大子 path
            String maxChildPath = productCommentMapper.selectMaxChildPath(parentPath + "%");
            nextPathSegment = PathUtils.genChildNextPath(parentPath, maxChildPath);
        }

        // 4. 保存评论
        ProductComment comment = ProductComment.builder()
                .productId(dto.getProductId())
                .userId(userId)
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .rootId(rootId)
                .path(nextPathSegment)
                .depth(depth)
                .likeCount(0)
                .replyCount(0)
                .createTime(LocalDateTime.now())
                .build();

        save(comment);

        // 5. 如果是回复，父评论 replyCount +1
        if (isReply) {
            update().setSql("reply_count = reply_count + 1").eq("id", parentId).update();
        }

        return Result.success("评论成功");
    }

    @Override
    //1.查出所有某商品的评论，按path递增排序
    //2.用Map分组
    //3.构造树结构
    public Result listComments(Long productId) {
//        Long userId = (Long)request.getSession().getAttribute("USER_ID");
//        if(userId == null){
//           Long userId = AuthContext.getUserId();
//        }
        //TODO 测试环境
        Long userId = 4L;
        //1.查询所有
        List<ProductComment> commentList = productCommentMapper.selectList(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getProductId, productId)
                .orderByAsc(ProductComment::getPath));
        // 0001 id=1
        // 0001.0002 id=2
        // 0001.0003 id=3

        //2.用Map分组
        Map<Long,ProductComment> commentMap = new HashMap<>();
        List<ProductComment> rootCommentList = new ArrayList<>();

        for (ProductComment comment : commentList) {
            commentMap.put(comment.getId(), comment);
        }
        //id  Comment
        //1   Comment1{id=1,product_id=1,rootId=null,userId=4,content=... path=0001}
        //2   Comment2{id=2,product_id=1,rootId=1,userId=4,content=... path=0001.0002}
        //3   Comment3{id=3,product_id=1,rootId=1,userId=4,content=... path=0001.0003}
        //4   Comment4{id=4,product_id=1,rootId=null,userId=4,content=... path=0002}

        //3.构建树结构
        for (ProductComment comment : commentList) {
            if (comment.getRootId() == null || comment.getRootId() == 0) {
                ProductCommentLike isLike = productCommentLikeMapper.selectOne(new LambdaQueryWrapper<ProductCommentLike>()
                        .eq(ProductCommentLike::getProductId, comment.getProductId())
                        .eq(ProductCommentLike::getUserId,userId)
                        .eq(ProductCommentLike::getCommentId, comment.getId())
                        .orderByAsc(ProductCommentLike::getCreateTime)
                        .last("FOR UPDATE"));
                comment.setLikedByCurrentUser(isLike != null);
                rootCommentList.add(comment);
                continue;
            }

            Long parentId = getParentIdFromPath(comment.getPath());
            ProductComment parent = commentMap.get(parentId);

            if (parent != null){
                if (parent.getChildren() == null){
                    parent.setChildren(new ArrayList<>());
                }
                ProductCommentLike isLike = productCommentLikeMapper.selectOne(new LambdaQueryWrapper<ProductCommentLike>()
                        .eq(ProductCommentLike::getProductId, comment.getProductId())
                        .eq(ProductCommentLike::getUserId,userId)
                        .eq(ProductCommentLike::getCommentId, comment.getId())
                        .orderByAsc(ProductCommentLike::getCreateTime)
                        .last("FOR UPDATE"));
                comment.setLikedByCurrentUser(isLike != null);
                parent.getChildren().add(comment);
            }
        }

        return Result.success(rootCommentList);
    }

    @Override
    public Result likeComment(ProductCommentLikeDTO productCommentLikeDTO) {
        String key = "comment:like:" + productCommentLikeDTO.getCommentId();
//        Long userId = (Long)request.getSession().getAttribute("USER_ID");
//        Long userId = AuthContext.getUserId();
        //TODO 测试环境
        Long userId = 4L;
        if (!userId.equals(productCommentLikeDTO.getUserId())) {
            return Result.error("请先登录");
        }
        Long result = stringRedisTemplate.execute(
                LIKE_SCRIPT,
                Collections.singletonList(key),
                userId.toString()
        );

        boolean liked = result == 0L;

        ProductCommentLikeMessage message = new ProductCommentLikeMessage();
        BeanUtils.copyProperties(productCommentLikeDTO, message);
        message.setLiked(liked);
        rocketMQTemplate.asyncSend("product-comment-like-topic", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("点赞消息发送成功");
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("点赞消息发送失败", throwable);
            }
        });
        return Result.success(liked ? "已点赞" : "取消点赞");
    }

    @Override
    public Result deleteComment(Long commentId) {
//        Long userId = (Long) request.getSession().getAttribute("USER_ID");
        Long userId = 4L;
        ProductComment comment = productCommentMapper.selectOne(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getUserId, userId)
                .eq(ProductComment::getId, commentId)
                .orderByDesc(ProductComment::getCreateTime)
                .last("FOR UPDATE"));
        if (comment == null) {
            return Result.error("未找到评论信息");
        }
        //删除子评论
        if(comment.getRootId() == null){
            productCommentMapper.delete(new LambdaQueryWrapper<ProductComment>()
                    .eq(ProductComment::getRootId, commentId));
        }
        productCommentMapper.deleteById(comment);
        return Result.success("已删除评论");
    }

    @Override
    public Result<PageBean<ProductComment>> pageComment(CommentQuery commentQuery) {
        RoleEnum role = (RoleEnum) StpUtil.getSession().get("ROLE");
        if (RoleEnum.USER.equals(role)) {
            return Result.error("权限不足");
        }
        Page<ProductComment> commentPage = productCommentMapper.selectPage(new Page<>(commentQuery.getPage(),commentQuery.getPageSize())
                ,new LambdaQueryWrapper<ProductComment>()
                        .like(commentQuery.getKeyWordComment() != null, ProductComment::getContent, commentQuery.getKeyWordComment())
                        .orderByAsc(ProductComment::getCreateTime));
        return Result.success(new PageBean<>(commentPage.getTotal(),commentPage.getRecords()));
    }

    private Long getParentIdFromPath(String path) {
        if (!path.contains(".")) {
            return null;
        }
        String parentPath = path.substring(0, path.lastIndexOf("."));
        ProductComment parent = productCommentMapper.selectOne(new LambdaQueryWrapper<ProductComment>()
                .eq(ProductComment::getPath, parentPath)
                .select(ProductComment::getId));
        return parent == null ? null : parent.getId();
    }
}
