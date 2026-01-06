package com.yyblcc.ecommerceplatforms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyblcc.ecommerceplatforms.constant.StatusConstant;
import com.yyblcc.ecommerceplatforms.domain.DTO.*;
import com.yyblcc.ecommerceplatforms.domain.Enum.ProductStatusEnum;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductAdminVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductListVO;
import com.yyblcc.ecommerceplatforms.domain.VO.ProductShowWorkShopVO;
import com.yyblcc.ecommerceplatforms.domain.VO.WorkShopVO;
import com.yyblcc.ecommerceplatforms.domain.message.ProductLikeFavoriteMessage;
import com.yyblcc.ecommerceplatforms.domain.po.*;
import com.yyblcc.ecommerceplatforms.domain.query.ProductQuery;
import com.yyblcc.ecommerceplatforms.exception.BusinessException;
import com.yyblcc.ecommerceplatforms.mapper.*;
import com.yyblcc.ecommerceplatforms.service.CategoryService;
import com.yyblcc.ecommerceplatforms.service.CraftsmanService;
import com.yyblcc.ecommerceplatforms.service.ProductService;
import com.yyblcc.ecommerceplatforms.util.StpKit;
import com.yyblcc.ecommerceplatforms.util.context.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImplement extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;

    private final CraftsmanService craftsmanService;

    private final StringRedisTemplate stringRedisTemplate;

    private final CategoryService categoryService;

    private final ProductFavoriteMapper productFavoriteMapper;

    private final ProductLikeMapper productLikeMapper;
    private final RocketMQTemplate rocketMQTemplate;
    private final CraftsmanMapper craftsmanMapper;
    private final WorkShopMapper workShopMapper;

    @Override
    public PageBean<ProductAdminVO> adminPage(ProductQuery query) {
        boolean isCondition = query.getKeyword() != null || query.getCategoryId() != null || query.getStatus() != null;
        String key = "product:page:"+query.getPage()+":"+query.getPageSize();
        if (!isCondition){
            String cacheStr = stringRedisTemplate.opsForValue().get(key);
            if (cacheStr != null){
                if (cacheStr.isEmpty()) {
                    return new PageBean<>();
                }
                return JSON.parseObject(cacheStr, PageBean.class);
            }
        }

        Page<Product> productPage = productMapper.selectPage(new Page<>(query.getPage(),query.getPageSize()),
                new LambdaQueryWrapper<Product>()
                        .like(query.getKeyword()!= null, Product::getProductName, query.getKeyword())
                        .eq(query.getCategoryId()!= null, Product::getCategoryId, query.getCategoryId())
                        .eq(query.getStatus() != null, Product::getStatus, query.getStatus())
                        .orderByDesc(Product::getCreateTime));

        List<ProductAdminVO> productAdminVOList = productPage.getRecords().stream().map(this::convertToVO).toList();
        PageBean<ProductAdminVO> pageBean = new PageBean<>(productPage.getTotal(), productAdminVOList);
        if (!isCondition){
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(pageBean), Duration.ofMinutes(10));
        }
        return pageBean;
    }

    private ProductAdminVO convertToVO(Product product) {
        ProductAdminVO productAdminVO = new ProductAdminVO();
        BeanUtils.copyProperties(product,productAdminVO);
        productAdminVO.setCraftsmanName(craftsmanService.query().eq("id",product.getCraftsmanId()).one().getName());
        productAdminVO.setCategoryName(categoryService.query().eq("id",product.getCategoryId()).one().getCategoryName());
        return productAdminVO;
    }

    @Override
    @Transactional
    public void review(ProductDTO reviewDTO) {
        Product product = checkProductExists(reviewDTO.getId());
        if (!ProductStatusEnum.PENDING.getCode().equals(product.getReviewStatus())) {
            throw new BusinessException("只有待审核状态的商品才能被审核");
        }
        if (ProductStatusEnum.REJECTED.getCode().equals(reviewDTO.getStatus())){
            product.setRejectReason(reviewDTO.getRejectReason());
        }
        product.setReviewStatus(reviewDTO.getStatus());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        stringRedisTemplate.keys("product:page:*").forEach(stringRedisTemplate::delete);
        stringRedisTemplate.keys("craftsman:product:page:*").forEach(stringRedisTemplate::delete);
    }

    @Override
    @Transactional
    public void changeStatus(Long productId, Integer status) {
        Product product = checkProductExists(productId);
        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        stringRedisTemplate.keys("product:page:*").forEach(stringRedisTemplate::delete);
        stringRedisTemplate.keys("craftsman:product:page:*").forEach(stringRedisTemplate::delete);
    }

    @Override
    public boolean removeByIds(List<Long> ids) {
        if(productMapper.deleteByIds(ids) > 0){
            return true;
        }
        return false;
    }


    @Override
    @Transactional
    public void craftsmanSave(ProductDTO dto, Long craftsmanId) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setCraftsmanId(craftsmanId);
        // 提交即待审核
        product.setReviewStatus(ProductStatusEnum.PENDING.getCode());
        product.setStatus(ProductStatusEnum.OFFLINE.getCode());
        product.setIsDeleted(0);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product);
        stringRedisTemplate.keys("product:page:*").forEach(stringRedisTemplate::delete);
        stringRedisTemplate.keys("craftsman:product:page:*").forEach(stringRedisTemplate::delete);
    }

    @Override
    @Transactional
    public Result<?> craftsmanUpdate(ProductDTO dto, Long craftsmanId) {
        Product product = checkProductExists(dto.getId());
        if (product.getStatus().equals(ProductStatusEnum.LISTED.getCode())) {
            return Result.error("请下架后再修改");
        }
        BeanUtils.copyProperties(dto, product);
        // 修改后重新进入待审核
        product.setReviewStatus(ProductStatusEnum.PENDING.getCode());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        stringRedisTemplate.keys("product:page:*").forEach(stringRedisTemplate::delete);
        stringRedisTemplate.keys("craftsman:product:page:*").forEach(stringRedisTemplate::delete);
        return Result.success("修改成功，请等待管理员审核");
    }

    @Override
    public Result<PageBean> myPage(ProductQuery query) {
        Long craftsmanId = StpKit.CRAFTSMAN.getLoginIdAsLong();
        String key = "craftsman:product:page:"+query.getPage()+":"+query.getPageSize();
        boolean isCondition = query.getKeyword() != null || query.getStatus() != null;
        if (!isCondition){
            try{
                String craftsmanProductStr = stringRedisTemplate.opsForValue().get(key);
                if (craftsmanProductStr != null) {
                    if (craftsmanProductStr.isEmpty()) {
                        return Result.success(new PageBean<>());
                    }
                    return Result.success(JSON.parseObject(craftsmanProductStr, PageBean.class));
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
            }
        }
        Page<Product> productPage = productMapper.selectPage(new Page<>(query.getPage(),query.getPageSize()),
                new LambdaQueryWrapper<Product>()
                        .like(query.getKeyword() != null, Product::getProductName, query.getKeyword())
                        .eq(query.getStatus() != null, Product::getStatus, query.getStatus())
                        .eq(Product::getCraftsmanId,craftsmanId)
                        .orderByDesc(Product::getCreateTime));
        PageBean<Product> pageBean = new PageBean<>(productPage.getTotal(),productPage.getRecords());
        if (!isCondition){
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(pageBean), Duration.ofMinutes(10));
        }
        return Result.success(pageBean);
    }

    @Override
    @Transactional
    public Result<?> craftsmanOffline(Long productId, Long craftsmanId) {
        Product product = checkProductExists(productId);
        if (ProductStatusEnum.LISTED.getCode().equals(product.getStatus())) {
            product.setStatus(ProductStatusEnum.OFFLINE.getCode());
            product.setUpdateTime(LocalDateTime.now());
            int row = productMapper.updateById(product);
            if (row > 0) {
                stringRedisTemplate.keys("craftsman:product:page:*").forEach(stringRedisTemplate::delete);
                return Result.success();
            }
        }else if(ProductStatusEnum.OFFLINE.getCode().equals(product.getStatus())){
            product.setStatus(ProductStatusEnum.LISTED.getCode());
            product.setUpdateTime(LocalDateTime.now());
            int row = productMapper.updateById(product);
            if (row > 0) {
                stringRedisTemplate.keys("craftsman:product:page:*").forEach(stringRedisTemplate::delete);
                return Result.success();
            }
        }
        return Result.error("操作失败");
    }

    @Override
    public ProductListVO getDetail(Long id) {
        Product product = checkProductExistsAndOnSale(id);
        ProductListVO vo = new ProductListVO();
        BeanUtils.copyProperties(product, vo);
        Craftsman craftsman = craftsmanMapper.selectOne(new LambdaQueryWrapper<Craftsman>().eq(Craftsman::getId, product.getCraftsmanId()));
        vo.setCraftsmanName(craftsman.getName());
        vo.setCraftsmanIntro(craftsman.getIntroduction());
        WorkShop workshop = workShopMapper.selectOne(new LambdaQueryWrapper<WorkShop>().eq(WorkShop::getId, craftsman.getWorkshopId()));
        vo.setWorkshopName(workshop.getWorkshopName());
        UserProductFavorite favorite = productFavoriteMapper.selectOne(
                new LambdaQueryWrapper<UserProductFavorite>()
                        .eq(UserProductFavorite::getProductId, id)
                        .eq(UserProductFavorite::getUserId, StpKit.USER.getLoginIdAsLong()));
        vo.setFavoriteByCurrentUser(favorite != null && favorite.getStatus().equals(1));
        return vo;
    }

    @Override
    public PageBean<ProductListVO> frontPage(ProductQuery search) {
        boolean isCondition = search.getCategoryId() != null || search.getKeyword() != null;
        String key = "user:product:page:" + search.getPage() + ":" + search.getPageSize();
        if (!isCondition){
            try{
                String jsonStr = stringRedisTemplate.opsForValue().get(key);
                if (jsonStr != null){
                    if (jsonStr.isEmpty()){
                        return new PageBean<>();
                    }
                    return JSON.parseObject(jsonStr, PageBean.class);
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
        Page<Product> page = productMapper.selectPage(new Page<>(search.getPage(), search.getPageSize()), new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, ProductStatusEnum.LISTED.getCode())
                .eq(search.getCategoryId() != null, Product::getCategoryId, search.getCategoryId())
                .like(search.getKeyword() != null, Product::getProductName, search.getKeyword())
                .orderByDesc(Product::getCreateTime));

        Page<ProductListVO> productListVOPage = new Page<>();
        BeanUtils.copyProperties(page, productListVOPage, "records");
        productListVOPage.setRecords(page.getRecords().stream().map(p -> {
            ProductListVO vo = new ProductListVO();
            BeanUtils.copyProperties(p, vo);
            vo.setImageUrl(p.getImageUrl());
            return vo;
        }).toList());
        PageBean pageBean = new PageBean(productListVOPage.getTotal(), productListVOPage.getRecords());
        if (!isCondition){
            stringRedisTemplate.opsForValue().set("user:product:page:"+search.getPage()+":"+search.getPageSize(), JSON.toJSONString(pageBean), Duration.ofMinutes(10));
        }
        return pageBean;
    }

    @Override
    public Result<List<ProductListVO>> recommend(Integer size) {
        return Result.success(productMapper.selectList(Wrappers.lambdaQuery(Product.class)
                        .eq(Product::getStatus, ProductStatusEnum.LISTED.getCode())
                        .orderByDesc(Product::getSaleCount)
                        .last("LIMIT " + size))
                .stream()
                .map(p -> {
                    ProductListVO vo = new ProductListVO();
                    BeanUtils.copyProperties(p, vo);
                    vo.setImageUrl(p.getImageUrl());
                    return vo;
                }).toList());
    }

    @Override
    public List<ProductListVO> listByCraftsman(Long craftsmanId) {
        return productMapper.selectList(Wrappers.lambdaQuery(Product.class)
                        .eq(Product::getCraftsmanId, craftsmanId)
                        .eq(Product::getStatus, ProductStatusEnum.LISTED.getCode()))
                .stream()
                .map(p -> {
                    ProductListVO vo = new ProductListVO();
                    BeanUtils.copyProperties(p, vo);
                    return vo;
                }).toList();
    }

    @Override
    public Result<String> favorite(Long productId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        Product product = checkProductExists(productId);
        if (product.getStatus().equals(ProductStatusEnum.OFFLINE.getCode())) {
            return Result.error("商品已下架");
        }
        String requestId = userId + "_" + productId + "_FAVORITE_" + System.currentTimeMillis();

        ProductLikeFavoriteMessage message = ProductLikeFavoriteMessage.builder()
                .userId(userId)
                .productId(productId)
                .requestId(requestId)
                .type("FAVORITE")
                .build();

        boolean currentFavorite = isFavorited(userId, productId);
        message.setAction(currentFavorite ? -1 : 1);
        log.warn("当前的currentFavorite状态为:{} message的action是:{}", currentFavorite, message.getAction());
        rocketMQTemplate.asyncSend("product-like-favorite-topic", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("收藏消息发送成功:{}", requestId);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("收藏消息发送失败",throwable);
            }
        });
        return Result.success(currentFavorite ? "取消收藏" : "已收藏");
    }

    @Override
    public Result<List<ProductListVO>> getMyFavorite() {
        Long userId = StpKit.USER.getLoginIdAsLong();
        List<UserProductFavorite> favoriteList = productFavoriteMapper.selectList(new LambdaQueryWrapper<UserProductFavorite>()
                .eq(UserProductFavorite::getUserId, userId)
                .eq(UserProductFavorite::getStatus, StatusConstant.ENABLE)
                .orderByDesc(UserProductFavorite::getCreateTime)
                .last("FOR UPDATE"));
        if (favoriteList == null) {
            //没有收藏则返回空
            return Result.success();
        }
        List<ProductListVO> productListVOList = new ArrayList<>();
        for (UserProductFavorite favorite : favoriteList) {
            Product product = productMapper.selectOne(Wrappers.lambdaQuery(Product.class).eq(Product::getId, favorite.getProductId()));
            ProductListVO vo = new ProductListVO();
            BeanUtils.copyProperties(product, vo);
            Craftsman craftsman = craftsmanMapper.selectOne(Wrappers.lambdaQuery(Craftsman.class).eq(Craftsman::getId, product.getCraftsmanId()));
            vo.setCraftsmanName(craftsman.getName());
            vo.setCraftsmanIntro(craftsman.getIntroduction());
            WorkShop workshop = workShopMapper.selectOne(Wrappers.lambdaQuery(WorkShop.class).eq(WorkShop::getId, craftsman.getWorkshopId()));
            vo.setWorkshopName(workshop.getWorkshopName());
            vo.setFavoriteByCurrentUser(isFavorited(userId, product.getId()));
            productListVOList.add(vo);
        }
        return Result.success(productListVOList);
    }

    @Override
    public Result like(Long productId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        Product product = checkProductExists(productId);
        if (product.getStatus().equals(ProductStatusEnum.OFFLINE.getCode())) {
            return Result.error("商品已下架");
        }

        String requestId = userId + "_" + productId + "_LIKE_" + System.currentTimeMillis();

        ProductLikeFavoriteMessage message = ProductLikeFavoriteMessage.builder()
                .userId(userId)
                .productId(productId)
                .requestId(requestId)
                .type("LIKE")
                .build();

        boolean currentLiked = isLiked(userId, productId);
        message.setAction(currentLiked ? -1 : 1);
        rocketMQTemplate.asyncSend("product-like-favorite-topic", message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("点赞消息发送成功:{}", requestId);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("点赞消息发送失败",throwable);
            }
        });
        return Result.success(currentLiked ? "取消点赞" : "已点赞");
    }

    @Override
    public Result<List<ProductListVO>> getMyLike(Long productId) {
        Long userId = StpKit.USER.getLoginIdAsLong();
        List<UserProductLike> likeList = productLikeMapper.selectList(new LambdaQueryWrapper<UserProductLike>()
                .eq(UserProductLike::getUserId, userId)
                .eq(UserProductLike::getProductId, productId)
                .orderByDesc(UserProductLike::getCreateTime)
                .last("FOR UPDATE"));
        if (likeList == null) {
            return Result.success();
        }
        List<ProductListVO> productListVOList = new ArrayList<>();
        for (UserProductLike productLike : likeList) {
            Product product = productMapper.selectOne(Wrappers.lambdaQuery(Product.class)
                    .eq(Product::getId, productLike.getProductId()));
            ProductListVO productVo = new ProductListVO();
            BeanUtils.copyProperties(product, productVo);
            productListVOList.add(productVo);
        }
        return Result.success(productListVOList);
    }

    @Override
    public Result updateProduct(ProductDTO productDTO) {
        Product existingProduct = checkProductExists(productDTO.getId());
        BeanUtils.copyProperties(productDTO, existingProduct);
        existingProduct.setUpdateTime(LocalDateTime.now());
        if (productMapper.updateById(existingProduct) > 0) {
            stringRedisTemplate.keys("product:page:*").forEach(stringRedisTemplate::delete);
            stringRedisTemplate.keys("craftsman:product:page:*").forEach(stringRedisTemplate::delete);
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @Override
    public Result<?> getProductWorkShop(Long productId) {
        Product product = checkProductExistsAndOnSale(productId);
        Long craftsmanId = product.getCraftsmanId();
        WorkShop workShop = workShopMapper.selectOne(new LambdaQueryWrapper<WorkShop>().eq(WorkShop::getCraftsmanId, craftsmanId));
        ProductShowWorkShopVO workShopVO = toWorkShopVO(workShop);
        List<String> productsImage = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getCraftsmanId, craftsmanId)
                .eq(Product::getStatus, ProductStatusEnum.LISTED.getCode())
                .orderByDesc(Product::getSaleCount)
                .last("LIMIT 4")).stream().map(Product::getImageUrl).map(List::getFirst).toList();
        workShopVO.setImages(productsImage);
        return Result.success(workShopVO);
    }

    @Override
    public Result<List<ProductListVO>> referRecommend(Long productId) {
        Product product = checkProductExistsAndOnSale(productId);
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, product.getCategoryId())
                        .ne(Product::getId, productId)
                        .orderByDesc(Product::getSaleCount));
        return Result.success(products.stream().map(p -> {
            ProductListVO vo = new ProductListVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).toList());
    }

    private ProductShowWorkShopVO toWorkShopVO(WorkShop workShop) {
        ProductShowWorkShopVO vo = new ProductShowWorkShopVO();
        BeanUtils.copyProperties(workShop, vo);
        vo.setWorkshopId(workShop.getId());
        return vo;
    }

    private Product checkProductExists(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getIsDeleted() == 1 ) {
            throw new BusinessException("商品不存在或已下架");
        }
        return product;
    }

    private Product checkProductExistsAndOnSale(Long id) {
        Product product = checkProductExists(id);
        if (!ProductStatusEnum.LISTED.getCode().equals(product.getStatus())) {
            throw new BusinessException("商品已下架或不存在");
        }
        return product;
    }


    public boolean isLiked(Long userId,Long productId) {
        UserProductLike productLike = productLikeMapper.selectOne(new LambdaQueryWrapper<UserProductLike>()
                .eq(UserProductLike::getProductId, productId)
                .eq(UserProductLike::getUserId, userId));
        if (productLike == null) {
            return false;
        }else {
            return productLike.getStatus().equals(1);
        }
    }

    public boolean isFavorited(Long userId,Long productId) {
        UserProductFavorite productFavorite = productFavoriteMapper.selectOne(
                new LambdaQueryWrapper<UserProductFavorite>()
                        .eq(UserProductFavorite::getUserId, userId)
                        .eq(UserProductFavorite::getProductId, productId)
                );
        if (productFavorite == null) {
            log.warn("进入到此处");
            return false;
        }else {
            return productFavorite.getStatus().equals(1);
        }
    }
}
